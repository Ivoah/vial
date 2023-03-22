// Your profile name of the sonatype account. The default is the same with the organization value
sonatypeProfileName := "Ivoah"

// To sync with Maven central, you need to supply the following information:
publishMavenStyle := true

// Open-source license of your choice
licenses := Seq("MIT License" -> url("https://opensource.org/license/mit/"))

// Where is the source code hosted: GitHub or GitLab?
import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("Ivoah", "vial", "noah@ivoah.net"))

developers := List(
  Developer(id="Ivoah", name="Noah Rosamilia", email="noah@ivoah.net", url=url("https://ivoah.net"))
)
