ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "Aufgabe1"
  )
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.0"
libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.7"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "2.0.7"