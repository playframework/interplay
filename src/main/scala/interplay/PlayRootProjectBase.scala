package interplay

import sbt._

/**
 * Base Plugin for a root project that doesn't get published.
 *
 * - Contains release configuration
 */
object PlayRootProjectBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = PlayBuildBase && PlaySonatypeBase && PlayReleaseBase
  override def projectSettings = PlayNoPublishBase.projectSettings
}
