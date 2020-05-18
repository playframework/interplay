package interplay

import bintray.BintrayPlugin
import bintray.BintrayPlugin.autoImport._
import sbt.Keys.isSnapshot
import sbt.{AutoPlugin, ThisBuild}

/**
 * Base plugin for all projects that publish to bintray
 */
object PlayLibraryBintrayBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = BintrayPlugin

  import PlayBuildBase.autoImport._

  override def projectSettings = Seq(
    playBuildPromoteBintray in ThisBuild := true,
    bintrayOrganization := Some("playframework"),
    bintrayRepository := (if (isSnapshot.value) "snapshots" else "maven"),
    bintrayPackage := (playBuildRepoName in ThisBuild).value,
    bintrayReleaseOnPublish := false
  )
}
