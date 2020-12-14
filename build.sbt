import scala.concurrent.duration._
import Dependencies._
import BuildSettings._
// factor out common settings
ThisBuild / organization := "org.beangle.notify"
ThisBuild / scalaVersion := "2.13.3"
// set the Scala version used for the project
ThisBuild / version := "0.0.1-SNAPSHOT"

// set the prompt (for this build) to include the project id.
ThisBuild / shellPrompt := { state => Project.extract(state).currentRef.project + "> " }

lazy val root = (project in file("."))
  .settings()
  .aggregate(core)

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-notify-core",
    commonSettings,
    libraryDependencies ++= (commonDeps),
    libraryDependencies ++= Seq(jakartamail,sunmail,greenmail)
  )

