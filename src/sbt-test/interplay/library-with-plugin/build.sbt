import interplay.ScalaVersions._

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
(ThisBuild / dynverVTagPrefix) := false

lazy val common: Seq[Setting[_]] = Seq(
  PgpKeys.publishSigned := {
    IO.write(crossTarget.value / "publish-version", s"${publishTo.value.get.name}:${version.value}")
  },
  publish := { throw sys.error("Publish should not have been invoked") },
  credentials := Seq(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", "sbt", "notcorrectpassword"))
)

// What an actual project would look like
lazy val `mock-root` = (project in file("."))
  .settings(
    common,
    crossScalaVersions := Seq(scala212, scala213, scala3)
  )
  .enablePlugins(PlayRootProject)
  .aggregate(`mock-library`, `mock-sbt-plugin`) // has a sbt plugin that will be built together with root project

lazy val `mock-sbt-plugin` = (project in file("mock-sbt-plugin"))
  .enablePlugins(PlaySbtPlugin)
  .dependsOn(`mock-sbt-library`)
  .settings(
    common,
  )

lazy val `mock-sbt-library` = (project in file("mock-sbt-library"))
  .enablePlugins(PlaySbtLibrary)
  .settings(common)

lazy val `mock-library` = (project in file("mock-library"))
  .enablePlugins(PlayLibrary)
  .settings(common)

ThisBuild / playBuildRepoName := "mock"

// Below this line is for facilitating tests

InputKey[Unit]("contains") := {
  val args = Def.spaceDelimited().parsed
  val filename = args.head.replace("target/SCALA3/", s"target/scala-${crossScalaVersions.value.find(_.startsWith("3.")).getOrElse("")}/")
  val expected = args.tail.mkString(" ")
  val contents: String = IO.read(file(filename))
  if (contents.contains(expected)) {
    println(s"Checked that $filename contains $expected")
  } else {
    throw sys.error(s"File $filename does not contain '$expected':\n$contents")
  }
}

ThisBuild / commands := {
  Seq("sonatypeRelease", "sonatypeBundleRelease").map { name =>
    Command.command(name) { state =>
      val extracted = Project.extract(state)
      IO.write(extracted.get(target) / "sonatype-release-version", extracted.get(version))
      state
    }
  } ++ (ThisBuild / commands).value
}
