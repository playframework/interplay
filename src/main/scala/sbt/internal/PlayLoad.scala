/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package sbt.internal

import sbt._
import sbt.compiler.Eval
import sbt.util.Show

/**
 * Provides access to an interal sbt class. Accessed through the PlaySbtCompat object.
 */
object PlayLoad {

  def defaultLoad(state: State, localBase: java.io.File): (() => Eval, BuildStructure) = {
    Load.defaultLoad(state, localBase, state.log)
  }

  def getRootProject(map: Map[URI, BuildUnitBase]): URI => String = {
    Load.getRootProject(map)
  }

  def reapply(
      newSettings: Seq[Setting[_]],
      structure: BuildStructure
  )(implicit display: Show[ScopedKey[_]]): BuildStructure = {
    Load.reapply(newSettings, structure)
  }

}