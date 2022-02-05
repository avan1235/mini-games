# Mini Games

## Set up the project

Project contains two parts - client app and the server app.

[Intellij Ultimate](https://www.jetbrains.com/idea/download/) is highly
recommended when working with server app. To develop Android part,
download the [Android Studio](https://developer.android.com/studio).

In both cases, open main project directory when importing the project (
not the `android-app`, `desktop-app` nor `server`) as the main directory contains the
configuration common to both projects and has to be loaded.

Additionally, the best approach seems to have two copies of project to
open them in two different IDEs (Intellij and Android Studio). It's
caused by the fact that the IDEs use the same directory for configuration
and their configuration may be misunderstood by the other.

**Notice:**
If you have any problems with importing the project, search in any IDE
for `Gradle JVM` with the search action (`Ctrl + Shift + A` or `Cmd + Shift + A`)
and set the `Gradle JVM` value to some Java 11 instance (that can be downloaded
from the IDE during selection process).

**Remember:**
When working with project use **Rebase** strategy as long as you can
not to create extra merge commits. You'll be asked by the IDE probably
during first push when some other developer also made changes.

## Run application

### Server

#### Run from terminal

The easiest way to run the instance of server is to start it from terminal by running
Gradle `./gradlew server:run` from the root directory of project.

#### Run from Intellij

To run the server from the Intellij, go to the `Server` class and run the `main`
function (starting database manually before).

**Notice:**
You need to have [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) plugin
downloaded and configured in order to run the server with the environment specified
in project file [.env](./.env). Download it and follow the
[usage instruction](https://github.com/Ashald/EnvFile#usage)
to add project file to the run configuration of entrypoint to the program.

#### Compile to JAR file

Just run `./gradlew stage` to build the server distribution. Build
result will be located in `server/build/libs/`. You can run it with `java -jar`
command, but you have to manually set the needed env variables to make application
working.

## Project structure

Projects is configured as Gradle modules that may depend on each other.
E.g. it contains module that is compiled to `.jar` distribution file of server
for the application and some other module that can be compiled to Android `.apk`
file.

### Application modules

- `build-src` - contains configuration of build logic (it's compiled before Gradle build of the other modules even starts)
- `shared` - contains common logic for `server` and `shared-client` like domain model or rest api model, depends on `build-src`
- `shared-client` - contains common logic for `android-app` (and future `ios-app`) like rest client, depends on `build-src` and `shared`
- `android-app` - targets Android, depends on `shared-client`
- `desktop-app` - targets any JVM desktop, depends on `shared-client`
- `server` - targets JVM, depends on `build-src` and `shared`

### Extra directories

- `dev-env` - useful tools that **should** be used by developers that are working
locally to test their current code and solutions
  - docker environment with PostgreSQL database can be run with `start.sh`
  - `http` contains http requests definitions that can be run from Intellij. They're
  configured by the `http-client.env.json` to switch between different environments
