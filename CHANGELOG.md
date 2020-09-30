# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Update `scala` versions in mergify rules

### Fixed
- Changelog step in release workflow is triggered now

## [0.0.2] - 2020-09-29
### Added
- Validate CHANGELOG in CI workflow
- Close CHANGELOG version automatically when a new release is made
- Handle input for `Process`es
- Include latest stable and latest snapshot build tools info in docs
- Add ScalaDoc support

### Changed
- Update `scala` to 2.12.12 and 2.13.3
- Update `scalafmt` to 2.7.2
- Update `sbt-scalafmt` to 2.4.0
- Update `sbt` to 1.3.13
- Update `cats` to 2.2.0
- Update `cats-effect` to 2.2.0
- Update `scala` to 2.13.2
- Update `sbt-ci-release` to 1.5.3
- Update `sbt-mdoc` to 2.2.9
- Update `log4cats` to 1.1.1
- Update `sbt-tpolecat` to 0.1.13
- Update `fs2` to 2.4.4
- Replace `specs2` with `munit` and `munit-cats-effect`

### Fixed
- :rocket: GitHub Release job in release workflow
- Library version in site
- Maven badge now shows the latest stable release
- Snapshot releases are now using the proper version

## [0.0.1] - 2020-04-07
### Added
- CHANGELOG.md
- Setup project
- Setup Scala Steward
- Setup GitHub Actions as CI
- Setup Mergify for patch versions
- Setup Mergify for minor versions with approvals
- Setup [Gitter.im channel](https://gitter.im/cats4scala/cats-process)
- Add badges
- Setup site: https://cats4scala.github.io/cats-process
- Crossbuild project for Scala 2.12.11 and 2.13.1

[Unreleased]: https://github.com/cats4scala/cats-process/compare/v0.0.2...HEAD
[0.0.2]: https://github.com/cats4scala/cats-process/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/cats4scala/cats-process/compare/4ee110a...v0.0.1
