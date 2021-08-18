import sbt.Keys._
import sbt._

object BuildSettings {
  val buildScalaVersion = "3.0.1"

  val commonSettings = Seq(
    organizationName := "The Beangle Software",
    licenses += ("GNU Lesser General Public License version 3", new URL("http://www.gnu.org/licenses/lgpl-3.0.txt")),
    startYear := Some(2005),
    scalaVersion := buildScalaVersion,
    scalacOptions := Seq("-Xtarget:11", "-deprecation", "-feature"),
    crossPaths := true,

    publishMavenStyle := true,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishM2Configuration := publishM2Configuration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),

    versionScheme := Some("early-semver"),
    pomIncludeRepository := { _ => false }, // Remove all additional repository other than Maven Central from POM
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
    })
}

object Dependencies {
  val logbackVer = "1.2.4"
  val scalatestVer = "3.2.9"
  val scalaxmlVer = "2.0.1"
  val commonsVer = "5.2.5"

  val scalatest = "org.scalatest" %% "scalatest" % scalatestVer % "test"
  val scalaxml = "org.scala-lang.modules" %% "scala-xml" % scalaxmlVer
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVer % "test"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVer % "test"
  val commonsCore = "org.beangle.commons" %% "beangle-commons-core" % commonsVer

  val jakartamail = "jakarta.mail" % "jakarta.mail-api" % "1.6.5"
  val sunmail = "com.sun.mail" % "jakarta.mail" % "1.6.5"
  val greenmail = "com.icegreen" % "greenmail" % "1.6.1" % "test"

  val commonDeps = Seq(logbackClassic, logbackCore, commonsCore, scalatest)
}
