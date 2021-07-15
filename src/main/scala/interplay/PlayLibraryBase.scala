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
    compile / javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    doc / javacOptions := Seq("-source", "1.8"),
    crossScalaVersions := Seq(scalaVersion.value, ScalaVersions.scala212),
    scalaVersion := sys.props.get("scala.version").getOrElse(ScalaVersions.scala213),
    ThisBuild / playCrossBuildRootProject := true
  )
}
