# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- nip44: Encrypted messages

### Fixed
- fix: correctly handle events without tags in tag filter

## [0.3.0] - 2025-04-09
### Added
- plugin: allowlist from properties
- plugin: allowlist from database

### Changed
- upgrade: update spring-boot from v3.4.0 to v3.4.4
- upgrade: update spring-shell from v3.3.3 to v3.4.0

## [0.2.0] - 2025-01-24
### Breaking
- MoreEvents: rename method `eventId` to `calculateEventId`
- MoreIdentities: remove methods `fromHex` and `of`
- Nip40: remove method `getExpiration` (use `findExpiration`)
- externalize modules `nostr-proto` and `nostr-proto-json`

### Added
- Nip40: add methods `findExpiration` and `expire`

### Changed
- upgrade: update gradle from v8.7 to v8.12

## [0.1.0] - 2024-12-21
### Added
- nip25: Reactions
- nip30: Custom Emoji
- nip38: User Statuses

### Breaking
- nip40: rename method from `expiration` to `expirationTag`

### Changed
- upgrade: update spring-boot from v3.3.4 to v3.4.0

## [0.0.4] - 2024-12-05
### Added
- nip18: repost events
- nip19: encode/decode `nprofile`, `nevent`, `naddr`
- nip21: `nostr:` URI scheme
- nip65: Relay List Metadata
- persona: add new module `nostr-persona`
- NostrTemplate: ability to fetch relay list by author
- Default connection timeouts for `NostrTemplate` and `NostrClientService` implementations
- Validation of relay URL in standard tags
- Validation of 'k' tag
- Utility class with named kind constants

### Breaking
- RelayUri: change signature of method `of(String)` and rename to `parse`
- EventUri: change signature of method `fromString(String)` and rename to `parse`
- NostrClientService: change return value of method `subscribe`
- NostrClientService: add method `subscribeToEvents`
- nip19: move from `nostr-core` to module `nostr-nip19`
- nip19: rename method from `from*` to `decode*`
- nip19: rename methods from `to*` to `encode*`
- nip10: remove `tag` method from markers

## [0.0.3] - 2024-11-12
### Breaking
- NostrTemplate: change signature of method `sendPlain` and rename to `publishPlain`
- NostrClientService: change return value of method `attachTo`
- Nip9: change return type of method `kind` from `int` to `Kind`
- "parameterized replaceable" events are now called "addressable" events

### Added
- Ability to easily send auth event with `NostrTemplate`
- nip42: authentication of clients to relays

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

[Unreleased]: https://github.com/theborakompanioni/nostr-spring-boot-starter/compare/0.3.0...HEAD
[0.3.0]: https://github.com/theborakompanioni/nostr-spring-boot-starter/releases/tag/0.3.0...0.2.0
[0.2.0]: https://github.com/theborakompanioni/nostr-spring-boot-starter/releases/tag/0.2.0...0.1.0
[0.1.0]: https://github.com/theborakompanioni/nostr-spring-boot-starter/releases/tag/0.1.0...0.0.4
[0.0.4]: https://github.com/theborakompanioni/nostr-spring-boot-starter/releases/tag/0.0.4...0.0.3
[0.0.3]: https://github.com/theborakompanioni/nostr-spring-boot-starter/releases/tag/0.0.3...0.0.2
[0.0.2]: https://github.com/theborakompanioni/nostr-spring-boot-starter/releases/tag/0.0.2...0.0.1
[0.0.1]: https://github.com/theborakompanioni/nostr-spring-boot-starter/releases/tag/0.0.1
