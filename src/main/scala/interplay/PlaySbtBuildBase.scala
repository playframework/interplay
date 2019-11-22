package interplay

import sbt.{AutoPlugin, CrossVersion}
import sbt.Keys.{javacOptions, crossScalaVersions, sbtVersion, compile, doc, pluginCrossBuild, scalaBinaryVersion }

/**
 * 
 */
private object PlaySbtBuildBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBuildBase

  private def choose[T](scalaBinVersion: String)(forScala210: T, forScala212: T) = CrossVersion.partialVersion(scalaBinVersion) match {
    case Some((2, 12)) => forScala212
    case _ => forScala210
  }

  override def projectSettings = Seq(
    crossScalaVersions := Seq(ScalaVersions.scala210, ScalaVersions.scala212),
    sbtVersion in pluginCrossBuild := choose(scalaBinaryVersion.value)(
      forScala210 = SbtVersions.sbt013,
      forScala212 = SbtVersions.sbt10
    ),
    javacOptions in compile ++= choose(scalaBinaryVersion.value)(
      forScala210 = Seq("-source", "1.6", "-target", "1.6"),
      forScala212 = Seq("-source", "1.8", "-target", "1.8")
    ),
    javacOptions in doc := choose(scalaBinaryVersion.value)(
      forScala210 = Seq("-source", "1.6"),
      forScala212 = Seq("-source", "1.8")
    )
  )
}
