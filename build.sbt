import org.beangle.parent.Dependencies._
import org.beangle.parent.Settings._

ThisBuild / organization := "org.beangle.notify"
ThisBuild / version := "0.0.3"

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


val beangle_commons_core = "org.beangle.commons" %% "beangle-commons-core" % "5.2.6"

val commonDeps = Seq(logback_classic, logback_core, beangle_commons_core, scalatest)

lazy val root = (project in file("."))
  .settings()
  .aggregate(core)

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-notify-core",
    common,
    libraryDependencies ++= (commonDeps),
    libraryDependencies ++= Seq(jakarta_mail_api,jakarta_mail,greenmail)
  )

publish / skip := true
