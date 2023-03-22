ThisBuild / organization := "net.ivoah"
ThisBuild / version := "0.3.2"
ThisBuild / scalaVersion := "3.2.0"

ThisBuild / scalacOptions += "-deprecation"

val jettyVersion = "11.0.12"

lazy val vial = (project in file("vial"))
  .settings(
    name := "Vial",
    description := "Tiny web framework for Scala inspired by bottle and flask.",
    idePackagePrefix := Some("net.ivoah.vial"),
    publishTo := {
      val nexus = "https://s01.oss.sonatype.org/"
      if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
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
