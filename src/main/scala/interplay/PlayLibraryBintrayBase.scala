package interplay

import bintray.BintrayPlugin.autoImport.bintrayRepository
import interplay.Omnidoc.autoImport.{omnidocGithubRepo, omnidocTagPrefix}
import sbt.Keys._
import sbt.{AutoPlugin, ThisBuild}

/**
 * Base Plugin for Play libraries that are published to Bintray.
 *
 * - Publishes to Bintray
 * - Includes omnidoc configuration
 * - Cross builds the project
 */
object PlayLibraryBintrayBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBuildBase && PlayBintrayBase && Omnidoc

  import PlayBuildBase.autoImport._

  override def projectSettings = Seq(
    omnidocGithubRepo := s"playframework/${(playBuildRepoName in ThisBuild).value}",
    omnidocTagPrefix := "",
    javacOptions in compile ++= Seq("-source", "1.8", "-target", "1.8"),
    javacOptions in doc := Seq("-source", "1.8"),
    crossScalaVersions := Seq(scalaVersion.value, ScalaVersions.scala212),
    scalaVersion := sys.props.get("scala.version").getOrElse(ScalaVersions.scala213),
    playCrossBuildRootProject in ThisBuild := true,
    bintrayRepository := (if (isSnapshot.value) "snapshots" else "maven")
  )
}
