package interplay

import sbt.{AutoPlugin, LocalRootProject, PluginTrigger, CrossVersion, Plugins}
import sbt.Keys.{version, isSnapshot, moduleName}
import sbtwhitesource.WhiteSourcePlugin
import sbtwhitesource.WhiteSourcePlugin.autoImport.whitesourceProduct
import sbtwhitesource.WhiteSourcePlugin.autoImport.whitesourceAggregateProjectName

/**
 * 
 */
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
