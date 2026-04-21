import play.sbt.PlayImport.jdbc
import sbt.Keys.libraryDependencies
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.7.0"
  private val oraVersion           = "19.3.0.0"
  private val slickVersion = "3.6.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"           % bootstrapVersion,
    "com.oracle.database.jdbc"         %  "ojdbc8"                     % oraVersion,
    "com.oracle.database.nls"          %  "orai18n"                    % oraVersion,
    "org.scala-lang"                   % "scala-library"               % "2.13.17",
    jdbc,
    "com.typesafe.slick" %% "slick"         % slickVersion,
    "com.typesafe.slick" %% "slick-codegen" % slickVersion
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion            % Test,
    
  )



  val it = Seq.empty
}
