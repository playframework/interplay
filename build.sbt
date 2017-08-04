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
