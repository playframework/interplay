import buildinfo.BuildInfo._

lazy val interplay = (project in file("."))
  .enablePlugins(PlaySbtPlugin && PlayReleaseBase)

description := "Base build plugin for all Play modules"

addSbtPlugin("com.github.gseitz" % "sbt-release" % sbtReleaseVersion)
addSbtPlugin("com.jsuereth" % "sbt-pgp" % sbtPgpVersion)
addSbtPlugin("org.foundweekends" % "sbt-bintray" % sbtBintrayVersion)
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % sbtSonatypeVersion)
addSbtPlugin("com.lightbend" % "sbt-whitesource" % sbtWhitesourceVersion)

libraryDependencies += "com.typesafe" % "config" % configVersion

scalacOptions ++= {
    if (scalaVersion.value.startsWith("2.10")) {
    Seq("-target:jvm-1.8")
  } else {
    Seq(
      "-target:jvm-1.8",
      "-Xlint",
      "-Ywarn-unused:imports",
      "-Xlint:nullary-unit",
      "-Ywarn-dead-code",
    )
  }
}

javacOptions ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-Xlint:deprecation",
  "-Xlint:unchecked",
)

// The location of the scripted-plugin changed from sbt 1
// onwards. The following conditional allows interplay to
// be built as both an sbt 0.13 and sbt 1 plugin.
libraryDependencies += {
  val sbtVer = (sbtVersion in pluginCrossBuild).value
  CrossVersion.partialVersion(sbtVer) match {
    case Some((0, _)) =>
      // sbt 0.x plugins weren't cross-built
      "org.scala-sbt" % "scripted-plugin" % sbtVer
    case Some((major, _)) if major >= 1 =>
      // sbt 1+ plugins are cross-built
      "org.scala-sbt" %% "scripted-plugin" % sbtVer
    case unknown =>
      sys.error(s"Can't work out scripted plugin for sbt version: $sbtVer (partial version: $unknown)")
  }
}

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
