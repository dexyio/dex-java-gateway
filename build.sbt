name := "DexJavaGateway"

version := "1.0"

scalaVersion := "2.12.1"

unmanagedBase <<= baseDirectory { base => base / "libs" }


// https://mvnrepository.com/artifact/junit/junit
libraryDependencies += "junit" % "junit" % "4.12"
