/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package sbt.internal

import sbt._
import sbt.internal._
import sbt.compiler.Eval

/**
 * Provides access to an interal sbt class. Accessed through the PlaySbtCompat object.
 */
object PlayEvaluateConfigurations {

  def evaluateConfigurations(sbtFile: java.io.File, imports: Seq[String], classLoader: ClassLoader, eval: () => Eval): Seq[Def.Setting[_]] = {
    EvaluateConfigurations.evaluateConfiguration(eval(), sbtFile, imports)(classLoader)
  }

}