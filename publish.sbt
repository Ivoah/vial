credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials")

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
