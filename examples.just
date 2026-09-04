# This justfile requires https://github.com/casey/just

# run "nostr-agentic" example application
[group("example")]
run-example-nostr-agentic profiles='development':
    @just --justfile examples/nostr-agentic-example-application/justfile run {{profiles}}

# run "nostr-shell" example application
[group("example")]
run-example-nostr-shell profiles='development':
    @SPRING_PROFILES_ACTIVE={{profiles}} \
      ./gradlew \
      --no-daemon \
      --console=plain \
      -p examples/nostr-shell-example-application \
      bootRun

# run "nostr-relay" example application
[group("example")]
run-example-nostr-relay port='7000' profiles='development':
    @SPRING_PROFILES_ACTIVE={{profiles}} \
      ./gradlew -p examples/nostr-relay-example-application \
        bootRun --args="--server.port={{port}}"
