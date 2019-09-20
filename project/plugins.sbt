import java.util.Locale

libraryDependencies ++= Seq(
  "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value,
  "com.typesafe" % "config" % "1.3.4"
)

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.5")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")
addSbtPlugin("com.lightbend" % "sbt-whitesource" % "0.1.16")

lazy val build = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := libraryDependencies.value.map { module =>
      val key = "-([a-z])".r.replaceAllIn(module.name, matched => matched.group(1).toUpperCase(Locale.ENGLISH)) + "Version"
      (key -> module.revision): BuildInfoKey
    }
  )

// The interplay "meta-build"—the stuff inside the `project`
// directory—uses interplay as a plugin. Since we haven't run the
// interplay "proper build"—the stuff in the root directory—yet we
// can't just say "addSbtPlugin(..."interplay"...) we need to actually
// compile the plugin sources as part of the meta-build.
//
// We do this by adding some of the interplay source directories to
// the meta-build. This means that interplay will be compiled once as
// part of the meta-build (but not published) and then *again* as part
// of the proper build (where it is properly cross-compiled and
// published).
//
// Note that for the meta-build we only need to include the files in
// `scala-sbt-1.0` directory, not the files in the `scala-sbt-0.13`
// directory. This is because the meta-build in the `project`
// directory is only built with sbt 1 (the value set in the
// build.properties file). In a later stage of the sbt build, when we
// do the "proper build" in the root directory, interplay will be
// cross-compiled for both sbt 1 and sbt 0.13. At this stage, for the
// meta-build, we only need sbt 1.
unmanagedSourceDirectories in Compile ++= Seq(
  baseDirectory.value.getParentFile / "src" / "main" / "scala",
)
