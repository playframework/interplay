package interplay

import sbt.{Def, _}
import sbt.Keys._
import sbt.io.Path._

object Playdoc extends AutoPlugin {

  object autoImport {
    val playdocDirectory: SettingKey[File] = settingKey[File]("Base directory of play documentation")
    val playdocPackage: TaskKey[File] = taskKey[File]("Package play documentation")
  }

  import autoImport._
  
  override def requires = sbt.plugins.JvmPlugin

  override def trigger: PluginTrigger = noTrigger

  override def projectSettings: Seq[Def.Setting[_]] =
    Defaults.packageTaskSettings(playdocPackage, mappings in playdocPackage) ++
    Seq(
      playdocDirectory := (baseDirectory in ThisBuild).value / "docs" / "manual",
      mappings in playdocPackage := {
        val base: sbt.File = playdocDirectory.value
        base.allPaths.get pair relativeTo(base.getParentFile)
      },
      artifactClassifier in playdocPackage := Some("playdoc")
      /*,
      artifact in playdocPackage ~= { _.withConfigurations(Seq(Docs)) }
      */
    ) ++
    addArtifact(artifact in playdocPackage, playdocPackage)

}
