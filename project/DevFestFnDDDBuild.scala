import sbt._
import sbt.Keys._

object DevFestFnDDDBuild extends Build {
  lazy val devfest = Project(
    id = "devfest",

    base = file("."),

    settings = Project.defaultSettings ++ Seq(
      name := "devfest-fnddd",
      organization := "at.devfest",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.1",

      resolvers ++= Seq(
        "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"
      ),

      libraryDependencies ++= Seq(
				"com.typesafe.akka" %% "akka-actor" % "2.1.2",
        "org.scalaz" %% "scalaz-core" % "7.0.0",
				"joda-time" % "joda-time" % "2.1",
				"org.joda" % "joda-convert" % "1.3"
      )
    )
  )
}
