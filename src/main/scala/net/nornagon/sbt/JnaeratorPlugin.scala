package net.nornagon.sbt

import java.io.File
import sbt.{config => sbtConfig, _}
import sbt.Keys.{cleanFiles, libraryDependencies, managedSourceDirectories,
  sourceDirectories, sourceDirectory, sourceGenerators, sourceManaged, streams,
  watchSources}

object JnaeratorPlugin extends AutoPlugin {
  object autoImport {

    val jnaerator = sbtConfig("jnaerator")

    val jnaeratorTargets = TaskKey[Seq[Jnaerator.Target]]("jnaerator-targets",
      "List of header-files and corresponding configuration for java interface generation")
    val jnaeratorGenerate = TaskKey[Seq[File]]("jnaerator-generate",
      "Run jnaerate and generate interfaces")
    val jnaeratorRuntime = SettingKey[Jnaerator.Runtime]("which runtime to use")
    val jnaeratorRuntimeVersion = SettingKey[String]("version of the runtime to use")

    object Jnaerator {
      sealed trait Runtime
      object Runtime {
        case object JNA extends Runtime
        case object BridJ extends Runtime
      }
      case class Target(
        headerFiles: Seq[File],
        packageName: String,
        libraryName: String,
        extraArgs: Seq[String] = Nil
      )

      lazy val settings = inConfig(jnaerator)(Seq(
        sourceDirectory := ((sourceDirectory in Compile) { _ / "native" }).value,
        sourceDirectories := ((sourceDirectory in Compile) { _ :: Nil }).value,
        sourceManaged := ((sourceManaged in Compile) { _ / "jnaerator_interfaces" }).value,
        jnaeratorGenerate <<= runJnaerator
      )) ++ Seq(
        jnaeratorTargets := Nil,
        jnaeratorRuntime := Runtime.BridJ,
        jnaeratorRuntimeVersion := ((jnaeratorRuntime in jnaerator) {
          /* Latest versions against which the targetted version of JNAerator is
           * known to be compatible */
          case Runtime.JNA => "4.2.1"
          case Runtime.BridJ => "0.7.0"
        }).value,
        cleanFiles += (sourceManaged in jnaerator).value,

        watchSources ++= (jnaeratorTargets in jnaerator).map { _.flatMap(_.headerFiles) }.value,
        watchSources += file("."),

        sourceGenerators in Compile += (jnaeratorGenerate in jnaerator).taskValue,
        managedSourceDirectories in Compile += (sourceManaged in jnaerator).value,
        libraryDependencies += (jnaeratorRuntime in jnaerator, jnaeratorRuntimeVersion in jnaerator).apply {
          case (Jnaerator.Runtime.JNA, v) =>
            "net.java.dev.jna" % "jna" % v
          case (Jnaerator.Runtime.BridJ, v) =>
            "com.nativelibs4java" % "bridj" % v
        }.value
      )
    }

    private def runJnaerator: Def.Initialize[Task[Seq[File]]] = Def.task {

      val targets = (jnaeratorTargets in jnaerator).value
      val s = (streams.value)
      val runtime = (jnaeratorRuntime in jnaerator).value
      val outputPath = (sourceManaged in jnaerator).value

      val targetId = "c" + (targets.toList.map { target =>
        (target, runtime, outputPath)
      }).hashCode
      val cachedCompile = FileFunction.cached(
        s.cacheDirectory / "jnaerator" / targetId,
        inStyle = FilesInfo.lastModified,
        outStyle = FilesInfo.exists
      ) { (_: Set[File]) =>
        IO.delete(outputPath)
        outputPath.mkdirs()

        targets.flatMap { target =>
          val args = Seq(
            "-package", target.packageName,
            "-library", target.libraryName,
            "-o", outputPath.getCanonicalPath,
            "-mode", "Directory",
            "-runtime", runtime.toString,
            "-f", "-scalaStructSetters"
          ) ++ target.extraArgs ++ target.headerFiles.map(_.getCanonicalPath)

          s.log.info(s"(${target.headerFiles.map(_.getName).mkString(",")}) Running JNAerator with args ${args.mkString(" ")}")
          try {
            com.ochafik.lang.jnaerator.JNAerator.main(args.toArray)
          } catch { case e: Exception =>
              throw new RuntimeException(s"error occured while running jnaerator: ${e.getMessage}", e)
          }

          (outputPath ** "*.java").get
        }.toSet
      }
      cachedCompile(targets.flatMap(_.headerFiles).toSet).toSeq
    }
  }


}
