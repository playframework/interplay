import _root_.interplay.ScalaVersions._
import buildinfo.BuildInfo._

lazy val interplay = (project in file("."))
  .enablePlugins(PlaySbtPlugin && PlayReleaseBase, SbtPlugin)

description := "Base build plugin for all Play modules"

crossScalaVersions -= scala210 // drop cross-build to sbt 0.13 (which uses Scala 2.10)

addSbtPlugin("com.github.gseitz" % "sbt-release" % sbtReleaseVersion)
addSbtPlugin("com.jsuereth" % "sbt-pgp" % sbtPgpVersion)
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % sbtSonatypeVersion)
addSbtPlugin("com.lightbend" % "sbt-whitesource" % sbtWhitesourceVersion)

libraryDependencies += "com.typesafe" % "config" % configVersion

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-Xlint",
  "-Ywarn-unused:imports",
  "-Xlint:nullary-unit",
  "-Ywarn-dead-code",
)

javacOptions ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-Xlint:deprecation",
  "-Xlint:unchecked",
)

playBuildExtraTests := {
  scripted.toTask("").value
}

// Supply the sbt.version to the scripted tests so
// that they can be run with either sbt 0.13 or
// sbt 1.
scriptedLaunchOpts += {
  val sbtV = (sbtVersion in pluginCrossBuild).value
  s"-Dsbt.version=$sbtV"
}

playBuildRepoName in ThisBuild := "interplay"

enablePlugins(SbtPlugin)

// Used by CI to check that interplay is working. Note
// that the scripted tests are cross-built; i.e. they are
// run for both sbt 0.13 and sbt 1.
addCommandAlias("validate", ";clean;+test;+scripted;+publishLocal")
