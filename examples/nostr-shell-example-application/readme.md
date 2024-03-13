nostr-shell-example-application
===

A simple Nostr shell example application, e.g. to "mine" notes (NIP-13).

## Build
```shell
./gradlew -p examples/nostr-shell-example-application bootJar
```

## Run
```shell
./examples/nostr-shell-example-application/build/libs/nostr-shell-example-application-<$version>-boot.jar
```

## Example
```shell
./examples/nostr-shell-example-application/build/libs/nostr-shell-example-application-0.1.0-dev-boot.jar 
nostr:>help
AVAILABLE COMMANDS

Built-In Commands
       help: Display help about available commands
       stacktrace: Display the full stacktrace of the last error.
       clear: Clear the shell screen.
       quit, exit: Exit the shell.
       history: Display or save the history of previously run commands
       version: Show version info
       script: Read and execute commands from a file.

Commands
       pow: Generate NIP-13 Proof of Work Notes
```

```shell
nostr:>pow --target 15 --json "{ \"kind\": 1, \"content\":\"GM!\" }"
{"id":"000112b43cd320aac4090a8aea6ff587cd690e951c12f985a18fa47dabe4224f","pubkey":"","created_at":1710363515,"kind":1,"tags":[["nonce","984","15"]],"content":"GM!","sig":""}
nostr:>pow --target 24 --json "{ \"kind\": 1, \"content\":\"GM!\" }"
{"id":"0000003ab4d37414fd72a009505ef11f98c1ce9c3af6a918235041f3530250ce","pubkey":"","created_at":1710363805,"kind":1,"tags":[["nonce","651487","24"]],"content":"GM!","sig":""}
nostr:>pow --target 25 --json "{ \"kind\": 1, \"content\":\"GM!\", \"tags\": [[ \"expiration\", \"1710378232\" ]] }"
{"id":"00000009af73d28a49db7f6047229cca1da09a46180b46129f2ca5a0a1f43a07","pubkey":"","created_at":1710368995,"kind":1,"tags":[["expiration","1710378232"],["nonce","189309","25"]],"content":"GM!","sig":""}
```
