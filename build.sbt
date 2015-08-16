
name :="Genesys"

version :="0.4"

scalaVersion :="2.11.4"

assemblyJarName in assembly := "Genesys.jar"


libraryDependencies ++= Seq(
  // "junit" % "junit" % "4.7" % "test",
  "org.scala-lang" % "scala-parser-combinators" % "2.11.0-M4" % "provided",
  "com.novocode" % "junit-interface" % "0.10" % "test",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.3" % "provided",
  "com.gilt" % "handlebars-scala_2.11" % "2.0.1",
  "org.json4s" % "json4s-native_2.11" % "3.2.11",
  "net.sourceforge.jtds" % "jtds" % "1.3.1",
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "com.beust" % "jcommander" % "1.30",
  "com.google.guava" % "guava" % "18.0",
  "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
  "mysql" % "mysql-connector-java" % "5.1.34"
)

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
