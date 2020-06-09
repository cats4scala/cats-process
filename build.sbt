val scalaV = "2.13.2"

val catsV = "2.1.1"
val catsEffectV = "2.1.3"
val fs2V = "2.4.1"
val specs2V = "4.9.4"
val log4catsV = "1.1.1"

val kindProjectorV = "0.11.0"
val betterMonadicForV = "0.3.1"

// Projects
lazy val `cats-process` = project
  .in(file("."))
  .disablePlugins(MimaPlugin)
  .enablePlugins(NoPublishPlugin)
  .settings(
    scalaVersion := scalaV
  )
  .aggregate(core)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "cats-process",
    scalaVersion := scalaV
  )

lazy val site = project
  .in(file("site"))
  .disablePlugins(MimaPlugin)
  .enablePlugins(MdocPlugin)
  .enablePlugins(ParadoxSitePlugin)
  .enablePlugins(ParadoxMaterialThemePlugin)
  .enablePlugins(PreprocessPlugin)
  .enablePlugins(NoPublishPlugin)
  .settings(
    scalaVersion := scalaV,
    makeSite := makeSite.dependsOn(mdoc.toTask("")).value,
    mdocIn := baseDirectory.value / "docs",
    sourceDirectory in (Compile, paradox) := mdocOut.value,
    Compile / paradoxMaterialTheme ~= {
      _.withRepository(uri("https://github.com/cats4scala/cats-process"))
    },
    paradoxProperties += ("project.version.stable" -> previousStableVersion.value.get)
  )
  .dependsOn(core)

// General Settings
lazy val commonSettings = Seq(
  scalaVersion := scalaV,
  crossScalaVersions := Seq(scalaV, "2.12.11"),
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % kindProjectorV cross CrossVersion.full),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % betterMonadicForV),
  libraryDependencies ++= Seq(
    "org.typelevel"     %% "cats-core"        % catsV,
    "org.typelevel"     %% "cats-effect"      % catsEffectV,
    "co.fs2"            %% "fs2-core"         % fs2V,
    "co.fs2"            %% "fs2-io"           % fs2V,
    "io.chrisdavenport" %% "log4cats-core"    % log4catsV,
    "io.chrisdavenport" %% "log4cats-slf4j"   % log4catsV,
    "io.chrisdavenport" %% "log4cats-testing" % log4catsV % Test,
    "org.specs2"        %% "specs2-core"      % specs2V % Test
  )
)

// General Settings
inThisBuild(
  List(
    organization := "io.github.cats4scala",
    developers := List(
      Developer("JesusMtnez", "Jesús Martínez", "jesusmartinez93@gmail.com", url("https://github.com/JesusMtnez")),
      Developer(
        "pirita",
        "Ignacio Navarro Martín",
        "ignacio.navarro.martin@gmail.com",
        url("https://github.com/pirita")
      ),
      Developer("BeniVF", "Benigno Villa Fernández", "beni.villa@gmail.com", url("https://github.com/BeniVF"))
    ),
    homepage := Some(url("https://github.com/cats4scala/cats-process")),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    pomIncludeRepository := { _ => false },
    scalacOptions in (Compile, doc) ++= Seq(
      "-groups",
      "-sourcepath",
      (baseDirectory in LocalRootProject).value.getAbsolutePath,
      "-doc-source-url",
      "https://github.com/cats4scala/cats-process/blob/v" + version.value + "€{FILE_PATH}.scala"
    )
  )
)
