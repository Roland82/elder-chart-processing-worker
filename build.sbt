import com.github.retronym.SbtOneJar._

oneJarSettings

name := "elder-chart-processing-worker"

version := "SNAPSHOT"

organization := "elder"

scalaVersion  := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

fork in run := true

mainClass in (Compile, run) := Some("uk.co.elder.Main")


resolvers ++= Seq(
  "typesafe.com"                     at "http://repo.typesafe.com/typesafe/repo/",
  "Typesafe repository snapshots"    at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases"     at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Twitter Repository"               at "http://maven.twttr.com",
  "Websudos releases"                at "https://dl.bintray.com/websudos/oss-releases/"
)

{
  val scalazVersion = "7.2.7"
  val Json4sVersion = "3.2.11"
  val Log4jVersion = "2.1"
  val ScalaTestVersion = "2.2.4"
  val SlickVersion = "2.1.0"

  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "io.relayr" %% "rabbitmq-scala-client" % "0.1.8",
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "org.scalaz" %% "scalaz-effect" % scalazVersion,
    "org.scalaz" %% "scalaz-concurrent" % scalazVersion,

    "com.googlecode.flyway" % "flyway-core" % "2.3.1",
    "com.github.tomakehurst" % "wiremock" % "1.18",
    "com.github.scopt" %% "scopt" % "3.2.0",
    "com.typesafe" % "config" % "1.2.1",
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
    "com.typesafe.slick" %% "slick" % SlickVersion,

    "net.codingwell" %% "scala-guice" % "4.0.0-beta4",

    "org.apache.logging.log4j" % "log4j-api" % Log4jVersion,
    "org.apache.logging.log4j" % "log4j-core" % Log4jVersion,
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % Log4jVersion,
    "net.liftweb"             %%  "lift-json"                % "3.0",
    "org.scalaj"              %%  "scalaj-http"              % "1.1.4",
    "com.websudos"            %%  "phantom-dsl"              % "1.26.1",
    "joda-time"               %   "joda-time"                % "2.7",
    "net.databinder.dispatch" %% "dispatch-core"             % "0.11.3",
    "com.github.tototoshi"    %%  "scala-csv"                % "1.2.1",
    "com.datastax.cassandra"  %   "cassandra-driver-core"    % "3.0.2" % "compile",
    //-------------------------------------------------------------------------
    "org.scalacheck" %% "scalacheck" % "1.13.0" % "test",
    "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test",
    "org.typelevel" %% "scalaz-scalatest" % "0.3.0" % "test",
    "com.typesafe.slick" %% "slick-testkit" % SlickVersion % "test",
    "org.mockito" % "mockito-core" % "1.10.8" % "test",
    "org.scalatest" %% "scalatest" % ScalaTestVersion % "test"
  )
}
