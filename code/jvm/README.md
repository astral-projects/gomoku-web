## Run Comands

> [!NOTE]
> This project uses [Gradle](https://gradle.org/) as a build tool.
>
> The commands mentioned in this doc correspond to Gradle tasks, which can be found in
> the [build.gradle.kts](build.gradle.kts) file.

### Table of contents

- [Run the project](#run-the-project)
- [Run tests](#run-tests)

### Run the project

> [!IMPORTANT]
> In order to launch the solution, [Docker Compose](https://docs.docker.com/compose/) needs to be installed.

To run the project services, run the following command:

```bash
./gradlew composeUp
```

To stop the project services, run the following command:

```bash
./gradlew composeDown
```

### Run tests

To run all tests, run the following command:

```bash
./gradlew check
```