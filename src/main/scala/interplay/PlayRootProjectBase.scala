package interplay

import sbt._
import sbt.Keys._

/**
 * Base Plugin for a root project that doesn't get published.
 *
 * - Contains release configuration
 */
object PlayRootProjectBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = PlayBuildBase && PlayBintrayBase && PlaySonatypeBase && PlayReleaseBase
  override def projectSettings = PlayNoPublishBase.projectSettings ++ Seq(
    crossScalaVersions := Nil
  )
}
