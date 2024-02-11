[![License](https://img.shields.io/github/license/theborakompanioni/nostr-spring-boot-starter.svg?maxAge=2592000)](https://github.com/theborakompanioni/nostr-spring-boot-starter/blob/master/LICENSE)

nostr-spring-boot-starter
===

Work in progress.

- [x] [NIP-1](https://github.com/nostr-protocol/nips/blob/master/01.md)
- [x] [NIP-6](https://github.com/nostr-protocol/nips/blob/master/06.md)
- [x] [NIP-10](https://github.com/nostr-protocol/nips/blob/master/10.md)
- [x] [NIP-40](https://github.com/nostr-protocol/nips/blob/master/40.md)

Planned:
- [ ] [NIP-9](https://github.com/nostr-protocol/nips/blob/master/09.md)
- [ ] [NIP-11](https://github.com/nostr-protocol/nips/blob/master/11.md)
- [ ] [NIP-19](https://github.com/nostr-protocol/nips/blob/master/19.md)
- [ ] [NIP-38](https://github.com/nostr-protocol/nips/blob/master/38.md)
- [ ] [NIP-45](https://github.com/nostr-protocol/nips/blob/master/45.md)

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
