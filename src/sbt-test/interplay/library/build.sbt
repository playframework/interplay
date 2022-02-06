lazy val `mock-library` = (project in file("."))
  .enablePlugins(PlayLibrary && PlayReleaseBase)
  .settings(common: _*)

ThisBuild / playBuildRepoName := "mock"

// Below this line is for facilitating tests
InputKey[Unit]("contains") := {
  val args = Def.spaceDelimited().parsed
  val contents = IO.read(file(args.head))
  val expected = args.tail.mkString(" ")
  if (!contents.contains(expected)) {
    throw sys.error(s"File ${args.head} does not contain '$expected':\n$contents")
  }
}

TaskKey[Unit]("verifyOmnidocSourceUrl") := {
  import java.util.jar.JarFile

  val expected = "https://github.com/playframework/mock/tree/main"

  val sourceUrl = omnidocSourceUrl.value
  sourceUrl match {
    case Some(`expected`) => ()
    case other => throw sys.error(s"Expected $expected source url, got $other")
  }

  val srcZip = (Compile / packageSrc).value
  val jarFile = new JarFile(srcZip)
  val manifest = jarFile.getManifest.getMainAttributes

  manifest.getValue(Omnidoc.SourceUrlKey) match {
    case `expected` => ()
    case other => throw sys.error(s"Expected $expected source url, got $other")
  }
  jarFile.close()
}

def common: Seq[Setting[_]] = Seq(
  PgpKeys.publishSigned := {
    IO.write(crossTarget.value / "publish-version", s"${publishTo.value.get.name}:${version.value}")
  },
  publish := { throw sys.error("Publish should not have been invoked") },
  credentials := Seq(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", "sbt", "notcorrectpassword"))
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
