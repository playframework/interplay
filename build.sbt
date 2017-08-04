import buildinfo.BuildInfo._

lazy val interplay = (project in file("."))
  .enablePlugins(PlaySbtPlugin && PlayReleaseBase)

description := "Base build plugin for all Play modules"

addSbtPlugin("com.github.gseitz" %% "sbt-release" % sbtReleaseVersion)
addSbtPlugin("com.jsuereth" %% "sbt-pgp" % sbtPgpVersion)
addSbtPlugin("org.foundweekends" %% "sbt-bintray" % sbtBintrayVersion)
addSbtPlugin("org.xerial.sbt" %% "sbt-sonatype" % sbtSonatypeVersion)
addSbtPlugin("com.lightbend" %% "sbt-whitesource" % sbtWhitesourceVersion)

libraryDependencies ++= Seq(
  "org.scala-sbt" %% "scripted-plugin" % scriptedPluginVersion,
  "com.typesafe" % "config" % "1.3.1"
)

playBuildExtraTests := {
  scripted.toTask("").value
}

playBuildRepoName in ThisBuild := "interplay"

crossSbtVersions := Vector("0.13.16", "1.0.0-RC3")

scalaVersion := (CrossVersion partialVersion (sbtVersion in pluginCrossBuild).value match {
  case Some((0, 13)) => "2.10.6"
  case Some((1, _)) => "2.12.3"
  case _ => sys error s"Unhandled sbt version ${(sbtVersion in pluginCrossBuild).value}"
})