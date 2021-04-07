package interplay

import sbt._
import sbt.Keys._

import interplay.PlayBuildBase.autoImport._

/**
 * Base Plugin for a root project that doesn't get published.
 *
 * - Contains release configuration
 */
object PlayRootProjectBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = PlayBuildBase && PlaySonatypeBase && PlayReleaseBase
  override def projectSettings = PlayNoPublishBase.projectSettings ++ Seq(
    crossScalaVersions := {
      if ((playCrossBuildRootProject in ThisBuild).?.value.exists(identity)) {
        Seq(ScalaVersions.scala212, ScalaVersions.scala213)
      } else {
        Seq(ScalaVersions.scala212)
      }
    }
  )
}
