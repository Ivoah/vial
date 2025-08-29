ThisBuild / organization := "net.ivoah"
ThisBuild / version := "0.6.1-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"

ThisBuild / scalacOptions += "-deprecation"

ThisBuild / description := "Tiny web framework for Scala inspired by bottle and flask."

val jettyVersion = "11.0.12"

lazy val vial = (project in file("vial"))
  .settings(
    name := "vial",
    idePackagePrefix := Some("net.ivoah.vial"),
    libraryDependencies ++= Seq(
      "org.eclipse.jetty" % "jetty-server" % jettyVersion,
      "org.eclipse.jetty" % "jetty-unixdomain-server" % jettyVersion
    )
  )

lazy val example = (project in file("example"))
  .dependsOn(vial)
  .settings(
    publish / skip := true,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "scalatags" % "0.12.0"
    )
  )
