import Dependencies._
import BuildSettings._
import scalariform.formatter.preferences._

ThisBuild / organization := "org.beangle.notify"
ThisBuild / organizationName  := "The Beangle Software"
ThisBuild / startYear := Some(2005)
ThisBuild / licenses += ("GNU General Public License version 3", new URL("http://www.gnu.org/licenses/lgpl-3.0.txt"))
ThisBuild / scalaVersion := "3.0.0-RC1"
ThisBuild / version := "0.0.1-SNAPSHOT"

lazy val root = (project in file("."))
  .settings()
  .aggregate(core)

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-notify-core",
    commonSettings,
    libraryDependencies ++= (commonDeps),
    libraryDependencies ++= Seq(jakartamail,sunmail,greenmail)
  ).enablePlugins(StylePlugin)

publish / skip := true
