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

### Interactive

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
       persona: Generate nostr personas
       identity: Generate a nostr key pair
       pow: Generate NIP-13 Proof of Work Notes
```

#### `pow`
```shell
nostr:>pow --target 15 --json "{ \"kind\": 1, \"content\":\"GM!\" }"
{"id":"000112b43cd320aac4090a8aea6ff587cd690e951c12f985a18fa47dabe4224f","pubkey":"","created_at":1710363515,"kind":1,"tags":[["nonce","984","15"]],"content":"GM!","sig":""}
nostr:>pow --target 24 --json "{ \"kind\": 1, \"content\":\"GM!\" }"
{"id":"0000003ab4d37414fd72a009505ef11f98c1ce9c3af6a918235041f3530250ce","pubkey":"","created_at":1710363805,"kind":1,"tags":[["nonce","651487","24"]],"content":"GM!","sig":""}
nostr:>pow --target 25 --json "{ \"kind\": 1, \"content\":\"GM!\", \"tags\": [[ \"expiration\", \"1710378232\" ]] }"
{"id":"00000009af73d28a49db7f6047229cca1da09a46180b46129f2ca5a0a1f43a07","pubkey":"","created_at":1710368995,"kind":1,"tags":[["expiration","1710378232"],["nonce","189309","25"]],"content":"GM!","sig":""}
```

#### `persona`
```shell
nostr:>persona --name alice
{
  "entropy" : "2bd806c97f0e00af1a1fc3328fa763a9",
  "mnemonic" : "cloth scan rather wrap theme fiscal half wear crater large suggest fancy",
  "keyPath" : "m/44'/1237'/0'/0/0",
  "privateKey" : "7eaab2f5e9359badb538722e23e6e65bb0c8265a707d317ec4b132ccd23aeb72",
  "publicKey" : "f319269a8757e84e9b6dad9325cb74933f64e9497c4c3a8f7757361e78edf564",
  "nsec" : "nsec1064t9a0fxkd6mdfcwghz8ehxtwcvsfj6wp7nzlkykyeve536adeqjksgqj",
  "npub" : "npub17vvjdx582l5yaxmd4kfjtjm5jvlkf62f03xr4rmh2umpu78d74jqxhkuj6"
}
```

#### `identity`
```shell
nostr:>identity
{
  "privateKey" : "1f503559eb276c40c8f476c8f486d971a26a99b4505cf483826ed2695339ead3",
  "publicKey" : "daed4eb7f731ba35c576ed1bb4cfd2d43964a5206a3b841d4998b436de9b4d4c",
  "nsec" : "nsec1ragr2k0tyakypj85wmy0fpkewx3x4xd52pw0fquzdmfxj5eeatfs47wjtq",
  "npub" : "npub1mtk5adlhxxart3tka5dmfn7j6sukfffqdgacg82fnz6rdh5mf4xq6tlxsh"
}
```

### Non-interactive
```shell
./examples/nostr-shell-example-application/build/libs/nostr-shell-example-application-0.1.0-dev-boot.jar pow --target 25 --parallelism 8 --json '{ \"kind\": 1, \"content\":\"GM!\", \"tags\": [[ \"expiration\", \"1710378232\" ]] }'
{"id":"000000469cab0be2c76b8585c081ce3ad84897cdd50e9348b2b0b75a82f4d7aa","pubkey":"","created_at":1710441044,"kind":1,"tags":[["expiration","1710378232"],["nonce","163290","25","2"]],"content":"GM!","sig":""}
```