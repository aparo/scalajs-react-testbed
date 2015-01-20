import sbt._
import Keys._

import com.typesafe.sbt.pgp.PgpKeys._
import org.apache.commons.io.FileUtils
import org.scalajs.sbtplugin.ScalaJSPlugin
import ScalaJSPlugin._
import ScalaJSPlugin.autoImport._

object ScalajsReact extends Build {

  val scalaJsOutputDir = Def.settingKey[File]("directory for javascript files output by scalajs")

  val Scala211 = "2.11.5"

  type PE = Project => Project

  def commonSettings: PE =
    _.enablePlugins(ScalaJSPlugin)
      .settings(
        organization       := "io.megl",
        version            := "0.7.2-RC1",
        homepage           := Some(url("https://github.com/aparo/scalajs-react-testbed")),
        licenses           += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0")),
        scalaVersion       := Scala211,
        // crossScalaVersions := Seq("2.10.4", Scala211), https://github.com/japgolly/scalajs-react/issues/39
        scalacOptions     ++= Seq("-deprecation", "-unchecked", "-feature",
                                "-language:postfixOps", "-language:implicitConversions",
                                "-language:higherKinds", "-language:existentials"),
        updateOptions      := updateOptions.value.withCachedResolution(true))

  def preventPublication: PE =
    _.settings(
      publishArtifact := false,
      publishLocalSigned := (),       // doesn't work
      publishSigned := (),            // doesn't work
      packagedArtifacts := Map.empty) // doesn't work - https://github.com/sbt/sbt-pgp/issues/42

  def utestSettings: PE =
    _.settings(utest.jsrunner.Plugin.utestJsSettings: _*)
      .configure(useReact("test"))
      .settings(
        scalaJSStage in Test := FastOptStage,
        requiresDOM := true,
        jsEnv in Test := PhantomJSEnv().value)



  def useReact(scope: String = "compile"): PE =
    _.settings(
      jsDependencies += "org.webjars" % "react" % "0.12.1" % scope / "react-with-addons.js" commonJSName "React",
      skip in packageJSDependencies := false)

  def addCommandAliases(m: (String, String)*) = {
    val s = m.map(p => addCommandAlias(p._1, p._2)).reduce(_ ++ _)
    (_: Project).settings(s: _*)
  }

  def extModuleName(shortName: String): PE =
    _.settings(name := s"ext-$shortName")

    /*
  def copyToGrunt(jsFileList: Seq[VirtualJSFile]): Unit = {
    println("Invoking copyToGrunt")
    jsFileList.foreach {
      x => x match {
        case ax:FileVirtualFile =>
          println(s"Copying file ${ax.path} to $outputGrunt")
          FileUtils.copyFileToDirectory(new File(ax.path), new File(outputGrunt))
          FileUtils.copyFileToDirectory(new File(ax.path.toString+".map"), new File(outputGrunt))

        case _ => ;
      }
    }
  }
  */

  lazy val scalaJsSettings =
    Seq(
      persistLauncher := true,
      persistLauncher in Test := false,
      relativeSourceMaps := true,
      libraryDependencies ++= Deps.scalaJs.value
    )
  // ==============================================================================================
//resolvers += "bintray/non" at "http://dl.bintray.com/non/maven"
/*
  lazy val settingsJS=Seq(
    scalaJsOutputDir := (classDirectory in Compile).value / "app" / "js"//,
    //compile in Compile <<= copySourceMapsTask

//    scalajsOutputDir := (crossTarget in Compile).value / "classes" / "public" / "javascripts",
//    compile in Compile <<= (compile in Compile) dependsOn (fastOptJS in (scalajs, Compile)),
//    dist <<= dist dependsOn (fullOptJS in (scalajs, Compile)),
//    stage <<= stage dependsOn (fullOptJS in (scalajs, Compile))

  )++ {
      // ask scalajs project to put its outputs in scalajsOutputDir
    } 
*/
  lazy val scalajs = Project("scalajs", file(".")).enablePlugins(ScalaJSPlugin)
    .settings(
      name := "core",
      libraryDependencies ++= Seq(
"com.github.japgolly.scalajs-react" %%% "core" % "0.7.2-RC1",
"com.github.japgolly.scalajs-react" %%% "extra" % "0.7.2-RC1",
"com.lihaoyi" %%% "upickle" % "0.2.6-RC1"),
      persistLauncher := true
    )
    .settings(
    scalaJsOutputDir := baseDirectory.value / "app" / "js"
      ).settings(      Seq(packageJSDependencies, packageScalaJSLauncher, fastOptJS, fullOptJS) map { packageJSKey =>

        crossTarget in(Compile, packageJSKey) := scalaJsOutputDir.value
      }:_*
)
//      .configure(useReact("scalajs"))
    .configure(commonSettings, preventPublication, addCommandAliases(
      "t"  -> "; test:compile ; test/test",
      "tt" -> ";+test:compile ;+test/test",
      "T"  -> "; clean ;t",
      "TT" -> ";+clean ;tt"))

  object Deps {

    val shared = Def.setting(Seq(
      "org.scala-lang.modules" %% "scala-async" % "0.9.2",
      "com.lihaoyi" %%% "upickle" % "0.2.6-RC1"
    ))

    val scalaJvm = Def.setting(shared.value ++ Seq(
      "org.webjars" % "react" % "0.12.2"
    ))

    val scalaJs = Def.setting(shared.value ++ Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.7.0",
      "com.github.japgolly.scalajs-react" %%% "core" % "0.7.2-RC1"
    ))
  }

  val copySourceMapsTask = Def.task {
    val scalaFiles = (Seq(scalajs.base) ** "*.scala").get
    for (scalaFile <- scalaFiles) {
      val target = new File((classDirectory in Compile).value, scalaFile.getPath)
      IO.copyFile(scalaFile, target)
    }
  }

/*
  lazy val test = project
    .configure(commonSettings, publicationSettings, utestSettings)
    .dependsOn(core, scalaz71, extra, monocle)
    .settings(
      name := "test",
      scalacOptions += "-language:reflectiveCalls")
*/
}
