package interplay

import sbt._
import sbt.Keys._
import sbt.plugins.SbtPlugin
import sbt.ScriptedPlugin.autoImport._

/**
 * Base Plugin for Play sbt plugins.
 *
 * - Publishes the plugin to sonatype
 * - Adds scripted configuration.
 */
object PlaySbtPluginBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlaySonatypeBase && PlayBuildBase && PlaySbtBuildBase && SbtPlugin

  override def projectSettings = Seq(
    scriptedLaunchOpts += (version apply { v => s"-Dproject.version=$v" }).value
  )
}
