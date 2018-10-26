import interplay.PlaySbtCompat

// What an actual project would look like
lazy val `mock-sbt-plugin` = (project in file("."))
  .enablePlugins(PlaySbtPlugin && PlayReleaseBase)
  .settings(common: _*)
  .settings(
    // Pass the file for the scripted test to write to so that we can check that it ran
    scriptedLaunchOpts += s"-Dscripted-file=${target.value / "scripted-ran"}"
  )

playBuildExtraTests := {
  (scripted in `mock-sbt-plugin`).toTask("").value
}

playBuildRepoName in ThisBuild := "mock"

// Below this line is for facilitating tests
InputKey[Unit]("contains") := {
  val args = Def.spaceDelimited().parsed
  val contents = IO.read(file(args.head))
  val expected = args.tail.mkString(" ")
  if (!contents.contains(expected)) {
    throw sys.error(s"File ${args.head} does not contain '$expected':\n$contents")
  }
}

def common: Seq[Setting[_]] = Seq(
  PlaySbtCompat.scriptedTask := PlaySbtCompat.scriptedTask.evaluated,
  PgpKeys.publishSigned := {
    IO.write(crossTarget.value / "publish-version", s"${publishTo.value.get.name}:${version.value}")
  },
  publish := { throw sys.error("Publish should not have been invoked") },
  bintrayRelease := IO.write(target.value / "bintray-release-version", version.value),
  bintrayCredentialsFile := (baseDirectory in ThisBuild).value / "bintray.credentials"
)

