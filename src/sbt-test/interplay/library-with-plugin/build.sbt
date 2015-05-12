// What an actual project would look like
lazy val `mock-root` = (project in file("."))
  .enablePlugins(PlayRootProject)
  .aggregate(`mock-library`)
  .settings(common: _*)

lazy val `mock-sbt-plugin` = (project in file("mock-sbt-plugin"))
  .enablePlugins(PlaySbtPlugin)
  .dependsOn(`mock-sbt-library`)
  .settings(common: _*)
  .settings(
    // Pass the file for the scripted test to write to so that we can check that it ran
    scriptedLaunchOpts += s"-Dscripted-file=${target.value / "scripted-ran"}"
  )

lazy val `mock-sbt-library` = (project in file("mock-sbt-library"))
  .enablePlugins(PlaySbtLibrary)
  .settings(common: _*)

lazy val `mock-library` = (project in file("mock-library"))
  .enablePlugins(PlayLibrary)
  .settings(common: _*)

playBuildExtraTests := {
  (scripted in `mock-sbt-plugin`).toTask("").value
  val _ = (test in (`mock-sbt-library`, Test)).value
}

playBuildExtraPublish := {
  val p = (PgpKeys.publishSigned in `mock-sbt-plugin`).value
  val l = (PgpKeys.publishSigned in `mock-sbt-library`).value
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
  PgpKeys.publishSigned := {
    IO.write(crossTarget.value / "publish-version", s"${publishTo.value.get.name}:${version.value}")
  },
  publish := { throw sys.error("Publish should not have been invoked") },
  bintrayRelease := IO.write(target.value / "bintray-release-version", version.value),
  bintrayCredentialsFile := (baseDirectory in ThisBuild).value / "bintray.credentials",
  credentials := Seq(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", "sbt", "notcorrectpassword"))
)

commands in ThisBuild := {
  Command.command("sonatypeRelease") { state =>
    val extracted = Project.extract(state)
    IO.write(extracted.get(target) / "sonatype-release-version", extracted.get(version))
    state
  } +: (commands in ThisBuild).value
}


