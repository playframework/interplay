# Interplay - Play Build plugins

Interplay is a set of sbt plugins for Play builds, sharing common configuration between Play builds so that they can be configured in one place.

[![Build Status](https://travis-ci.org/playframework/interplay.svg?branch=main)](https://travis-ci.org/playframework/interplay)

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
