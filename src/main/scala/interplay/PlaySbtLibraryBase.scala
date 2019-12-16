package interplay

import sbt.{ AutoPlugin, ThisBuild }

/**
 * Base Plugin for Play SBT libraries.
 *
 * - Publishes to sonatype
 */
object PlaySbtLibraryBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBuildBase && PlaySbtBuildBase && PlaySonatypeBase

  import PlayBuildBase.autoImport._

  override def projectSettings = Seq(
    playBuildPromoteSonatype in ThisBuild := true
  )
}
