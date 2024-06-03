ThisBuild / developers := List(
  Developer(
    id = "Ivoah",
    name = "Noah Rosamilia",
    email = "noah@ivoah.net",
    url = url("https://ivoah.net")
  )
)

ThisBuild / homepage := Some(url("https://github.com/Ivoah/vial"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/Ivoah/vial"),
    "scm:git@github.com:Ivoah/vial.git"
  )
)

ThisBuild / licenses := List(
  "MIT License" -> url("https://opensource.org/license/mit/")
)

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / publishMavenStyle := true
