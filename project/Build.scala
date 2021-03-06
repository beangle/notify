import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.beangle.notify"
  val buildVersion = "4.0.2-SNAPSHOT"
  val buildScalaVersion = "2.10.3"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt := ShellPrompt.buildShellPrompt,
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-target:jvm-1.6"),
    crossPaths := false)
}

object ShellPrompt {

  object devnull extends ProcessLogger {
    def info(s: ⇒ String) {}

    def error(s: ⇒ String) {}

    def buffer[T](f: ⇒ T): T = f
  }

  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
    getOrElse "-" stripPrefix "## ")

  val buildShellPrompt = {
    (state: State) ⇒
      {
        val currProject = Project.extract(state).currentProject.id
        "%s:%s:%s> ".format(
          currProject, currBranch, BuildSettings.buildVersion)
      }
  }
}

object Dependencies {
  val h2Ver = "1.3.172"
  val slf4jVer = "1.6.6"
  val mockitoVer = "1.9.5"
  val logbackVer = "1.0.7"
  val scalatestVer = "2.0.M5b"

  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVer
  val scalatest = "org.scalatest" % "scalatest_2.10" % scalatestVer % "test"
  val mockito = "org.mockito" % "mockito-core" % mockitoVer % "test"

  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVer % "test"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVer % "test"

  val javamail = "javax.mail" % "mail" % "1.4"
  val greenmail = "com.icegreen" % "greenmail" % "1.3.1b"
}

object Resolvers {
  val m2repo = "Local Maven2 Repo" at "file://" + Path.userHome + "/.m2/repository"
}

object BeangleBuild extends Build {

  import Dependencies._
  import BuildSettings._
  import Resolvers._

  val commonDeps = Seq(slf4j, logbackClassic, logbackCore, scalatest)

  lazy val notify = Project("beangle-notify", file("."), settings = buildSettings) aggregate (notify_core)

  lazy val notify_core = Project("beangle-notify-core",file("core"),
    settings = buildSettings ++ Seq(libraryDependencies ++= commonDeps ++ Seq(javamail, greenmail))
      ++ Seq(resolvers += m2repo)) dependsOn (notify_core)
 
}
