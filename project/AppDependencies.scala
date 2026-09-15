import sbt._

object AppDependencies {

  private val bootstrapVersion = "10.8.0"
  private val hmrcMongoVersion = "2.13.0"
  private val scalaCatsVersion = "2.13.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"    % "13.13.0",
    "org.typelevel"     %% "cats-core"                     % "2.13.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"            % hmrcMongoVersion,
    "org.typelevel" %% "cats-core" % scalaCatsVersion,
    "uk.gov.hmrc"       %% s"crypto-json-play-30"          % "8.4.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"       %% "scalacheck-1-17"         % "3.2.18.0",
    "org.typelevel" %% "cats-core" % scalaCatsVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
