package interplay

import sbt.{ AutoPlugin, ThisBuild }
import sbt.Keys.crossScalaVersions

/**
 * Base Plugin for a root project that doesn't get published.
 *
 * - Contains release configuration
 */
object PlayRootProjectBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = PlayBuildBase && PlayBintrayBase && PlaySonatypeBase && PlayReleaseBase

  import PlayBuildBase.autoImport._

  override def projectSettings = PlayNoPublishBase.projectSettings ++ Seq(
    crossScalaVersions := {
      if ((playCrossBuildRootProject in ThisBuild).?.value.exists(identity)) {
        Seq(ScalaVersions.scala212, ScalaVersions.scala213)
      } else {
        Seq(ScalaVersions.scala210, ScalaVersions.scala212)
      }
    }
  )
}
