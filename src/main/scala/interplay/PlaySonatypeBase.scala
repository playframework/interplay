package interplay

import sbt.AutoPlugin
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.autoImport.sonatypeProfileName

/**
 * Base plugin for all projects that publish to sonatype (which is all of them!)
 */
object PlaySonatypeBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = Sonatype

  override def projectSettings = Seq(
    sonatypeProfileName := "com.typesafe.play"
  )
}
