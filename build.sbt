               name := "interplay"
            version := "0.1.0"
       organization := "com.typesafe.play"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

          sbtPlugin := true
  publishMavenStyle := false
          publishTo := sbtPluginRepos.value

def sbtPluginRepos = Def.setting {
  if (isSnapshot.value) Some(Classpaths.sbtPluginSnapshots)
  else Some(Classpaths.sbtPluginReleases)
}
