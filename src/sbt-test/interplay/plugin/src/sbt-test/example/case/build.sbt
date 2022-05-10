// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
(ThisBuild / dynverVTagPrefix) := false

TaskKey[Unit]("touchFile") := {
  IO.write(file(sys.props("scripted-file")), "ran")
}