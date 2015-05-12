TaskKey[Unit]("touchFile") := {
  IO.write(file(sys.props("scripted-file")), "ran")
}