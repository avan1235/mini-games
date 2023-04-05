# Mini Games

[![Release](https://github.com/avan1235/mini-games/actions/workflows/release.yml/badge.svg)](https://github.com/avan1235/mini-games/releases/latest)
[![Docker](https://github.com/avan1235/mini-games/actions/workflows/push-docker-image.yml/badge.svg)](https://hub.docker.com/repository/docker/avan1235/mini-games/tags?ordering=last_updated)

## Project description

You can find project description in [Essay](ESSAY.md).

## Project presentation

You can watch Mini Games android client in action (on [YouTube](https://www.youtube.com/watch?v=sPgB5PpPs6w))

https://user-images.githubusercontent.com/11787040/230045385-157c20ca-183b-4b15-8e1b-1c8d5ecf2f7e.mp4

as well as see how the project can be compiled locally and executed with desktop and android (qemu) clients in the same time (on [YouTube](https://www.youtube.com/watch?v=kvV8ffqI1WQ))

https://user-images.githubusercontent.com/11787040/230044938-f243df3d-ecc7-47ce-b056-227405fc889b.mp4

## Download and run application

Android released version is available on
[Google Play](https://play.google.com/store/apps/details?id=ml.dev.kotlin.minigames).

<a href='https://play.google.com/store/apps/details?id=ml.dev.kotlin.minigames&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'/></a>

You can download compiled version of application from
[release page](https://github.com/avan1235/mini-games/releases).

Client applications are configured to work with the server deployed on test server.
You can download them to play with "production" version of application - to run it locally,
you should compile server and clients by yourself.

Please note that for running unsigned version of macOS application, you need to temporarily
disable Gatekeeper, so executing command
```shell
sudo xattr -dr com.apple.quarantine  /Applications/MiniGames.app
```
is required to be able to run the app on macOS. You can learn more about this
[here](https://web.archive.org/web/20230318124537/https://disable-gatekeeper.github.io/).

To install Linux version run:

```shell
sudo dpkg -i  minigames.deb
```

You can try playing with default login credentials which are publicly available:

- user: **user**
- password: **pass**

You need a fast internet connection to have a good play experience in _SnakeIO_ game.

## Set up the project

Project contains two parts - client app and the server app.

[Intellij Ultimate](https://www.jetbrains.com/idea/download/) is highly
recommended when working with server app.

In both cases, open main project directory when importing the project (
not the `android-app`, `desktop-app` nor `server`) as the main directory contains the
configuration common to both projects and has to be loaded.

**Remember:**
When working with project use **Rebase** strategy as long as you can
not to create extra merge commits. You'll be asked by the IDE probably
during first push when some other developer also made changes.

## Compile and run application

To compile client application you need Android SDK as well as JDK 15 (corretto version was used).

### Server

### Environment

There is a docker environment configured in [docker-compose.yml](dev-env/docker-compose.yml)
required to run server application. It contains Postgres database for server data.
You can start it with

```shell
./start.sh
```

on linux or some equivalent commands on other platform.

#### Run from terminal

The easiest way to run the instance of server is to start it from terminal by running
Gradle

```shell
./gradlew server:run
```

from the root directory of project.

#### Run from Intellij

To run the server from the Intellij, go to the `Server` class and run the `main`
function (starting database manually before).

**Notice:**
You need to have environment variables loaded to build configuration to start
application correctly. The easiest way is to copy the content of [.env](.env)
file and paste it in the build configuration in Intellij as committed version
contains the definition of developer environment.

#### Compile to JAR file

Just run `./gradlew stage` to build the server distribution. Build
result will be located in `server/build/libs/`. You can run it with `java -jar`
command, but you have to manually set the needed env variables to make application
working.

### Desktop

#### Run from terminal

The easiest way to run the instance of desktop client is to start it from terminal by running
Gradle

```shell
./gradlew desktop-app:runDistributable
```

from the root directory of project.

### Android

#### Run from Intellij

The easiest way to run the instance of android client is to start it from Android Studio by running
Gradle the generated build configuration `android-app`. It should build the debug version of application
and deploy it on available emulator of android device. This is the only advised method of running the
client application for android platform.

## Project structure

Projects is configured as Gradle modules that may depend on each other.
E.g. it contains module that is compiled to `.jar` distribution file of server
for the application and some other module that can be compiled to Android `.apk`
file.

### Application modules

- `build-src` - contains configuration of build logic (it's compiled before Gradle build of the other modules even
  starts)
- `shared` - contains common logic for `server` and `shared-client` like domain model or rest api model, depends
  on `build-src`
- `shared-client` - contains common logic for `android-app` (and future `ios-app`) like rest client, depends
  on `build-src` and `shared`
- `android-app` - targets Android, depends on `shared-client`
- `desktop-app` - targets any JVM desktop, depends on `shared-client`
- `server` - targets JVM, depends on `build-src` and `shared`

### Extra directories

- `dev-env` - useful tools that **should** be used by developers that are working
  locally to test their current code and solutions
  - docker environment with Postgres database can be run with `start.sh`
  - `http` contains http requests definitions that can be run from Intellij. They're
    configured by the `http-client.env.json` to switch between different environments
