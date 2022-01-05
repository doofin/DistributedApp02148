ThisBuild / scalaVersion := "2.13.7"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

resolvers += "jitpack" at "https://jitpack.io"

lazy val root = (project in file("."))
  .settings(
    name := "DistributedApp",
    libraryDependencies ++= Seq(
      "com.github.pSpaces" % "jSpace" % "9ff32b60f1",
      "com.lihaoyi" %% "pprint" % "0.7.1",
    )
  )

