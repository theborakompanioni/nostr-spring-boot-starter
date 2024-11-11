# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Breaking
- NostrTemplate: change signature of method `sendPlain`
- NostrClientService: change return value of method `attachTo`

## [0.0.2] - 2024-11-01

### Added
- nip11: add icon field to relay document
- nip6: wrap identity in own class
- nip24: add bot property to metadata
- nip5: add nip05 and lud16 property to metadata
- nip42: add auth request/response to nostr-proto
- ability to send plain strings with `NostrTemplate`
- ability to customize error messages
- example(shell): add command `identity`
- example(shell): add command `persona`

### Breaking
- rename public event interceptor classes
- nip1: adhere to error message prefix

### Changed
- upgrade: update spring-boot from v3.2.1 to v3.3.4
- upgrade: update lightning-kmp from v1.7.3 to v1.8.4
- upgrade: update jmolecules-bom from v2023.1.3 to v2023.1.6

## [0.0.1] - 2024-04-25

### Added
- Initial release

[Unreleased]: https://github.com/theborakompanioni/nostr-spring-boot-starter/compare/0.0.2...HEAD
[0.0.2]: https://github.com/theborakompanioni/nostr-spring-boot-starter/releases/tag/0.0.2...0.0.1
[0.0.1]: https://github.com/theborakompanioni/nostr-spring-boot-starter/releases/tag/0.0.1
