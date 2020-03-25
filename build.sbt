val scalaV = "2.13.1"

val catsV = "2.1.1"
val catsEffectV = "2.1.2"
val fs2V = "2.3.0"
val specs2V = "4.9.2"
val log4catsV = "1.0.1"

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
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(MdocPlugin)
  .enablePlugins(NoPublishPlugin)
  .settings(commonSettings)
  .dependsOn(core)
  .settings {
    import microsites._
    Seq(
      micrositeName := "cats-process",
      micrositeDescription := "Functional command and process",
      micrositeAuthor := "cats4scala",
      micrositeGithubOwner := "cats4scala",
      micrositeGithubRepo := "cats-process",
      micrositeBaseUrl := "/cats-process",
      micrositeDocumentationUrl := "https://www.javadoc.io/doc/c4s/cats-process_2.13",
      micrositeFooterText := None,
      micrositeHighlightTheme := "atom-one-light",
      micrositePalette := Map(
        "brand-primary" -> "#3e5b95",
        "brand-secondary" -> "#294066",
        "brand-tertiary" -> "#2d5799",
        "gray-dark" -> "#49494B",
        "gray" -> "#7B7B7E",
        "gray-light" -> "#E5E5E6",
        "gray-lighter" -> "#F4F3F4",
        "white-color" -> "#FFFFFF"
      ),
      micrositeCompilingDocsTool := WithMdoc,
      scalacOptions in Tut --= Seq(
        "-Xfatal-warnings",
        "-Ywarn-unused-import",
        "-Ywarn-numeric-widen",
        "-Ywarn-dead-code",
        "-Ywarn-unused:imports",
        "-Xlint:-missing-interpolator,_"
      ),
      micrositePushSiteWith := GitHub4s,
      micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
      micrositeExtraMdFiles := Map(
        file("CODE_OF_CONDUCT.md") -> ExtraMdFileConfig(
          "code-of-conduct.md",
          "page",
          Map("title" -> "code of conduct", "section" -> "code of conduct", "position" -> "100")
        ),
        file("LICENSE") -> ExtraMdFileConfig(
          "license.md",
          "page",
          Map("title" -> "license", "section" -> "license", "position" -> "101")
        )
      )
    )
  }

// General Settings
lazy val commonSettings = Seq(
  scalaVersion := scalaV,
  crossScalaVersions := Seq(scalaV, "2.12.10"),
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
    organization := "c4s",
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
