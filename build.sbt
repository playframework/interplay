import _root_.interplay.ScalaVersions._
import buildinfo.BuildInfo._

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
ThisBuild / dynverVTagPrefix := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

lazy val interplay = (project in file("."))
  .enablePlugins(PlaySbtPlugin)

description := "Base build plugin for all Play modules"

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % sbtCiReleaseVersion)

libraryDependencies += "com.typesafe" % "config" % configVersion

scalacOptions ++= Seq(
  "-release:11",
  "-Xlint",
  "-Ywarn-unused:imports",
  "-Xlint:nullary-unit",
  "-Ywarn-dead-code",
)

javacOptions ++= Seq(
  "--release", "11",
  "-Xlint:deprecation",
  "-Xlint:unchecked",
)

// Supply the sbt.version to the scripted tests so
// that they can be run with either sbt 0.13 or
// sbt 1.
scriptedLaunchOpts += {
  val sbtV = (pluginCrossBuild / sbtVersion).value
  s"-Dsbt.version=$sbtV"
}

ThisBuild / playBuildRepoName := "interplay"

enablePlugins(SbtPlugin)
