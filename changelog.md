# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- upgrade: update spring-boot from v3.2.1 to v3.3.4
- upgrade: update lightning-kmp from v1.7.3 to v1.8.4
- upgrade: update jmolecules-bom from v2023.1.3 to v2023.1.6

### Added
- ability to send plain strings with `NostrTemplate`
- ability to customize error messages
- nip11: add icon field to relay document
- nip6: wrap identity in own class
- example(shell): add command `identity`
- example(shell): add command `persona`

### Breaking
- rename public event interceptor classes
- nip1: adhere to error message prefix

## [0.0.1] - 2024-04-25

### Added
- Initial release

[Unreleased]: https://github.com/theborakompanioni/nostr-spring-boot-starter/compare/0.0.1...HEAD
[0.0.1]: https://github.com/theborakompanioni/nostr-spring-boot-starter/releases/tag/0.0.1
