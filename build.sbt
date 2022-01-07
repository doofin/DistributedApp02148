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
//      "org.fxmisc.richtext" % "richtextfx" % "0.10.7",
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
      "com.formdev" % "flatlaf" % "2.0-rc1"
    )
  )
/*
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _                            => throw new Exception("Unknown platform!")
}

// Add dependency on JavaFX libraries, OS dependent
lazy val javaFXModules =
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")

++ javaFXModules.map(
//       https://mvnrepository.com/artifact/org.openjfx/javafx
      m => "org.openjfx" % s"javafx-$m" % "18-ea+9" classifier osName //"16"
    )
 * */
