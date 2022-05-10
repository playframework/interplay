
// What an actual project would look like
lazy val `mock-sbt-plugin` = (project in file("."))
  .enablePlugins(PlaySbtPlugin)
  .settings(
    common,
  )

ThisBuild / playBuildRepoName := "mock-without-cross-release"

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
(ThisBuild / dynverVTagPrefix) := false

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

ThisBuild / commands := {
  Seq("sonatypeRelease", "sonatypeBundleRelease").map { name =>
    Command.command(name) { state =>
      val extracted = Project.extract(state)
      IO.write(extracted.get(target) / "sonatype-release-version", extracted.get(version))
      state
    }
  } ++ (ThisBuild / commands).value
}
