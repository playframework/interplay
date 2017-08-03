import buildinfo.BuildInfo._

lazy val interplay = (project in file("."))
  .enablePlugins(PlaySbtPlugin && PlayReleaseBase)

description := "Base build plugin for all Play modules"

val currentSbtVersion = sbtBinaryVersion in pluginCrossBuild

// FIXME: https://github.com/sbt/sbt/issues/3393
//addSbtPlugin("com.github.gseitz" %% "sbt-release" % sbtReleaseVersion)
//addSbtPlugin("com.jsuereth" %% "sbt-pgp" % sbtPgpVersion)
//addSbtPlugin("org.foundweekends" %% "sbt-bintray" % sbtBintrayVersion)
//addSbtPlugin("org.xerial.sbt" %% "sbt-sonatype" % sbtSonatypeVersion)
//addSbtPlugin("com.lightbend" %% "sbt-whitesource" % sbtWhitesourceVersion)
libraryDependencies ++= Seq(
  Defaults.sbtPluginExtra("com.github.gseitz" % "sbt-release" % sbtReleaseVersion, currentSbtVersion.value, scalaBinaryVersion.value),
  Defaults.sbtPluginExtra("com.jsuereth" % "sbt-pgp" % sbtPgpVersion, currentSbtVersion.value, scalaBinaryVersion.value),
  Defaults.sbtPluginExtra("org.foundweekends" % "sbt-bintray" % sbtBintrayVersion, currentSbtVersion.value, scalaBinaryVersion.value),
  Defaults.sbtPluginExtra("org.xerial.sbt" % "sbt-sonatype" % sbtSonatypeVersion, currentSbtVersion.value, scalaBinaryVersion.value),
  Defaults.sbtPluginExtra("com.lightbend" % "sbt-whitesource" % sbtWhitesourceVersion, currentSbtVersion.value, scalaBinaryVersion.value)
)

libraryDependencies ++= Seq(
  "org.scala-sbt" % "scripted-plugin" % scriptedPluginVersion,
  "com.typesafe" % "config" % configVersion
)

playBuildExtraTests := {
  scripted.toTask("").value
}

playBuildRepoName in ThisBuild := "interplay"
