package interplay

import sbt._
import sbt.Keys._

private[interplay] object PlaySbtBuildBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBuildBase

  override def projectSettings = Seq(
    scalaVersion := ScalaVersions.scala212,
    crossScalaVersions := Seq(ScalaVersions.scala212),
    pluginCrossBuild / sbtVersion := SbtVersions.sbt15,
    compile / javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    doc / javacOptions := Seq("-source", "1.8")
  )
}
