val root = project
  .in(file("."))
  .settings(
    scalaVersion := "3.1.0-RC2",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % "0.23.4",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "0.19.0-M9",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "0.19.0-M9",
    ),
    semanticdbEnabled := true,
  )
