package interplay

import sbt._
import sbt.Keys.publishTo
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.autoImport.{sonatypeProfileName, sonatypePublishToBundle}

/**
 * Base plugin for all projects that publish to sonatype
 */
object PlaySonatypeBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = Sonatype

  override def projectSettings = Seq(
    sonatypeProfileName := "com.typesafe",
    publishTo := sonatypePublishToBundle.value,
  )
}
