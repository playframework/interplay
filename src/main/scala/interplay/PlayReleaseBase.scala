package interplay

import sbt.{AutoPlugin, SettingKey, State, Project, ThisBuild}
import sbt.Keys.{version, thisProjectRef}
import bintray.BintrayPlugin.autoImport.bintrayRelease
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport._
import com.jsuereth.sbtpgp.PgpKeys

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
        releaseStepCommandAndRemaining("+test"),

        releaseStepTask(playBuildExtraTests in thisProjectRef.value),
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,

        releaseStepCommandAndRemaining("+publishSigned"),

        releaseStepTask(playBuildExtraPublish in thisProjectRef.value),
        ifDefinedAndTrue(playBuildPromoteBintray, releaseStepTask(bintrayRelease in thisProjectRef.value)),
        ifDefinedAndTrue(playBuildPromoteSonatype, releaseStepCommand("sonatypeBundleRelease")),
        setNextVersion,
        commitNextVersion,
        pushChanges
      )
    }
  )

}
