package interplay

import sbt._
import sbt.Keys._

object Playdoc extends AutoPlugin {

  object Import {
    object PlaydocKeys {
      val playdocDirectory = settingKey[File]("Base directory of play documentation")
      val packagePlaydoc = taskKey[File]("Package play documentation")
    }
  }

  val autoImport = Import

  override def requires = sbt.plugins.JvmPlugin

  override def trigger = noTrigger

  import Import.PlaydocKeys._

  override def projectSettings =
    Defaults.packageTaskSettings(packagePlaydoc, mappings in packagePlaydoc) ++
    Seq(
      playdocDirectory := (baseDirectory in ThisBuild).value / "docs" / "manual",
      mappings in packagePlaydoc := {
        val base = playdocDirectory.value
        base.***.get pair relativeTo(base.getParentFile)
      },
      artifactClassifier in packagePlaydoc := Some("playdoc"),
      artifact in packagePlaydoc ~= { _.copy(configurations = Seq(Docs)) }
    ) ++
    addArtifact(artifact in packagePlaydoc, packagePlaydoc)

}
