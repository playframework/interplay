# Interplay - Play Build plugins

[![Twitter Follow](https://img.shields.io/twitter/follow/playframework?label=follow&style=flat&logo=twitter&color=brightgreen)](https://twitter.com/playframework)
[![Discord](https://img.shields.io/discord/931647755942776882?logo=discord&logoColor=white)](https://discord.gg/g5s2vtZ4Fa)
[![GitHub Discussions](https://img.shields.io/github/discussions/playframework/playframework?&logo=github&color=brightgreen)](https://github.com/playframework/playframework/discussions)
[![StackOverflow](https://img.shields.io/static/v1?label=stackoverflow&logo=stackoverflow&logoColor=fe7a16&color=brightgreen&message=playframework)](https://stackoverflow.com/tags/playframework)
[![YouTube](https://img.shields.io/youtube/channel/views/UCRp6QDm5SDjbIuisUpxV9cg?label=watch&logo=youtube&style=flat&color=brightgreen&logoColor=ff0000)](https://www.youtube.com/channel/UCRp6QDm5SDjbIuisUpxV9cg)
[![Twitch Status](https://img.shields.io/twitch/status/playframework?logo=twitch&logoColor=white&color=brightgreen&label=live%20stream)](https://www.twitch.tv/playframework)
[![OpenCollective](https://img.shields.io/opencollective/all/playframework?label=financial%20contributors&logo=open-collective)](https://opencollective.com/playframework)

[![Build Status](https://github.com/playframework/interplay/actions/workflows/build-test.yml/badge.svg)](https://github.com/playframework/interplay/actions/workflows/build-test.yml)
[![Maven](https://img.shields.io/maven-central/v/com.typesafe.play/interplay_2.13.svg?logo=apache-maven)](https://mvnrepository.com/artifact/com.typesafe.play/interplay_2.13)
[![Repository size](https://img.shields.io/github/repo-size/playframework/interplay.svg?logo=git)](https://github.com/playframework/interplay)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
[![Mergify Status](https://img.shields.io/endpoint.svg?url=https://api.mergify.com/v1/badges/playframework/interplay&style=flat)](https://mergify.com)

Interplay is a set of sbt plugins for Play builds, sharing common configuration between Play builds so that they can be configured in one place.

## Usage

Ensure you're using sbt 0.13.15, by setting `sbt.version` in `project/build.properties`.

Add the interplay plugin to `project/plugins.sbt`:

```scala
addSbtPlugin("com.typesafe.play" % "interplay" % sys.props.get("interplay.version").getOrElse("2.1.2"))
```

By allowing the version to be overridden using system properties, this means the Play nightly builds can override it to use the latest version.

Now which plugins you enable and what configuration you set depends on the structure of your build.  In all cases, you should set `playBuildRepoName in ThisBuild` to be the name of the GitHub repository in the `playframework` organisation that the project lives in.

### Play libraries

If your project is a simple library that gets used in a Play application then enable the `PlayLibrary` plugin on it.

### SBT plugins

If your project is an sbt plugin, then enable the `PlaySbtPlugin` plugin on it.

### SBT libraries

If your project is not an sbt plugin, but does get used by sbt, then enable the `PlaySbtLibrary` plugin on it.

### The root project

If you have a root project that is just a meta project that aggregates all your projects together, but itself shouldn't be published, then enable the `PlayRootProject` plugin on it.

### Aggregating projects

In general, your root project should aggregate all the projects you want to publish.  If it aggregates projects that you don't want to publish, you can make them not published by enabling the `PlayNoPublish` plugin.

### Play docs

If your project includes documentation that you want included in the main Play documentation, you can allow this by adding the `Playdoc` plugin to it.  In that case you also will need to configure the `playdocDirectory` to point to the documentation directory.
