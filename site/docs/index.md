# cats-process

`cats-process` is a library for purely functional command and process execution in the Scala language. It is available for Scala 2.12 and Scala 2.13. `cats-process` is built upon two major functional libraries for Scala, [`cats`][cats], and [`cats-effect`][cats-effect].

## Getting started

To start using `cats-process`, add the following dependencies to your `build.sbt`:

@@dependency[sbt,Maven,Gradle] {
  group="io.github.cats4scala"
  artifact="cats-process_$scala.binary.version$"
  version="$project.version$"
}

[cats]: https://typelevel.org/cats/
[cats-effect]: https://typelevel.org/cats-effect/
