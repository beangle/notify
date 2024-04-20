import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

ThisBuild / organization := "org.beangle.notify"
ThisBuild / version := "0.1.6"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/beangle/notify"),
    "scm:git@github.com:beangle/notify.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "The Beangle Notify Library"
ThisBuild / homepage := Some(url("https://beangle.github.io/notify/index.html"))


val beangle_commons = "org.beangle.commons" % "beangle-commons" % "5.6.16"

lazy val root = (project in file("."))
  .settings(
    name := "beangle-notify",
    common,
    libraryDependencies ++= Seq(logback_classic % "test", greenmail, scalatest),
    libraryDependencies ++= Seq(beangle_commons, jakarta_mail_api, jakarta_mail_angus)
  )
