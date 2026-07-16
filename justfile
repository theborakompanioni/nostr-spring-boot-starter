# This justfile requires https://github.com/casey/just

# run example applications
import 'examples.just'

# Load environment variables from `.env` file.
set dotenv-load
# Fail the script if the env file is not found.
set dotenv-required

project_dir := justfile_directory()

# print available targets
[group("project-agnostic")]
default:
    @just --list --justfile {{justfile()}}

# evaluate and print all just variables
[group("project-agnostic")]
evaluate:
    @just --evaluate

# print system information such as OS and architecture
[group("project-agnostic")]
system-info:
    @echo "architecture: {{arch()}}"
    @echo "os: {{os()}}"
    @echo "os family: {{os_family()}}"

# clean (remove) the build artifacts
[group("development")]
clean:
    @./gradlew clean

# compile the project
[group("development")]
build:
    @./gradlew build -x test

# list dependency tree of this project
[group("development")]
dependencies:
    @./gradlew dependencyTree

# run unit tests
[group("development")]
test:
    @./gradlew test

# run integration tests
[group("development")]
test-integration *args='':
    @./gradlew integrationTest --no-parallel {{args}}

# run integration tests  (with `--rerun-tasks`)
[group("development")]
test-integration-force *args='':
    @just test-e2e --rerun-tasks {{args}}

# run end-to-end tests
[group("development")]
test-e2e *args='':
    @./gradlew e2eTest --no-parallel

# run end-to-end tests (with `--rerun-tasks`)
[group("development")]
test-e2e-force *args='':
    @just test-e2e --rerun-tasks {{args}}

# run all tests
[group("development")]
test-all *args='':
    @./gradlew test integrationTest e2eTest --no-parallel {{args}}

# run all tests (with `--rerun-tasks`)
[group("development")]
test-all-force *args='':
    @just test-all --rerun-tasks {{args}}

# build javadocs
[group("development")]
javadoc:
    @./gradlew javadoc -PjavadocEnabled

# update metadata for dependency verification
[group("development")]
update-verification:
    @./gradlew dependencies --write-verification-metadata pgp,sha256 --export-keys --write-locks

# check style
[group("development")]
checkstyle:
    @./gradlew checkstyleMain checkstyleTest checkstyleIntegTest checkstyleE2eTest

# spot bugs
[group("development")]
spotbugs:
    @./gradlew spotbugsMain spotbugsTest spotbugsIntegTest spotbugsE2eTest

# lint files
[group("development")]
lint:
    @./gradlew autoLintGradle --no-parallel
