name := "DexJavaGateway"

version := "1.0"

scalaVersion := "2.12.1"

unmanagedBase <<= baseDirectory { base => base / "libs" }

// https://mvnrepository.com/artifact/junit/junit
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.12",
  "org.scala-lang" % "scala-library" % scalaVersion.value
)
