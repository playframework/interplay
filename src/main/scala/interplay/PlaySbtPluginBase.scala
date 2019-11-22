package interplay

import sbt.{ AutoPlugin, ThisBuild, Opts }
import sbt.Keys.{sbtPlugin,publishTo,version,isSnapshot,publishMavenStyle}

/**
 * Base Plugin for Play sbt plugins.
 *
 * - Publishes the plugin to bintray, or sonatype snapshots if it's a snapshot build.
 * - Adds scripted configuration.
 */
object PlaySbtPluginBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBintrayBase && PlayBuildBase && PlaySbtBuildBase

  import PlayBuildBase.autoImport._

  override def projectSettings = PlaySbtCompat.scriptedSettings /* FIXME: Not needed in sbt 1 */ ++ Seq(
    PlaySbtCompat.scriptedLaunchOpts += (version apply { v => s"-Dproject.version=$v" }).value,
    sbtPlugin := true,
    publishTo := {
      val currentValue = publishTo.value
      if (isSnapshot.value) {
        Some(Opts.resolver.sonatypeSnapshots)
      } else currentValue
    },

    publishMavenStyle := isSnapshot.value,
    playBuildPromoteBintray in ThisBuild := true
  )
}
