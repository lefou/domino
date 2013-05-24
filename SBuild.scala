import de.tototec.sbuild._
import de.tototec.sbuild.TargetRefs._
import de.tototec.sbuild.ant._
import de.tototec.sbuild.ant.tasks._

@version("0.4.0")
@classpath("mvn:org.apache.ant:ant:1.8.4")
class SBuild(implicit _project: Project) {

  val namespace = "org.helgoboss.domino"
  val version = "1.0.0.9000"
  val jar = s"target/${namespace}-${version}.jar"

  val scalaVersion = "2.10.0"
  val scalacCp =
    s"mvn:org.scala-lang:scala-library:${scalaVersion}" ~
    s"mvn:org.scala-lang:scala-reflect:${scalaVersion}" ~
    s"mvn:org.scala-lang:scala-compiler:${scalaVersion}"
  val bndCp = "mvn:biz.aQute:bndlib:1.50.0"

  val compileCp =
    s"mvn:org.scala-lang:scala-library:${scalaVersion}" ~
    s"mvn:org.scala-lang:scala-reflect:${scalaVersion}" ~
    "mvn:org.osgi:org.osgi.core:4.3.0" ~
    "mvn:org.osgi:org.osgi.compendium:4.3.0" ~
    "mvn:org.helgoboss:scala-osgi-metatype:1.0.0" ~
    "mvn:org.helgoboss:scala-logging:1.0.0" ~
    "mvn:org.helgoboss:capsule:1.1.0"

  Target("phony:clean").evictCache exec {
    AntDelete(dir = Path("target"))
  }

  Target("phony:all") dependsOn jar

  Target("phony:compile").cacheable dependsOn scalacCp ~ compileCp ~ "scan:src/main/scala" exec {
    addons.scala.Scalac(
      compilerClasspath = scalacCp.files,
      classpath = compileCp.files,
      destDir = Path("target/classes"),
      sources = "scan:src/main/scala".files,
      debugInfo = "vars"
    )
  }

  Target(jar) dependsOn "compile" ~ bndCp ~ compileCp ~ "scan:src/main/resources" exec { ctx: TargetContext =>
    addons.bnd.BndJar(
      bndClasspath = bndCp.files,
      classpath = compileCp.files ++ Seq(Path("target/classes")),
      destFile = ctx.targetFile.get,
      props = Map(
        "Bundle-SymbolicName" -> namespace,
        "Bundle-Version" -> version,
        "Bundle-License" -> "http://www.opensource.org/licenses/mit-license.php",
        "Bundle-Name" -> "Domino",
        "Bundle-Description" -> "A lightweight Scala library for writing elegant OSGi bundle activators",
        "Export-Package" -> s"""${namespace};version="${version}",
                                ${namespace}.bundle_watching;version="${version}",
                                ${namespace}.configuration_watching;version="${version}",
                                ${namespace}.logging;version="${version}",
                                ${namespace}.service_consuming;version="${version}",
                                ${namespace}.service_providing;version="${version}",
                                ${namespace}.service_watching;version="${version}"""",
        "Import-Package" -> """scala.*;version="[2.10,2.11)",
                               *"""
      )
    )
  }

}
