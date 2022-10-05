package interplay

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import xerial.sbt.Sonatype
import com.jsuereth.sbtpgp.SbtPgp

/**
 * Plugin that defines base settings for all Play projects
 */
object PlayBuildBase extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = SbtPgp && JvmPlugin

  object autoImport {
    val playBuildRepoName = settingKey[String]("The name of the repository in the playframework GitHub organization")

    /**
     * Plugins configuration for a Play sbt plugin.
     */
    def PlaySbtPlugin: Plugins = PlaySbtPluginBase

    /**
     * Plugins configuration for a Play sbt library.
     */
    def PlaySbtLibrary: Plugins = PlaySbtLibraryBase

    /**
     * Plugins configuration for a Play library.
     */
    def PlayLibrary: Plugins = PlayLibraryBase

    /**
     * Plugins configuration for a Play Root Project that doesn't get published.
     */
    def PlayRootProject: Plugins = PlayRootProjectBase

    /**
     * Plugins configuration for a Play project that doesn't get published.
     */
    def PlayNoPublish: Plugins = PlayNoPublishBase && PluginsAccessor.exclude(Sonatype)

    /**
     * Convenience function to get the Play version. Allows the version to be overridden by a system property, which is
     * necessary for the nightly build.
     */
    def playVersion(version: String): String = sys.props.getOrElse("play.version", version)
  }

  import autoImport._

  override def projectSettings = Seq(
    // General settings
    organization := "com.typesafe.play",
    homepage := Some(url(s"https://github.com/playframework/${(ThisBuild / playBuildRepoName).value}")),
    licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),

    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
    javacOptions ++= Seq("-encoding", "UTF-8", "-Xlint:-options"),

    resolvers ++= {
      if (isSnapshot.value) {
        Opts.resolver.sonatypeOssSnapshots
      } else {
        Nil
      }
    },

    developers += Developer("playframework", "Play Framework Team", "contact playframework com", url("https://github.com/playframework")),
    pomIncludeRepository := { _ => false }
  )
}
