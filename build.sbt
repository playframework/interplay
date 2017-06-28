import buildinfo.BuildInfo._

lazy val interplay = (project in file("."))
  .enablePlugins(PlaySbtPlugin && PlayReleaseBase)

description := "Base build plugin for all Play modules"

addSbtPlugin("com.github.gseitz" % "sbt-release" % sbtReleaseVersion)
addSbtPlugin("com.jsuereth" % "sbt-pgp" % sbtPgpVersion)
addSbtPlugin("me.lessis" % "bintray-sbt" % bintraySbtVersion)
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % sbtSonatypeVersion)
addSbtPlugin("com.lightbend" % "sbt-whitesource" % sbtWhitesourceVersion)

libraryDependencies ++= Seq(
  "org.scala-sbt" % "scripted-plugin" % scriptedPluginVersion,
  "com.typesafe" % "config" % configVersion
)

playBuildExtraTests := {
  scripted.toTask("").value
}

playBuildRepoName in ThisBuild := "interplay"
