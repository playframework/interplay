import java.util.Locale

libraryDependencies ++= Seq(
  "org.scala-sbt" % "scripted-plugin" % sbtVersion.value,
  "com.typesafe" % "config" % "1.3.1"
)

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.1")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")
addSbtPlugin("com.lightbend" % "sbt-whitesource" % "0.1.10")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

lazy val build = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := libraryDependencies.value.map { module =>
      val key = "-([a-z])".r.replaceAllIn(module.name, matched => matched.group(1).toUpperCase(Locale.ENGLISH)) + "Version"
      (key -> module.revision): BuildInfoKey
    }
  )

unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile / "src" / "main" / "scala"
