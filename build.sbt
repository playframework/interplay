import buildinfo.BuildInfo._

lazy val interplay = (project in file("."))
  .enablePlugins(PlaySbtPlugin && PlayReleaseBase)

description := "Base build plugin for all Play modules"

sbtPlugin := true

sbtVersion in Global := "0.13.16"

crossSbtVersions := List("0.13.16", "1.0.0")

scalaVersion := (CrossVersion partialVersion (sbtVersion in pluginCrossBuild).value match {
  case Some((0, 13)) => "2.10.6"
  case Some((1, _)) => "2.12.3"
  case _ => sys error s"Cannot set scalaVersion: Unhandled sbt version ${(sbtVersion in pluginCrossBuild).value}"
})

crossScalaVersions := (CrossVersion partialVersion (sbtVersion in pluginCrossBuild).value match {
  case Some((0, 13)) => Seq("2.10.6")
  case Some((1, _)) => Seq("2.12.3")
  case _ => sys error s"Cannot set crossScalaVersions: Unhandled sbt version ${(sbtVersion in pluginCrossBuild).value}"
})

playBuildExtraTests := {
  scripted.toTask("").value
}

playBuildRepoName in ThisBuild := "interplay"

libraryDependencies ++= Seq(
  Defaults.sbtPluginExtra("com.github.gseitz" % "sbt-release" % sbtReleaseVersion, (sbtBinaryVersion in pluginCrossBuild).value, scalaBinaryVersion.value),
  Defaults.sbtPluginExtra("com.jsuereth" % "sbt-pgp" % sbtPgpVersion, (sbtBinaryVersion in pluginCrossBuild).value, scalaBinaryVersion.value),
  Defaults.sbtPluginExtra("org.foundweekends" % "sbt-bintray" % sbtBintrayVersion, (sbtBinaryVersion in pluginCrossBuild).value, scalaBinaryVersion.value),
  Defaults.sbtPluginExtra("org.xerial.sbt" % "sbt-sonatype" % sbtSonatypeVersion, (sbtBinaryVersion in pluginCrossBuild).value, scalaBinaryVersion.value),
  Defaults.sbtPluginExtra("com.lightbend" % "sbt-whitesource" % sbtWhitesourceVersion, (sbtBinaryVersion in pluginCrossBuild).value, scalaBinaryVersion.value)
)

libraryDependencies ++= (CrossVersion partialVersion scriptedSbt.value match {
  case Some((0, 13)) => Seq("org.scala-sbt" % "scripted-plugin" % scriptedSbt.value)
  case Some((1, _))  => Seq("org.scala-sbt" %% "scripted-plugin" % scriptedSbt.value)
  case _ => Nil
})

scriptedLaunchOpts += s"-Dscala.version=${scalaVersion.value}"
