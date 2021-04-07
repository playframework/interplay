package interplay

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbtrelease.ReleasePlugin.autoImport._
import xerial.sbt.Sonatype
import com.jsuereth.sbtpgp.SbtPgp
import com.jsuereth.sbtpgp.PgpKeys

/**
 * Plugin that defines base settings for all Play projects
 */
object PlayBuildBase extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = SbtPgp && JvmPlugin

  object autoImport {
    val playBuildExtraTests = taskKey[Unit]("Run extra tests during the release")
    val playBuildExtraPublish = taskKey[Unit]("Publish extract non aggregated projects during the release")
    val playBuildRepoName = settingKey[String]("The name of the repository in the playframework GitHub organization")
    val playCrossBuildRootProject = settingKey[Boolean]("Whether the root project should be cross built or not")

    // This is not using sbt-git because we need a more stable way to set
    // the current branch in a more stable way, for example, we may want to
    // get the current branch as "master" even if we are at a detached commit.
    //
    // This is useful when running tasks on Travis, where the builds runs in
    // a detached commit. See the discussion here:
    // https://github.com/travis-ci/travis-ci/issues/1701
    val playCurrentBranch = settingKey[String]("The current branch for the project")

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
    homepage := Some(url(s"https://github.com/playframework/${(playBuildRepoName in ThisBuild).value}")),
    licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),

    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
    javacOptions ++= Seq("-encoding", "UTF-8", "-Xlint:-options"),

    releasePublishArtifactsAction := PgpKeys.publishSigned.value,

    resolvers ++= {
      if (isSnapshot.value) {
        Seq(Opts.resolver.sonatypeSnapshots)
      } else {
        Nil
      }
    },

    // Tries to automatically set playCurrentBranch setting by reading
    // an environment variable or system property.
    //
    // Reading from a environment variable could be useful when running
    // in CI which have this automatically configured, for example Travis:
    // https://docs.travis-ci.com/user/environment-variables/#Default-Environment-Variables
    playCurrentBranch := {
      sys.env.get("CURRENT_BRANCH")
    } orElse {
      sys.props.get("currentBranch")
    } getOrElse {
      "master"
    },

    pomExtra := {
      val repoName = (playBuildRepoName in ThisBuild).value
      <scm>
        <url>https://github.com/playframework/{repoName}</url>
        <connection>scm:git:git@github.com:playframework/{repoName}.git</connection>
      </scm>
        <developers>
          <developer>
            <id>playframework</id>
            <name>Play Framework Team</name>
            <url>https://github.com/playframework</url>
          </developer>
        </developers>
    },
    pomIncludeRepository := { _ => false }
  )
}
