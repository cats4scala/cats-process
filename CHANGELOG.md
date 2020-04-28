# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Validate CHANGELOG in CI workflow
- Close CHANGELOG version automatically when a new release is made

### Changed
- Update `specs2` to 4.9.3
- Update `sbt-scalafmt` to 2.3.4
- Update `sbt` to 1.3.10
- Update `cats-effect` to 2.1.3
- Update `scala` to 2.13.2

### Fixed
- :rocket: GitHub Release job in release workflow
- Library version in site
- Maven badge now shows the latest stable release

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

[Unreleased]: https://github.com/cats4scala/cats-process/compare/v0.0.1...HEAD
[0.0.1]: https://github.com/cats4scala/cats-process/compare/4ee110a...v0.0.1
