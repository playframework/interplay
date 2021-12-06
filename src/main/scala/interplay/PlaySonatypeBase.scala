package interplay

import sbt.AutoPlugin
import sbt.Keys.publishTo
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.autoImport.{ sonatypePublishToBundle, sonatypeProfileName }

/**
 * Base plugin for all projects that publish to sonatype (which is all of them!)
 */
object PlaySonatypeBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = Sonatype

  override def projectSettings = Seq(
    sonatypeProfileName := "com.typesafe.play",
    publishTo := sonatypePublishToBundle.value,
  )
}
