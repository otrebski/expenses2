ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.6"

val tapirVersion = "0.19.3"

lazy val root = (project in file(".")).settings(
  name := "expenses2",
  libraryDependencies ++= Seq(
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % "3.3.0",
    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % "3.3.0",
    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % "3.3.0",

    //Expression calucaltor
    "com.udojava" % "EvalEx" % "2.7",

    "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
    //Circe
    "io.circe" %% "circe-generic-extras" % "0.14.1",
    //Tapir
    "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % "0.19.0-M4",
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % "0.19.0-M4",
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % "0.19.0-M4",
//    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "0.20.0-M3",
    "com.softwaremill.sttp.client3" %% "core" % "3.3.18",
    "org.http4s" %% "http4s-dsl" % "0.23.6"
  )
)
