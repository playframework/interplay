package interplay

import sbt._
import sbt.Keys._

private object PlaySbtBuildBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBuildBase

  override def projectSettings = Seq(
    scalaVersion := ScalaVersions.scala212,
    crossScalaVersions := Seq(ScalaVersions.scala212),
    sbtVersion in pluginCrossBuild := SbtVersions.sbt10,
    javacOptions in compile ++= Seq("-source", "1.8", "-target", "1.8"),
    javacOptions in doc := Seq("-source", "1.8")
  )
}
