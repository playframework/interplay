package interplay

import sbt._
import sbt.Keys._

/**
 * Base Plugin for Play sbt plugins.
 *
 * - Publishes the plugin to bintray, or sonatype snapshots if it's a snapshot build.
 * - Adds scripted configuration.
 */
object PlaySbtPluginBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBintrayBase && PlayBuildBase

  import PlayBuildBase.autoImport._

  override def projectSettings = ScriptedPlugin.scriptedSettings ++ Seq(
    ScriptedPlugin.scriptedLaunchOpts += (version apply { v => s"-Dproject.version=$v" }).value,
    sbtPlugin := true,
    scalaVersion := sys.props.get("scala.version").getOrElse(ScalaVersions.scala210),
    crossScalaVersions := Seq(ScalaVersions.scala210),
    publishTo := {
      if (isSnapshot.value) {
        Some(Opts.resolver.sonatypeSnapshots)
      } else publishTo.value
    },

    publishMavenStyle := isSnapshot.value,
    playBuildPromoteBintray in ThisBuild := true,

    (javacOptions in compile) ++= Seq("-source", "1.6", "-target", "1.6"),
    (javacOptions in doc) := Seq("-source", "1.6")
  )
}

/**
 * Base Plugin for Play SBT libraries.
 *
 * - Publishes to sonatype
 */
object PlaySbtLibraryBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBuildBase && PlaySonatypeBase

  import PlayBuildBase.autoImport._

  override def projectSettings = Seq(
    playBuildPromoteSonatype in ThisBuild := true,
    (javacOptions in compile) := Seq("-source", "1.6", "-target", "1.6"),
    (javacOptions in doc) := Seq("-source", "1.6"),
    crossScalaVersions := Seq(ScalaVersions.scala210)
  )
}