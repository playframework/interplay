package interplay

import bintray.BintrayPlugin
import bintray.BintrayPlugin.autoImport._
import com.typesafe.sbt.SbtPgp
import com.typesafe.sbt.pgp.PgpKeys
import interplay.Omnidoc.autoImport._
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport._
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.autoImport._
import sbtwhitesource.WhiteSourcePlugin
import sbtwhitesource.WhiteSourcePlugin.autoImport._

object ScalaVersions {
  val scala210 = "2.10.7"
  val scala212 = "2.12.8"
  val scala213 = "2.13.0-RC1"
}

object SbtVersions {
  val sbt013 = "0.13.18"
  val sbt10 = "1.2.8"
}

/**
 * Plugin that defines base settings for all Play projects
 */
object PlayBuildBase extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = SbtPgp && JvmPlugin

  /** Helper for operations on plugins. */
  private implicit class EnhancedPlugins(val plugins: Plugins) extends AnyVal {
    /** Disable the given plugin. */
    def &&!(plugin: AutoPlugin): Plugins = plugins && PluginsAccessor.exclude(plugin)
    /** Disable the given plugin, if it's defined. */
    def &&!(optPlugin: Option[AutoPlugin]): Plugins = optPlugin match {
      case None => plugins
      case Some(plugin) => this &&! plugin
    }
  }
  
  object autoImport {
    val playBuildExtraTests = taskKey[Unit]("Run extra tests during the release")
    val playBuildExtraPublish = taskKey[Unit]("Publish extract non aggregated projects during the release")
    val playBuildPromoteBintray = settingKey[Boolean]("Whether a Bintray promotion should be done on release")
    val playBuildPromoteSonatype = settingKey[Boolean]("Whether a Sonatype promotion should be done on release")
    val playCrossBuildRootProject = settingKey[Boolean]("Whether the root project should be cross built or not")
    val playCrossReleasePlugins = settingKey[Boolean]("Whether the sbt plugins should be cross released or not")
    val playBuildRepoName = settingKey[String]("The name of the repository in the playframework GitHub organization")

    // This is not using sbt-git because we need a more stable way to set
    // the current branch in a more stable way, for example, we may want to
    // get the current branch as "master" even if we are at a detached commit.
    //
    // This is useful when running tasks on Travis, where the builds runs in
    // a detached commit. See the discussion here:
    // https://github.com/travis-ci/travis-ci/issues/1701
    val playCurrentBranch = settingKey[String]("The current branch for the project")

    /**
     * Plugins configuration for a Play sbt plugin. Use this in preference to PlaySbtPluginBase, because this will
     * also disable the Sonatype plugin.
     */
    def PlaySbtPlugin: Plugins = PlaySbtPluginBase &&! Sonatype

    /**
     * Plugins configuration for a Play sbt library. Use this in preference to PlaySbtLibraryBase, because this will
     * also disable the Bintray plugin.
     */
    def PlaySbtLibrary: Plugins = PlaySbtLibraryBase &&! BintrayPlugin

    /**
     * Plugins configuration for a Play library. Use this in preference to PlayLibraryBase, because this will
     * also disable the Bintray plugin.
     */
    def PlayLibrary: Plugins = PlayLibraryBase &&! BintrayPlugin &&! PlaySbtCompat.optScriptedAutoPlugin

    /**
     * Plugins configuration for a Play Root Project that doesn't get published.
     */
    def PlayRootProject: Plugins = PlayRootProjectBase &&! PlaySbtCompat.optScriptedAutoPlugin

    /**
     * Plugins configuration for a Play project that doesn't get published.
     */
    def PlayNoPublish: Plugins = PlayNoPublishBase &&! BintrayPlugin &&! Sonatype

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

private object PlaySbtBuildBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBuildBase

  import PlayBuildBase.autoImport._

  private def choose[T](scalaBinVersion: String)(forScala210: T, forScala212: T) = CrossVersion.partialVersion(scalaBinVersion) match {
    case Some((2, 12)) => forScala212
    case _ => forScala210
  }

  override def projectSettings = Seq(
    crossScalaVersions := Seq(ScalaVersions.scala210, ScalaVersions.scala212),
    sbtVersion in pluginCrossBuild := choose(scalaBinaryVersion.value)(
      forScala210 = SbtVersions.sbt013,
      forScala212 = SbtVersions.sbt10
    ),
    javacOptions in compile ++= choose(scalaBinaryVersion.value)(
      forScala210 = Seq("-source", "1.6", "-target", "1.6"),
      forScala212 = Seq("-source", "1.8", "-target", "1.8")
    ),
    javacOptions in doc := choose(scalaBinaryVersion.value)(
      forScala210 = Seq("-source", "1.6"),
      forScala212 = Seq("-source", "1.8")
    )
  )
}

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
    playBuildPromoteSonatype in ThisBuild := true,
    omnidocGithubRepo := s"playframework/${(playBuildRepoName in ThisBuild).value}",
    omnidocTagPrefix := "",
    javacOptions in compile ++= Seq("-source", "1.8", "-target", "1.8"),
    javacOptions in doc := Seq("-source", "1.8"),
    crossScalaVersions := Seq(scalaVersion.value, ScalaVersions.scala213),
    scalaVersion := sys.props.get("scala.version").getOrElse(ScalaVersions.scala212),
    playCrossBuildRootProject in ThisBuild := true
  )
}

