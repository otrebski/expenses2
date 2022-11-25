ThisBuild / organization := "o"
ThisBuild / scalaVersion := "3.2.0"

val tapirVersion = "1.2.2"

lazy val root = (project in file(".")).settings(
  name := "expenses2",
  libraryDependencies ++= Seq(
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % "3.4.1",
    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % "3.3.14",
    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % "3.3.14",

    //Expression calucaltor
    "com.udojava" % "EvalEx" % "2.7",

    "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
    //Circe
//    "io.circe" %% "circe-generic-extras" % "0.14.2",
    //Tapir
    "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
    //    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % "0.19.0-M4",
    //    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
    //    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % "1.0.0-M9",
    //    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
    "com.softwaremill.sttp.client3" %% "core" % "3.8.3",
    //    "org.http4s" %% "http4s-dsl" % "1.0.0-M33" //,"0.23.12"
    "org.http4s" %% "http4s-dsl" % "0.23.16",
    "org.http4s" %% "http4s-ember-server" % "0.23.16",
  )
)
