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
  val scala210 = "2.10.6"
  val scala211 = "2.11.11"
  val scala212 = "2.12.3"
}

object SbtVersions {
  val sbt013 = "0.13.16"
  val sbt10 = "1.0.1"
}

/**
 * Plugin that defines base settings for all Play projects
 */
object PlayBuildBase extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = SbtPgp && JvmPlugin

  object autoImport {
    val playBuildExtraTests = taskKey[Unit]("Run extra tests during the release")
    val playBuildExtraPublish = taskKey[Unit]("Publish extract non aggregated projects during the release")
    val playBuildPromoteBintray = settingKey[Boolean]("Whether a Bintray promotion should be done on release")
    val playBuildPromoteSonatype = settingKey[Boolean]("Whether a Sonatype promotion should be done on release")
    val playCrossBuildRootProject = settingKey[Boolean]("Whether the root project should be cross built or not")
    val playBuildRepoName = settingKey[String]("The name of the repository in the playframework GitHub organization")

    /**
     * Plugins configuration for a Play sbt plugin. Use this in preference to PlaySbtPluginBase, because this will
     * also disable the Sonatype plugin.
     */
    def PlaySbtPlugin: Plugins = PlaySbtPluginBase && PluginsAccessor.exclude(Sonatype)

    /**
     * Plugins configuration for a Play sbt library. Use this in preference to PlaySbtLibraryBase, because this will
     * also disable the Bintray plugin.
     */
    def PlaySbtLibrary: Plugins = PlaySbtLibraryBase && PluginsAccessor.exclude(BintrayPlugin)

    /**
     * Plugins configuration for a Play library. Use this in preference to PlayLibraryBase, because this will
     * also disable the Bintray plugin.
     */
    def PlayLibrary: Plugins = PlayLibraryBase && PluginsAccessor.exclude(BintrayPlugin)

    /**
     * Plugins configuration for a Play Root Project that doesn't get published.
     */
    def PlayRootProject: Plugins = PlayRootProjectBase

    /**
     * Plugins configuration for a Play project that doesn't get published.
     */
    def PlayNoPublish: Plugins = PlayNoPublishBase && PluginsAccessor.exclude(BintrayPlugin) && PluginsAccessor.exclude(Sonatype)

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

  override def projectSettings = ScriptedPlugin.scriptedSettings ++ Seq(
    ScriptedPlugin.scriptedLaunchOpts += (version apply { v => s"-Dproject.version=$v" }).value,
    sbtPlugin := true,
    publishTo := {
      if (isSnapshot.value) {
        Some(Opts.resolver.sonatypeSnapshots)
      } else publishTo.value
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
    crossScalaVersions := Seq(ScalaVersions.scala211, scalaVersion.value),
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
    playBuildExtraPublish := (),
    playBuildExtraTests := (),

    // Release settings
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseTagName := (version in ThisBuild).value,
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
        runTest,
        releaseStepTask(playBuildExtraTests in thisProjectRef.value),
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        publishArtifacts,
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

  override def requires: Plugins = WhiteSourcePlugin

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings = Seq(
    whitesourceProduct := "Lightbend Reactive Platform"
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
        Seq(ScalaVersions.scala211, ScalaVersions.scala212)
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
    sonatypeProfileName := "com.typesafe"
  )
}
