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

  override def projectSettings = ScriptedPlugin.projectSettings ++ Seq(
    ScriptedPlugin.autoImport.scriptedLaunchOpts += (version apply { v => s"-Dproject.version=$v" }).value,
    sbtPlugin := true,
    scalaVersion := sys.props.get("scala.version").getOrElse(ScalaVersions.scala212),
    crossScalaVersions := Seq(ScalaVersions.scala212),
    publishTo := {
      val pubTo = publishTo.value
      if (isSnapshot.value) Some(Opts.resolver.sonatypeSnapshots)
      else pubTo
    },

    publishMavenStyle := isSnapshot.value,
    playBuildPromoteBintray in ThisBuild := true,

    (javacOptions in compile) ++= Seq("-source", "1.8", "-target", "1.8"),
    (javacOptions in doc) := Seq("-source", "1.8")
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
    (javacOptions in compile) := Seq("-source", "1.8", "-target", "1.8"),
    (javacOptions in doc) := Seq("-source", "1.8"),
    crossScalaVersions := Seq(ScalaVersions.scala212)
  )
}