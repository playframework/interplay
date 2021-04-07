package interplay

import sbt.{ AutoPlugin }

/**
 * Base Plugin for Play SBT libraries.
 *
 * - Publishes to sonatype
 */
object PlaySbtLibraryBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBuildBase && PlaySbtBuildBase && PlaySonatypeBase

}
