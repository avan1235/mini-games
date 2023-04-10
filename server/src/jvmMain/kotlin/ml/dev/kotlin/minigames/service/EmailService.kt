package ml.dev.kotlin.minigames.service

import ml.dev.kotlin.minigames.util.envVar
import net.axay.simplekotlinmail.delivery.mailerBuilder
import net.axay.simplekotlinmail.delivery.send
import net.axay.simplekotlinmail.email.emailBuilder

object EmailService {

    private val from by lazy { envVar<String>("EMAIL_USERNAME") }

    private val mailer by lazy {
        mailerBuilder(
            host = envVar("EMAIL_HOST"),
            port = envVar("EMAIL_PORT"),
            username = envVar("EMAIL_USERNAME"),
            password = envVar("EMAIL_PASSWORD"),
        )
    }

    suspend fun send(email: String, subject: String, text: String) {
        emailBuilder {
            from(from)
            to(email)
            withSubject(subject)
            withHTMLText(text)
        }.send(mailer)
    }
}
