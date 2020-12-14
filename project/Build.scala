import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.beangle.notify"
  val buildVersion = "0.0.1-SNAPSHOT"
  val buildScalaVersion = "2.13.3"

  val commonSettings = Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-target:jvm-1.8"),
    crossPaths := false)
}

object Dependencies {
  val h2Ver = "1.3.172"
  val slf4jVer = "2.0.0-alpha1"
  val mockitoVer = "3.1.0"
  val logbackVer = "1.3.0-alpha5"
  val scalatestVer = "3.2.2"
  val commonsVer = "5.2.0"

  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVer
  val scalatest = "org.scalatest" % "scalatest_2.13" % scalatestVer % "test"
  val mockito = "org.mockito" % "mockito-core" % mockitoVer % "test"

  val commonsCore = "org.beangle.commons" % "beangle-commons-core_2.13" % commonsVer
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVer % "test"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVer % "test"

  val javamail = "jakarta.mail" % "jakarta.mail-api" % "2.0.0"
  val sunmail = "com.sun.mail" % "jakarta.mail" % "2.0.0"
  val greenmail = "com.icegreen" % "greenmail" % "1.6.1"

  val commonDeps = Seq(slf4j, logbackClassic, logbackCore, commonsCore, scalatest)
}
