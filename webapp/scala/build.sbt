import java.io.File

servletSettings

scalariformSettings

com.typesafe.sbt.SbtStartScript.startScriptForClassesSettings

name := "isucon5-qualify-scala"

version := "1.0"

crossPaths := false

scalaVersion := "2.11.7"

lazy val skinnyMicroVersion = "0.9.12"

resolvers += "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"

// for Scalate
dependencyOverrides := Set("org.scala-lang" % "scala-compiler" % scalaVersion.value)

libraryDependencies ++= Seq(
  // micro Web framework
  "org.skinny-framework" %% "skinny-micro" % skinnyMicroVersion,
  "org.skinny-framework" %% "skinny-micro-scalate" % skinnyMicroVersion,
  // Standalone Web server (Jetty 9.2 / Servlet 3.1)
  "org.skinny-framework" %% "skinny-micro-server" % skinnyMicroVersion,
  "org.eclipse.jetty" % "jetty-webapp" % "9.2.13.v20150730" % "container",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "com.github.nscala-time" %% "nscala-time" % "2.2.0"
)

mainClass in Compile := Some("skinny.standalone.JettyLauncher")

// add src/main/webapp to unmanaged resources for sbt-start-script
unmanagedResourceDirectories in Compile <++= baseDirectory { base =>
  Seq(base / "../static") ++ sys.env.get("LOCAL_DEV").map(_ => Seq.empty).getOrElse(Seq(base / "src/main/webapp"))
}

webappResources in Compile += baseDirectory.value / "../static"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

lazy val launcher = taskKey[File]("launcher generation task")

launcher := {
  val s = streams.value
  val runtimeClassPaths = (dependencyClasspath in Runtime).value
  val cpStr = runtimeClassPaths.seq.map(_.data.getAbsolutePath).mkString(java.io.File.pathSeparator)
  // Ensure running compile task to generate target/classes folder
  (compile in Compile).value
  val staticWebappResoruce = target.value / "webapp"
  val script =
    s"""#!/bin/bash
      |PROJECT_DIR=$$(cd "$${BASH_SOURCE[0]%/*}" && pwd -P)/..
      |MAINCLASS=isucon5.JettyLauncher
      |
      |exec java $$JAVA_OPTS -cp "${cpStr}" \\
      |"$$MAINCLASS" "$$@"
      |""".stripMargin
  val launcherFile = target.value / "launcher"
  IO.write(launcherFile, script)
  launcherFile.setExecutable(true)
  s.log.info(s"Generated ${launcherFile}")
  launcherFile
}
