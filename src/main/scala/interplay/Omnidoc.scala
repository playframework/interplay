package interplay

import sbt._
import sbt.Keys._
import sbt.Package.ManifestAttributes

object Omnidoc extends AutoPlugin {

  object Import {
    object OmnidocKeys {
      val githubRepo = SettingKey[String]("omnidoc-github-repo", "Github repository for source URL")
      val snapshotBranch = SettingKey[String]("omnidoc-snapshot-branch", "Git branch for development versions")
      val tagPrefix = SettingKey[String]("omnidoc-tag-prefix", "Prefix before git tagged versions")
      val pathPrefix = SettingKey[String]("omnidoc-path-prefix", "Prefix before source directory paths")
      val sourceUrl = SettingKey[Option[String]]("omnidoc-source-url", "Source URL for scaladoc linking")
    }
  }

  val autoImport = Import

  val SourceUrlKey = "Omnidoc-Source-URL"

  override def requires = sbt.plugins.JvmPlugin

  override def trigger = noTrigger

  import Import.OmnidocKeys._

  override def projectSettings = Seq(
    sourceUrl := githubRepo.?.value map { repo =>
      val development = (snapshotBranch ?? "master").value
      val tagged = (tagPrefix ?? "v").value + version.value
      val tree = if (isSnapshot.value) development else tagged
      val prefix = "/" + (pathPrefix ?? "").value
      val directory = IO.relativize((baseDirectory in ThisBuild).value, baseDirectory.value)
      val path = directory.fold("")(prefix.+)
      s"https://github.com/${repo}/tree/${tree}${path}"
    },
    packageOptions in (Compile, packageSrc) ++= sourceUrl.value.toSeq map { url =>
      ManifestAttributes(SourceUrlKey -> url)
    }
  )

}