/**
 * Base Plugin for Play SBT libraries.
 *
 * - Publishes to sonatype
 */
object PlaySbtLibraryBase extends AutoPlugin {

  override def trigger = noTrigger
  override def requires = PlayBuildBase && PlaySbtBuildBase && PlaySonatypeBase

  import PlayBuildBase.autoImport._

  override def projectSettings = Seq(
    playBuildPromoteSonatype in ThisBuild := true
  )
}

/**
 * Base Plugin for releasing.
 *
 * Generally this should only be enabled for the root project.
 */
object PlayReleaseBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = PlayBuildBase && ReleasePlugin

  import PlayBuildBase.autoImport._

  override def projectSettings = Seq(
    playBuildExtraPublish := { () },
    playBuildExtraTests := { () },

    // Release settings
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseTagName := (version in ThisBuild).value,
    playCrossReleasePlugins := true,
    releaseCrossBuild := (playCrossBuildRootProject in ThisBuild).?.value.exists(identity),
    releaseProcess := {
      import ReleaseTransformations._

      def ifDefinedAndTrue(key: SettingKey[Boolean], step: State => State): State => State = { state =>
        Project.extract(state).getOpt(key in ThisBuild) match {
          case Some(true) => step(state)
          case _ => state
        }
      }

      Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runClean,

        if (playCrossReleasePlugins.value) releaseStepCommandAndRemaining("+test")
        else runTest,

        releaseStepTask(playBuildExtraTests in thisProjectRef.value),
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,

        if (playCrossReleasePlugins.value) releaseStepCommandAndRemaining("+publishSigned")
        else publishArtifacts,

        releaseStepTask(playBuildExtraPublish in thisProjectRef.value),
        ifDefinedAndTrue(playBuildPromoteBintray, releaseStepTask(bintrayRelease in thisProjectRef.value)),
        ifDefinedAndTrue(playBuildPromoteSonatype, releaseStepCommand("sonatypeRelease")),
        setNextVersion,
        commitNextVersion,
        pushChanges
      )
    }
  )

}

object PlayWhitesourcePlugin extends AutoPlugin {

  override def requires: Plugins = WhiteSourcePlugin && PlayBuildBase
  override def trigger: PluginTrigger = allRequirements

  import PlayBuildBase.autoImport._

  override lazy val projectSettings = Seq(
    whitesourceProduct := "Lightbend Reactive Platform",
    whitesourceAggregateProjectName := (moduleName in LocalRootProject).value + "-" + {
      if (isSnapshot.value) {
        // There are two scenarios then:
        // 1. It is the master branch
        // 2. It is a release branch (2.6.x, 2.5.x, etc)
        if (playCurrentBranch.value == "master") {
          "master"
        } else {
          // If it is not "master", then it is a release branch
          // that should also be handled as an snapshot report.
          CrossVersion.partialVersion((version in LocalRootProject).value) match {
            case Some((major, minor)) => s"$major.$minor-snapshot"
            case None => "snapshot"
          }
        }
      } else {
        // Here we have only the case where we are releasing a version.
        CrossVersion.partialVersion((version in LocalRootProject).value) match {
          case Some((major, minor)) => s"$major.$minor-stable"
          case None => "snapshot"
        }
      }
    }
  )
}

/**
 * Base Plugin for a root project that doesn't get published.
 *
 * - Contains release configuration
 */
object PlayRootProjectBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = PlayBuildBase && PlayBintrayBase && PlaySonatypeBase && PlayReleaseBase

  import PlayBuildBase.autoImport._

  override def projectSettings = PlayNoPublishBase.projectSettings ++ Seq(
    crossScalaVersions := {
      if ((playCrossBuildRootProject in ThisBuild).?.value.exists(identity)) {
        Seq(ScalaVersions.scala212, ScalaVersions.scala213)
      } else {
        Seq(ScalaVersions.scala210, ScalaVersions.scala212)
      }
    }
  )
}

object PlayNoPublishBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = PlayBuildBase

  override def projectSettings = Seq(
    PgpKeys.publishSigned := {},
    publish := {},
    publishLocal := {},
    publishTo := Some(Resolver.file("no-publish", crossTarget.value / "no-publish"))
  )
}

/**
 * Base plugin for all projects that publish to bintray
 */
object PlayBintrayBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = BintrayPlugin

  import PlayBuildBase.autoImport._

  override def projectSettings = Seq(
    bintrayOrganization := Some("playframework"),
    bintrayRepository := "sbt-plugin-releases",
    bintrayPackage := (playBuildRepoName in ThisBuild).value,
    bintrayReleaseOnPublish := false
  )
}

/**
 * Base plugin for all projects that publish to sonatype
 */
object PlaySonatypeBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = Sonatype

  override def projectSettings = Seq(
    sonatypeProfileName := "com.typesafe",
    publishTo := Some(sonatypeDefaultResolver.value)
  )
}
