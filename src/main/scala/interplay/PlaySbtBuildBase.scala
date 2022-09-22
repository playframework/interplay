package interplay

import sbt._
import sbt.Keys._

private[interplay] object PlaySbtBuildBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBuildBase

  override def projectSettings = Seq(
    scalaVersion := ScalaVersions.scala212,
    crossScalaVersions := Seq(ScalaVersions.scala212),
    pluginCrossBuild / sbtVersion := SbtVersions.sbt17,
    compile / javacOptions ++= Seq("--release", "11"),
    doc / javacOptions := Seq("-source", "11")
  )
}
