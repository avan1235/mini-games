# Background

My name is Maciej Procyk, and I'm a Computer Science Masters student at the University of Warsaw.

I first learnt about Kotlin about 4 years ago and since then it has been my most loved
programming language. I've been programming a lot in Java, but I have a good background
in C and Python, while recently I got more familiar with Rust.

I like programming web and android applications as well as work with programming languages.
I've written the [latte language compiler](https://github.com/avan1235/latte-compiler)
in Kotlin as well as [kotlin interpreter](https://gitlab.com/avan1235/kotlin-interpreter)
in Haskell - they're my most favourite projects that I'm really proud of. I did some
contributions to open-source project [KotlinDL](https://github.com/Kotlin/kotlindl).

I've been working as Java developer for almost a year, and I've had two internships
as Kotlin developer during my studies. I've been also working as teaching assistant
at my faculty (Object-Oriented Programming classes and laboratories with students).

I created Mini Games as a great sample how Kotlin code can be reused between multiple
platforms. I didn't find any Android online version of
[Set Game](https://en.wikipedia.org/wiki/Set_(card_game)), so I decided to build it myself,
and then it become building more generic approach to have different online games.

Thanks to Compose UI it can be built for Android and Desktop with **single**
definition of view and view model as well as it has only single definition of http model and
client used to exchange data between clients and server. Thanks to this, there's no need to repeat
some model definitions for different platforms or for different parts of system.

The most useful libraries used in build the project were:

- [Jetpack Compose](https://github.com/JetBrains/compose-jb) - view layer (clients)
- [Decompose](https://github.com/arkivanov/Decompose/) - view-model layer (clients)
- [Essenty](https://github.com/arkivanov/Essenty/) - lifecycle and state managements (clients)
- [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization) - shared model serialization (server &
  clients)
- [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines) - shared approach to concurrency (server & clients)
- [Ktor](https://github.com/ktorio/ktor) - web application framework (server) and http client (clients)
- [Exposed](https://github.com/JetBrains/Exposed) with [Krush](https://github.com/TouK/krush) - database layer for
  server with auto table model generation
