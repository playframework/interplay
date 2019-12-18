package interplay

import sbt._
import sbt.Keys._
import sbt.plugins.SbtPlugin
import sbt.ScriptedPlugin.autoImport._

/**
 * Base Plugin for Play sbt plugins.
 *
 * - Publishes the plugin to bintray, or sonatype snapshots if it's a snapshot build.
 * - Adds scripted configuration.
 */
object PlaySbtPluginBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBintrayBase && PlayBuildBase && PlaySbtBuildBase && SbtPlugin

  import PlayBuildBase.autoImport._

  override def projectSettings = Seq(
    scriptedLaunchOpts += (version apply { v => s"-Dproject.version=$v" }).value,
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
