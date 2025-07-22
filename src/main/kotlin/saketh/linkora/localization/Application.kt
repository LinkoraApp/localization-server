package saketh.linkora.localization

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import saketh.linkora.localization.data.repository.LocalizationRepoImpl
import saketh.linkora.localization.domain.repository.LocalizationRepo
import saketh.linkora.localization.routing.configureRouting

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

val availableLanguages: List<String> =
    object {}.javaClass.getResource("/raw/availableLanguages.txt").readText().split(",").filter {
        it.isNotBlank()
    }

val DefaultJSONConfig = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}

fun Application.module() {
    install(ContentNegotiation) {
        json(DefaultJSONConfig)
    }
    val localizationRepo: LocalizationRepo = LocalizationRepoImpl()
    configureRouting(localizationRepo)
}
