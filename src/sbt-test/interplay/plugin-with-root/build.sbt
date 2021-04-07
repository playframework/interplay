// What an actual project would look like
lazy val `mock-root` = (project in file("."))
  .enablePlugins(PlayRootProject)
  .aggregate(`mock-sbt-plugin`)
  .settings(common)

lazy val `mock-sbt-plugin` = (project in file("mock-sbt-plugin"))
  .enablePlugins(PlaySbtPlugin)
  .settings(common)
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
  val filename = args.head
  val expected = args.tail.mkString(" ")
  val contents: String = IO.read(file(filename))
  if (contents.contains(expected)) {
    println(s"Checked that $filename contains $expected")
  } else {
    throw sys.error(s"File $filename does not contain '$expected':\n$contents")
  }
}

def common: Seq[Setting[_]] = Seq(
  PgpKeys.publishSigned := {
    IO.write(crossTarget.value / "publish-version", s"${publishTo.value.get.name}:${version.value}")
  },
  publish := { throw sys.error("Publish should not have been invoked") },
)

commands in ThisBuild := {
  Seq("sonatypeBundleRelease").map { name =>
    Command.command(name) { state =>
      val extracted = Project.extract(state)
      IO.write(extracted.get(target) / "sonatype-release-version", extracted.get(version))
      state
    }
  } ++ (commands in ThisBuild).value
}
