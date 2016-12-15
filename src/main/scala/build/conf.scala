package build

import com.typesafe.config._
import java.io.File

object conf {

  def apply(key: String): String = config.getString(key)

  private lazy val cwd: File = new File(sys.props("user.dir"))

  private lazy val config: Config = load(find("build.conf"))

  private def load(conf: Option[File]): Config = conf match {
    case Some(file) => ConfigFactory.defaultOverrides.withFallback(ConfigFactory.parseFileAnySyntax(file)).resolve
    case None => ConfigFactory.defaultOverrides
  }

  private def find(name: String, current: File = cwd): Option[File] = {
    path(current.getCanonicalFile).map(dir => new File(dir, name)).find(_.exists)
  }

  private def path(current: File, files: Seq[File] = Seq.empty): Seq[File] = {
    if (current eq null) files else path(current.getParentFile, files :+ current)
  }

}
