lazy val ScalaVersion = sys.props.get("scala.version").getOrElse("2.12.3")

def common: Seq[Setting[_]] = Seq(
  PgpKeys.publishSigned := {
    IO.write(crossTarget.value / "publish-version", s"${publishTo.value.get.name}:${version.value}")
  },
  publish := { throw sys.error("Publish should not have been invoked") },
  credentials := Seq(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", "sbt", "notcorrectpassword")),
  scalaVersion := ScalaVersion,
  crossScalaVersions := Seq(ScalaVersion)
)

// What an actual project would look like
lazy val `mock-root` = (project in file("."))
  .enablePlugins(PlayRootProject)
  .aggregate(`mock-library`)
  .settings(common: _*)

lazy val `mock-library` = (project in file("mock-library"))
  .enablePlugins(PlayLibrary)
  .settings(common: _*)

playBuildRepoName in ThisBuild := "mock"

// This task can receive a list of files to check its content.
// That way, a it can be used when cross building sbt plugins
// since we can pass generate files for sbt 0.13 or 1.0 (and
// also Scala 2.10 or 2.12).
InputKey[Unit]("someContains") := {
  val args = Def.spaceDelimited().parsed
  val files = args.init
  val expected = args.last

  val expectedContentIsPresent = files.exists { f =>
    val _file = file(f)
    if (_file.exists()) {
      val contents = IO.read(_file)
      println(
        s"""
           |[debug]: Checking if ${_file} contains $expected:
           |[debug]: File Content is: $contents
           """.stripMargin)

      contents.contains(expected)
    } else {
      println(s"[debug]: File ${_file} does not exists")
      false
    }
  }

  if (!expectedContentIsPresent) {
    throw sys.error(s"""None of files ${files.mkString("")} contains '$expected'""")
  }
}

commands in ThisBuild := {
  Command.command("sonatypeRelease") { state =>
    val extracted = Project.extract(state)
    IO.write(extracted.get(target) / "sonatype-release-version", extracted.get(version))
    state
  } +: (commands in ThisBuild).value
}

publishTo in ThisBuild := Some(Opts.resolver.sonatypeSnapshots)