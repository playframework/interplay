package interplay

import sbt.{AutoPlugin,ThisBuild }
import bintray.BintrayPlugin
import bintray.BintrayPlugin.autoImport._

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
