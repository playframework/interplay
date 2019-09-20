package interplay

import java.io.File

import sbt.Keys._
import sbt.inc.{Doc => IncDoc}
import sbt.internal.inc.{AnalyzingCompiler, ManagedLoggedReporter}
import sbt.io.Path._
import sbt.io.{IO, PathFinder}
import sbt.util.CacheStoreFactory
import sbt.{Def, _}
import xsbti.Reporter
import xsbti.compile.{Compilers, IncToolOptionsUtil}

/**
 * An object that offers compatibility helpers when creating builds that must work in
 * different versions of sbt. This version is designed to be compatible with sbt 1.0.
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
  def optScriptedAutoPlugin: Option[AutoPlugin] = Some(ScriptedPlugin)
  /** Access to the ScriptedPlugin's settings. */
  def scriptedSettings: Seq[Setting[_]] = ScriptedPlugin.projectSettings
  /** The ScriptedPlugin's `scriptedLaunchOpts` setting. */
  def scriptedLaunchOpts: SettingKey[Seq[String]] = ScriptedPlugin.autoImport.scriptedLaunchOpts
  /** The ScriptedPlugin's `scripted` task. */
  def scriptedTask: InputKey[Unit] = ScriptedPlugin.autoImport.scripted

  /** Various methods that have changed signature or location between sbt 0.13 and 1.0 */
  object PathCompat {
    def allPaths(f: File): PathFinder = f.allPaths
    def relativeTo(f: File): PathMap = IO.relativize(f, _)
    def relativeTo(bases: Iterable[File]): PathMap = Path.relativeTo(bases)
    def rebase(a: File, b: String): PathMap = Path.rebase(a, b)
  }

  /** The Process object used by this version of sbt. */
  val Process = scala.sys.process.Process

  /**
   * Provides access to sbt's Load functionality. The method signature changed
   * in sbt 1 and the class moved to an internal package.
   */
  val LoadCompat = sbt.internal.PlayLoad

  /**
   * Provides access to sbt's EvaluateConfigurations functionality. The method signature changed
   * in sbt 1 and the class moved to an internal package.
   */
  val EvaluateConfigurationsCompat = sbt.internal.PlayEvaluateConfigurations

  /**
   * Run the scaladoc task, but with optional caching.
   *
   * Caching can be disabled to work around: https://github.com/sbt/sbt/issues/1614
   */
  def scaladocTask(
    label: String, cacheName: Option[String],
    sources: Seq[File], classpath: Seq[File], outputDirectory: File, options: Seq[String]) = Def.task {
    // This code based on sbt.Defaults
    val cs: Compilers = Keys.compilers.value
    val s: TaskStreams = streams.value
    val scalac: AnalyzingCompiler = cs.scalac.asInstanceOf[AnalyzingCompiler]
    val cacheStoreFactory: CacheStoreFactory = optionalCache(cacheName).value
    val runDoc: RawCompileLike.Gen =
      Doc.scaladoc(label, cacheStoreFactory, scalac)
    runDoc(sources, classpath, outputDirectory, options, maxErrors.value, s.log)
   }

  /**
   * Run the javadoc task, but with optional caching.
   *
   * Caching can be disabled to work around: https://github.com/sbt/sbt/issues/1614
   */
  def javadocTask(
      label: String, cacheName: Option[String],
      sources: Seq[File], classpath: Seq[File], outputDirectory: File, options: Seq[String]) = Def.task {
    // This code based on sbt.Defaults.docTaskSettings
    val cs: Compilers = Keys.compilers.value
    val s: TaskStreams = streams.value
    val cacheStoreFactory: CacheStoreFactory = optionalCache(cacheName).value

    val javadoc: IncDoc.JavaDoc = IncDoc.cachedJavadoc(label, cacheStoreFactory, cs.javaTools)

    // We can't access `(compilerReporter in compile).value` so we recreate it
    // here.
    val defaultCompilerReporter: Reporter = {
      // Copy of a private method from sbt.Defaults
      def foldMappers[A](mappers: Seq[A => Option[A]]) = {
        mappers.foldRight({ p: A =>
          p
        }) { (mapper, mappers) => { p: A =>
          mapper(p).getOrElse(mappers(p))
        }
        }
      }
      new ManagedLoggedReporter(
        maxErrors.value,
        s.log,
        foldMappers(sourcePositionMappers.value)
      )
    }

    // This code based on sbt.Defaults.docTaskSettings
    javadoc.run(sources.toList,
      classpath.toList,
      outputDirectory,
      options.toList,
      IncToolOptionsUtil.defaultIncToolOptions(),
      s.log,
      defaultCompilerReporter
    )
  }

  /**
   * Gets a cache if a name is provided, otherwise use a nop cache.
   */
  private def optionalCache(cacheName: Option[String]): Def.Initialize[Task[CacheStoreFactory]] = Def.task {
    cacheName match {
      case None => NopCacheStoreFactory // Not cached
      case Some(dirName) => streams.value.cacheStoreFactory.sub(dirName)
    }
  }

}