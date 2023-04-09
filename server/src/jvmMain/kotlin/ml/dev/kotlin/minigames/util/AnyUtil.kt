@file:Suppress("NOTHING_TO_INLINE")

package ml.dev.kotlin.minigames.util

import java.security.MessageDigest

fun String.sha256(): String = MessageDigest
        .getInstance("SHA-256")
        .digest(toByteArray())
        .joinToString("") { "%02x".format(it) }
