// What an actual project would look like
lazy val `mock-sbt-plugin` = (project in file("."))
  .enablePlugins(PlaySbtPlugin)
  .settings(
    common,
  )


// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
(ThisBuild / dynverVTagPrefix) := false

ThisBuild / playBuildRepoName := "mock"

// Below this line is for facilitating tests
InputKey[Unit]("contains") := {
  val args = Def.spaceDelimited().parsed
  val filename = args.head.replace("target/SCALA3/", s"target/scala-${crossScalaVersions.value.find(_.startsWith("3.")).getOrElse("")}/")
  val contents = IO.read(file(filename))
  val expected = args.tail.mkString(" ")
  if (!contents.contains(expected)) {
    throw sys.error(s"File ${filename} does not contain '$expected':\n$contents")
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
