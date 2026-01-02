
# Publishing

Signing artifacts
```sh
./gradlew clean sign -Psigning.password=secret
```

In your local `~/.gradle/gradle.properties` you need
```properties
signing.keyId=ABCDEF01
#signing.password=secret - must be provided via command line
signing.secretKeyRingFile=/home/user/.gnupg/secring.gpg
```

### Local Staging Deploy Repository
e.g. to publish to your local staging deploy repository (in your build directory):
```sh
./gradlew publishNebulaPublicationToStagingDeployRepository -Prelease.version=1.0.0-SNAPSHOT
```
```sh
./gradlew publishNebulaPublicationToStagingDeployRepository -Prelease.useLastTag=true
```

### Local Maven Repository
e.g. to publish to your local maven repository:
```sh
./gradlew publishNebulaPublicationToMavenLocal -Prelease.stage=SNAPSHOT -Prelease.scope=patch
```
```sh
./gradlew publishNebulaPublicationToMavenLocal -Prelease.useLastTag=true
```


## Resources
- Signing Plugin: https://docs.gradle.org/current/userguide/signing_plugin.html#signing_plugin
- Nebula Publishing Plugin: https://github.com/nebula-plugins/nebula-publishing-plugin
- https://discuss.gradle.org/t/how-to-publish-artifacts-signatures-asc-files-using-maven-publish-plugin/7422/24
