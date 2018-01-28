package interplay

import java.io.File

import sbt._
import sbt.compiler.{AnalyzingCompiler, Eval, JavaTool}

/**
 * An object that offers compatibility helpers when creating builds that must work in
 * different versions of sbt. This version is designed to be compatible with sbt 0.13.
 */
object PlaySbtCompat {
  /**
   * If the ScriptedPlugin is an AutoPlugin then provide it here. This is
   * convenient if we want to disable it automatically. The plugin is a
   * normal plugin in sbt 0.13 and an AutoPlugin (which we often need to
   * disable) in sbt 1.0.
   *
   * @see https://github.com/sbt/sbt/issues/3514
   */
  def optScriptedAutoPlugin: Option[AutoPlugin] = None
  /** Access to the ScriptedPlugin's settings. */
  def scriptedSettings: Seq[Setting[_]] = ScriptedPlugin.scriptedSettings
  /** The ScriptedPlugin's `scriptedLaunchOpts` setting. */
  def scriptedLaunchOpts: SettingKey[Seq[String]] = ScriptedPlugin.scriptedLaunchOpts
  /** The ScriptedPlugin's `scripted` task. */
  def scriptedTask: InputKey[Unit] = ScriptedPlugin.scripted

  /** Various methods that have changed signature or location between sbt 0.13 and 1.0 */
  object PathCompat {
    def allPaths(base: File): PathFinder = base.***
    def relativeTo(f: File): PathMap = IO.relativize(f, _)
    def relativeTo(bases: Iterable[File]): PathMap = Path.relativeTo(bases)
    def rebase(a: File, b: String): PathMap = Path.rebase(a, b)
  }

  /** The Process object used by this version of sbt. */
  val Process = sbt.Process

  /**
   * Provides access to sbt Load functionality. The method signature changed
   * in sbt 1 and the class moved to an internal package.
   */
  object LoadCompat {
    def defaultLoad(state: State, localBase: java.io.File): (() => Eval, BuildStructure) = {
      Load.defaultLoad(state, localBase, state.log)
    }
    def getRootProject(map: Map[URI, sbt.BuildUnitBase]): URI => String = {
      Load.getRootProject(map)
    }
    def reapply(
        newSettings: Seq[Setting[_]],
        structure: BuildStructure
    )(implicit display: Show[ScopedKey[_]]): BuildStructure = {
      Load.reapply(newSettings, structure)
    }
  }

  /**
   * Provides access to sbt EvaluateConfigurations functionality. The method signature changed
   * in sbt 1 and the class moved to an internal package.
   */
  object EvaluateConfigurationsCompat {
    def evaluateConfigurations(sbtFile: java.io.File, imports: Seq[String], classLoader: ClassLoader, eval: () => Eval): Seq[Def.Setting[_]] = {
      EvaluateConfigurations.evaluateConfiguration(eval(), sbtFile, imports)(classLoader)
    }
  }

  /**
   * Run the scaladoc task, but with optional caching.
   *
   * Caching can be disabled to work around: https://github.com/sbt/sbt/issues/1614
   */
  def scaladocTask(
      label: String, cacheName: Option[String],
      sources: Seq[File], classpath: Seq[File],
      outputDirectory: File, options: Seq[String]): Def.Initialize[Task[Unit]] = Def.task {

    // This is based on code in sbt.Defaults and sbt.Doc
    val scalac: AnalyzingCompiler = Keys.compilers.value.scalac
    val docGen: RawCompileLike.Gen = scalac.doc
    docTask(
      label + " Scala API documentation", cacheName, docGen,
      sources, sourceFilter = (_: File) => true,
      classpath, outputDirectory, options)
  }

  /**
   * Run the javadoc task, but with optional caching.
   *
   * Caching can be disabled to work around: https://github.com/sbt/sbt/issues/1614
   */
  def javadocTask(
      label: String, cacheName: Option[String],
      sources: Seq[File], classpath: Seq[File],
      outputDirectory: File, options: Seq[String]): Def.Initialize[Task[Unit]] = Def.task {

    // This is based on code in sbt.Defaults and sbt.Doc
    val javac: JavaTool = Keys.compilers.value.javac
    val docGen: RawCompileLike.Gen = javac.doc
    docTask(
      label + " Java API documentation", cacheName, docGen,
      sources, sourceFilter = Doc.javaSourcesOnly,
      classpath, outputDirectory, options)
  }

   /** Used to run scaladoc or javadoc with caching, etc. Takes a Gen, wraps it and calls it. */
   private def docTask(
      prepareMsg: String, cacheName: Option[String], compileGen: RawCompileLike.Gen,
      sources: Seq[File], sourceFilter: File => Boolean, classpath: Seq[File],
      outputDirectory: File, options: Seq[String]): Def.Initialize[Task[Unit]] = Def.task {

    // This is based on code in sbt.Defaults and sbt.Doc. We wrap take a function of
    // type RawCompileLike.Gen then wrap it to add more functionality (e.g. filtering,
    // caching).
    val streamsValue = Keys.streams.value
    val prepareGen: RawCompileLike.Gen = RawCompileLike.prepare(prepareMsg, compileGen)
    val filterGen: RawCompileLike.Gen = RawCompileLike.filterSources(sourceFilter, prepareGen)
    val maybeCacheGen: RawCompileLike.Gen = cacheName match {
      case None => filterGen // Not cached
      case Some(dirName) =>
        RawCompileLike.cached(
          new File(streamsValue.cacheDirectory, dirName),
          filterGen
        )
    }
    maybeCacheGen(sources, classpath, outputDirectory, options, 10, streamsValue.log)
  }

}