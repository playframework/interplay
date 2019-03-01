
// What an actual project would look like
lazy val `mock-sbt-plugin` = (project in file("."))
  .enablePlugins(PlaySbtPlugin && PlayReleaseBase, SbtPlugin)
  .settings(common: _*)
  .settings(
    // Pass the file for the scripted test to write to so that we can check that it ran
    scriptedLaunchOpts += s"-Dscripted-file=${target.value / "scripted-ran"}"
  )

playBuildExtraTests := {
  (scripted in `mock-sbt-plugin`).toTask("").value
}

playBuildRepoName in ThisBuild := "mock-without-cross-release"

playCrossReleasePlugins := false

// Below this line is for facilitating tests

InputKey[Unit]("contains") := {
  val args = Def.spaceDelimited().parsed
  val filename = substVersions(sbtVersion.value, args.head)
  val expected = args.tail.mkString(" ")
  val contents: String = IO.read(file(filename))
  if (contents.contains(expected)) {
    println(s"Checked that $filename contains $expected")
  } else {
    throw sys.error(s"File $filename does not contain '$expected':\n$contents")
  }
}

InputKey[Unit]("existsCustom") := {
  val args = Def.spaceDelimited().parsed
  val filename = substVersions(sbtVersion.value, args.head)
  assert(args.tail.isEmpty)
  val exists: Boolean = file(filename).exists()
  if (exists) {
    println(s"Checked that $filename exists")
  } else {
    throw sys.error(s"File $filename does not exist")
  }
}

def substVersions(sbtVersion: String, str: String): String = {
  val substitutions: Seq[(String, String)] = if (sbtVersion.startsWith("0.13.")) {
    // Note: run 'OTHER_' substitutions first otherwise they won't work
    Vector(
      "OTHER_SBT_API" -> "1.0",
      "OTHER_SCALA_API" -> "2.12",
      "SBT_API" -> "0.13",
      "SCALA_API" -> "2.10"
    )
  } else {
    Vector(
      "OTHER_SBT_API" -> "0.13",
      "OTHER_SCALA_API" -> "2.10",
      "SBT_API" -> "1.0",
      "SCALA_API" -> "2.12"
    )
  }
  substitutions.foldLeft(str) {
    case (currStr, (target, replacement)) => currStr.replace(target, replacement)
  }
}

def common: Seq[Setting[_]] = Seq(
  PgpKeys.publishSigned := {
    IO.write(crossTarget.value / "publish-version", s"${publishTo.value.get.name}:${version.value}")
  },
  publish := { throw sys.error("Publish should not have been invoked") },
  bintrayRelease := IO.write(target.value / "bintray-release-version", version.value),
  bintrayCredentialsFile := (baseDirectory in ThisBuild).value / "bintray.credentials"
)

