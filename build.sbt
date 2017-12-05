name := "stock-exchange-service"

version := "0.0.1"

scalaVersion := "2.12.4"

mainClass in (Compile, run) := Some("ru.roman3pm.stock.exchange.service.Main")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.7",
  "com.typesafe.akka" %% "akka-stream" % "2.5.7",
  "org.json4s" %% "json4s-native" % "3.5.3",
  "org.json4s" %% "json4s-jackson" % "3.5.3",
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)
        