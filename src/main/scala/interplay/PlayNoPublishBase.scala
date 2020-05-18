package interplay

import sbt._
import sbt.Keys._
import sbt.Resolver
import com.jsuereth.sbtpgp.PgpKeys

object PlayNoPublishBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = PlayBuildBase

  override def projectSettings = Seq(
    publish / skip := true,
    PgpKeys.publishSigned := {},
    publish := {},
    publishLocal := {},
    publishTo := Some(Resolver.file("no-publish", crossTarget.value / "no-publish"))
  )
}
