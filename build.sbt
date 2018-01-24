import buildinfo.BuildInfo._

lazy val interplay = (project in file("."))
  .enablePlugins(PlaySbtPlugin && PlayReleaseBase)
  .settings(playCrossReleasePlugins := false)

description := "Base build plugin for all Play modules"

addSbtPlugin("com.github.gseitz" % "sbt-release" % sbtReleaseVersion)
addSbtPlugin("com.jsuereth" % "sbt-pgp" % sbtPgpVersion)
addSbtPlugin("org.foundweekends" % "sbt-bintray" % sbtBintrayVersion)
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % sbtSonatypeVersion)
addSbtPlugin("com.lightbend" % "sbt-whitesource" % sbtWhitesourceVersion)
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % sbtGitVersion)

libraryDependencies ++= Seq(
  "org.scala-sbt" % "scripted-plugin" % scriptedPluginVersion,
  "com.typesafe" % "config" % configVersion
)

playBuildExtraTests := {
  scripted.toTask("").value
}

playBuildRepoName in ThisBuild := "interplay"

sbtPlugin := true

sbtVersion := "0.13.16"

crossSbtVersions := Seq("0.13.16")

addCommandAlias("validate", ";clean;test;scripted")