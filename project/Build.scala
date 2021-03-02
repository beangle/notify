import sbt.Keys._
import sbt._

object BuildSettings {
  val buildOrganization = "org.beangle.notify"
  val buildVersion = "0.0.1-SNAPSHOT"
  val buildScalaVersion = "3.0.0-RC1"

  val commonSettings = Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions := Seq("-encoding", "UTF-8", "-feature", "-unchecked", "-deprecation"
      , "-language:implicitConversions", "-Xtarget:11", "-Xfatal-warnings"),
    // "-rewrite","-indent","-source:3.0-migration",
    crossPaths := false)
}

object Dependencies {
  val h2Ver = "1.3.172"
  val slf4jVer = "2.0.0-alpha1"
  val mockitoVer = "3.1.0"
  val logbackVer = "1.3.0-alpha5"
  val scalatestVer = "3.2.5"
  val commonsVer = "5.2.0"

  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVer
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVer % "test"
  val mockito = "org.mockito" % "mockito-core" % mockitoVer % "test"

  val commonsCore = "org.beangle.commons" % "beangle-commons-core_2.13" % commonsVer
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVer % "test"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVer % "test"

  val jakartamail = "jakarta.mail" % "jakarta.mail-api" % "1.6.5"
  val sunmail = "com.sun.mail" % "jakarta.mail" % "1.6.5"
  val greenmail = "com.icegreen" % "greenmail" % "1.6.1" % "test"

  val commonDeps = Seq(slf4j, logbackClassic, logbackCore, commonsCore, scalatest)
}
