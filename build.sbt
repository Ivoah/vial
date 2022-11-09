ThisBuild / organization := "net.ivoah"
ThisBuild / version := "0.3.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.0"

ThisBuild / scalacOptions += "-deprecation"

val jettyVersion = "11.0.12"

lazy val root = (project in file("."))
  .settings(
    name := "Vial",
    idePackagePrefix := Some("net.ivoah.vial"),
    credentials += Credentials(Path.userHome / ".sbt" / "space-maven.credentials"),
    publishTo := Some("space-maven" at "https://maven.pkg.jetbrains.space/ivoah/p/vial/maven"),
    libraryDependencies ++= Seq(
      "org.eclipse.jetty" % "jetty-server" % jettyVersion,
      "org.eclipse.jetty" % "jetty-unixdomain-server" % jettyVersion
    )
  )
