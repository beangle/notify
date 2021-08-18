import Dependencies._
import BuildSettings._
import sbt.url

ThisBuild / organization := "org.beangle.notify"
ThisBuild / version := "0.0.2-SNAPSHOT"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/beangle/notify"),
    "scm:git@github.com:beangle/notify.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "chaostone",
    name  = "Tihua Duan",
    email = "duantihua@gmail.com",
    url   = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "The Beangle Notify Library"
ThisBuild / homepage := Some(url("https://beangle.github.io/notify/index.html"))
ThisBuild / resolvers += Resolver.mavenLocal

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

publish / skip := true
