package interplay

import sbt.{ AutoPlugin, ThisBuild }
import sbt.Keys._
import interplay.Omnidoc.autoImport.omnidocGithubRepo
import interplay.Omnidoc.autoImport.omnidocTagPrefix

/**
 * Base Plugin for Play libraries.
 *
 * - Publishes to sonatype
 * - Includes omnidoc configuration
 * - Cross builds the project
 */
  object PlayLibraryBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBuildBase && PlaySonatypeBase && Omnidoc

  import PlayBuildBase.autoImport._

  override def projectSettings = Seq(
    omnidocGithubRepo := s"playframework/${(ThisBuild / playBuildRepoName).value}",
    omnidocTagPrefix := "",
    compile / javacOptions ++= Seq("--release", "11"),
    doc / javacOptions := Seq("-source", "11"),
    crossScalaVersions := Seq(scalaVersion.value), // TODO: Add ScalaVersions.scala3
    scalaVersion := sys.props.get("scala.version").getOrElse(ScalaVersions.scala213),
  )
}
