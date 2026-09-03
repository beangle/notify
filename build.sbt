import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

organization := "org.beangle.notify"
version := "0.1.28-SNAPSHOT"

scmInfo := Some(
  ScmInfo(
    uri("https://github.com/beangle/notify"),
    "scm:git@github.com:beangle/notify.git"
  )
)

developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = uri("http://github.com/duantihua")
  )
)

description := "The Beangle Notify Library"
homepage := Some(uri("https://beangle.github.io/notify/index.html"))

val beangle_commons = "org.beangle.commons" % "beangle-commons" % "6.3.2"
val beangle_cache = "org.beangle.cache" % "beangle-cache" % "0.1.21"

lazy val root = (project in file("."))
  .settings(
    name := "beangle-notify",
    common,
    Compile / run / mainClass := Some("org.beangle.notify.Main"),
    libraryDependencies ++= Seq(slf4j, logback_classic % "test", greenmail, scalatest),
    libraryDependencies ++= Seq(beangle_commons, beangle_cache, jakarta_mail_api, jakarta_mail_angus),
    libraryDependencies ++= Seq(jedis % "optional")
  )
