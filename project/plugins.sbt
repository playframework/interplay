import java.util.Locale

libraryDependencies ++= Seq(
  "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value,
  "com.typesafe" % "config" % "1.4.0"
)

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.6")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.8.1")
addSbtPlugin("com.lightbend" % "sbt-whitesource" % "0.1.18")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.0.0")

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
unmanagedSourceDirectories in Compile ++= Seq(
  baseDirectory.value.getParentFile / "src" / "main" / "scala"
)
