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
  .settings(common: _*)
  .settings(
    playCrossBuildRootProject in ThisBuild := true // activates cross build for Scala 2.11 and 2.12
  )
  .enablePlugins(PlayRootProject)
  .aggregate(`mock-library`, `mock-sbt-plugin`) // has a sbt plugin that will be built together with root project

lazy val `mock-sbt-plugin` = (project in file("mock-sbt-plugin"))
  .enablePlugins(PlaySbtPlugin, SbtPlugin)
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

commands in ThisBuild := {
  Seq("sonatypeRelease", "sonatypeBundleRelease").map { name =>
    Command.command(name) { state =>
      val extracted = Project.extract(state)
      IO.write(extracted.get(target) / "sonatype-release-version", extracted.get(version))
      state
    }
  } ++ (commands in ThisBuild).value
}
