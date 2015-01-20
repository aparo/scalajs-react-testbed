
resolvers += "spray repo" at "http://repo.spray.io"

resolvers  += "Online Play Repository" at
  "http://repo.typesafe.com/typesafe/simple/maven-releases/"

//addSbtPlugin("com.lihaoyi" % "workbench" % "0.2.1")

libraryDependencies ++= Seq(
  // Add your project dependencies here,
  "commons-io" % "commons-io" % "2.0.1"
)
