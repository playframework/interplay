package interplay

import sbt.AutoPlugin
import sbt.Keys.{ javacOptions, crossScalaVersions, sbtVersion, compile, doc, pluginCrossBuild }

private object PlaySbtBuildBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBuildBase

  override def projectSettings = Seq(
    crossScalaVersions := Seq(ScalaVersions.scala212),
    sbtVersion in pluginCrossBuild := SbtVersions.sbt10,
    javacOptions in compile ++= Seq("-source", "1.8", "-target", "1.8"),
    javacOptions in doc := Seq("-source", "1.8")
  )
}
