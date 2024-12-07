[![Build](https://github.com/theborakompanioni/nostr-spring-boot-starter/actions/workflows/build.yml/badge.svg)](https://github.com/theborakompanioni/nostr-spring-boot-starter/actions/workflows/build.yml)
[![GitHub Release](https://img.shields.io/github/release/theborakompanioni/nostr-spring-boot-starter.svg?maxAge=3600)](https://github.com/theborakompanioni/nostr-spring-boot-starter/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.theborakompanioni/nostr-proto.svg?maxAge=3600)](https://search.maven.org/#search|g%3A%22io.github.theborakompanioni%22)
[![License](https://img.shields.io/github/license/theborakompanioni/nostr-spring-boot-starter.svg?maxAge=2592000)](https://github.com/theborakompanioni/nostr-spring-boot-starter/blob/master/LICENSE)


<p align="center">
    <img src="https://github.com/theborakompanioni/nostr-spring-boot-starter/blob/master/docs/assets/images/logo.png" alt="Logo" width="255" />
</p>


nostr-spring-boot-starter
===

Spring boot starter projects for building [Nostr](https://github.com/nostr-protocol/nostr) applications.
Whether you're building your own client or custom relay software, this framework provides most of what you
need to write scalable and efficient solutions effortlessly.

**Note**: Most code is still experimental - **this is work in progress**.

- [x] [NIP-1](https://github.com/nostr-protocol/nips/blob/master/01.md)
- [x] [NIP-6](https://github.com/nostr-protocol/nips/blob/master/06.md)
- [x] [NIP-9](https://github.com/nostr-protocol/nips/blob/master/09.md)
- [x] [NIP-10](https://github.com/nostr-protocol/nips/blob/master/10.md)
- [x] [NIP-13](https://github.com/nostr-protocol/nips/blob/master/13.md)
- [x] [NIP-18](https://github.com/nostr-protocol/nips/blob/master/18.md)
- [x] [NIP-19](https://github.com/nostr-protocol/nips/blob/master/19.md)
- [x] [NIP-21](https://github.com/nostr-protocol/nips/blob/master/21.md)
- [x] [NIP-24](https://github.com/nostr-protocol/nips/blob/master/24.md)
- [x] [NIP-25](https://github.com/nostr-protocol/nips/blob/master/25.md)
- [x] [NIP-30](https://github.com/nostr-protocol/nips/blob/master/30.md)
- [x] [NIP-38](https://github.com/nostr-protocol/nips/blob/master/38.md)
- [x] [NIP-40](https://github.com/nostr-protocol/nips/blob/master/40.md)
- [x] [NIP-42](https://github.com/nostr-protocol/nips/blob/master/42.md)
- [x] [NIP-50](https://github.com/nostr-protocol/nips/blob/master/50.md)
- [x] [NIP-65](https://github.com/nostr-protocol/nips/blob/master/65.md)

Planned:
- [ ] [NIP-02](https://github.com/nostr-protocol/nips/blob/master/02.md)
- [ ] [NIP-11](https://github.com/nostr-protocol/nips/blob/master/11.md)
- [ ] [NIP-45](https://github.com/nostr-protocol/nips/blob/master/45.md)
- [ ] [NIP-64](https://github.com/nostr-protocol/nips/blob/master/64.md)
- [ ] [NIP-70](https://github.com/nostr-protocol/nips/blob/master/70.md)


### `nostr-proto`

See [nostr-proto](./nostr/nostr-proto/src/main/proto/event.proto) for protobuf definitions of core nostr concepts used in all modules.

```protobuf
message Event {
  bytes id = 1 [json_name = "id"];
  bytes pubkey = 2 [json_name = "pubkey"];
  uint64 created_at = 3 [json_name = "created_at"];
  uint32 kind = 4 [json_name = "kind"];
  repeated TagValue tags = 5 [json_name = "tags"];
  string content = 6 [json_name = "content"];
  bytes sig = 7 [json_name = "sig"];
}

[...]
```


## Table of Contents

- [Install](#install)
- [Examples](#examples)
- [Development](#development)
- [Contributing](#contributing)
- [Resources](#resources)
- [License](#license)


## Install

[Download](https://search.maven.org/#search|g%3A%22io.github.theborakompanioni%22) from Maven Central.

### Gradle
```groovy
repositories {
    mavenCentral()
}
```

```groovy
implementation "io.github.theborakompanioni:nostr-proto:${bitcoinSpringBootStarterVersion}"
```


## Examples

[This project contains various examples](examples/) that are stand-alone applications showing basic usage of the functionality provided.

| Application                                                | Description                                                                      |
|------------------------------------------------------------|----------------------------------------------------------------------------------|
| [nostr-client](nostr-client-example-application/readme.md) | A simple Nostr client example application that subscribes to all kind `1` notes. |
| [nostr-relay](nostr-relay-example-application/readme.md)   | A simple Nostr relay example application.                                        |
| [nostr-shell](nostr-shell-example-application/readme.md)   | A simple Nostr shell example application, e.g. to "mine" notes (NIP-13).         |


## Development

### Requirements
- java >=21
- docker

### Build
```shell script
./gradlew build -x test
```

### Test
```shell script
./gradlew test integrationTest --rerun-tasks --no-parallel
```

Run full test suite (including load tests):
```shell script
CI=true ./gradlew test integrationTest e2eTest --rerun-tasks --no-parallel
```

## Contributing
All contributions and ideas are always welcome. For any question, bug or feature request,
please create an [issue](https://github.com/theborakompanioni/nostr-spring-boot-starter/issues).
Before you start, please read the [contributing guidelines](contributing.md).

## Resources

- nostr (GitHub): https://github.com/nostr-protocol/nostr
- NIPs (GitHub): https://github.com/nostr-protocol/nips

---

- nostr.com: https://nostr.com
- nostr Relay Registry: https://nostr-registry.netlify.app
- awesome-nostr (GitHub): https://github.com/aljazceru/awesome-nostr
- protocol-buffers: https://developers.google.com/protocol-buffers/docs/proto3#json

## License

The project is licensed under the Apache License. See [LICENSE](LICENSE) for details.
