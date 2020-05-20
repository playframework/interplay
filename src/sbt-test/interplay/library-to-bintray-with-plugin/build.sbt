lazy val common: Seq[Setting[_]] = Seq(
  PgpKeys.publishSigned := {
    IO.write(crossTarget.value / "publish-version", s"${publishTo.value.get.name}:${version.value}")
  },
  publish := { throw sys.error("Publish should not have been invoked") },
  bintrayRelease := IO.write(target.value / "bintray-release-version", version.value),
  bintrayCredentialsFile := (baseDirectory in ThisBuild).value / "bintray.credentials",
  credentials := Seq(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", "sbt", "notcorrectpassword"))
)

// What an actual project would look like
lazy val `mock-root` = (project in file("."))
  .settings(
    common,
    playCrossBuildRootProject in ThisBuild := true // activates cross build for Scala 2.12 and 2.13
  )
  .enablePlugins(PlayRootProject)
  .aggregate(`mock-library`, `mock-sbt-plugin`) // has a sbt plugin that will be built together with root project

lazy val `mock-sbt-plugin` = (project in file("mock-sbt-plugin"))
  .enablePlugins(PlaySbtPlugin)
  .dependsOn(`mock-sbt-library`)
  .settings(
    common,
    // Pass the file for the scripted test to write to so that we can check that it ran
    scriptedLaunchOpts += s"-Dscripted-file=${target.value / "scripted-ran"}"
  )

lazy val `mock-sbt-library` = (project in file("mock-sbt-library"))
  .enablePlugins(PlaySbtLibrary)
  .settings(common)

lazy val `mock-library` = (project in file("mock-library"))
  .enablePlugins(PlayLibraryToBintray)
  .settings(common)

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
  val filename = args.head
  val expected = args.tail.mkString(" ")
  val contents: String = IO.read(file(filename))
  if (contents.contains(expected)) {
    println(s"Checked that $filename contains $expected")
  } else {
    throw sys.error(s"File $filename does not contain '$expected':\n$contents")
  }
}

commands in ThisBuild := {
  Seq("sonatypeRelease", "sonatypeBundleRelease").map { name =>
    Command.command(name) { state =>
      val extracted = Project.extract(state)
      IO.write(extracted.get(target) / "sonatype-release-version", extracted.get(version))
      state
    }
  } ++ Seq("bintrayRelease").map { name =>
    Command.command(name) { state =>
      val extracted = Project.extract(state)
      IO.write(extracted.get(target) / "bintray-release-version", extracted.get(version))
      state
    }
  } ++ (commands in ThisBuild).value
}
