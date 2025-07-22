package saketh.linkora.localization.data.repository

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import saketh.linkora.localization.availableLanguages
import saketh.linkora.localization.domain.model.info.AvailableLanguageDTO
import saketh.linkora.localization.domain.model.info.LocalizedInfoDTO
import saketh.linkora.localization.domain.repository.LocalizationRepo

class LocalizationRepoImpl : LocalizationRepo {
    val json = Json { isLenient = true }

    private fun retrieveRawFileString(languageCode: String): String {
        return this::class.java.getResource("/raw/$languageCode.json").readText()
    }

    override fun getTranslationsFor(languageCode: String): String {
        return retrieveRawFileString(languageCode = languageCode).substringAfter("---").trim()
    }

    override suspend fun getLocalizedInfo(): LocalizedInfoDTO {
        return LocalizedInfoDTO(
            availableLanguages = availableLanguages.map {
                val currentLanguageCode = it.substringBefore(".").trim()
                val localizedStringsCount = Json.decodeFromString<Map<String, String>>(
                    string = getTranslationsFor(currentLanguageCode)
                ).filter {
                    it.value.isNotBlank()
                }.size
                AvailableLanguageDTO(
                    localizedName = retrieveRawFileString(languageCode = currentLanguageCode).substringBefore("---")
                        .trim().substringAfter("localizedName").substringAfter(":").substringBefore("\n").trim(),
                    languageCode = currentLanguageCode,
                    localizedStringsCount = localizedStringsCount
                )
            }, totalDefaultValues = getLatestKeysWithDefaultValues().size, lastUpdatedOn = "09-02-2025::11:06 PM IST"
        )
    }

    // this is terrible
    override suspend fun getLatestKeysWithDefaultValues(): Map<String, String> {
        return HttpClient(CIO).use { httpClient ->
            httpClient.get("https://raw.githubusercontent.com/LinkoraApp/Linkora/master/composeApp/src/commonMain/kotlin/com/sakethh/linkora/common/Localization.kt")
                .bodyAsText().substringAfter("/***** THE SACRED SCRAPING RITUAL BEGINS *****/").substringBefore(
                    "/*****  SCRAPING RITUAL COMPLETE  *****/"
                ).trimIndent().split("),").map {
                    it.trim().run {
                        if (this[this.lastIndex] == ')') this.substring(0, this.lastIndex) else this
                    }
                }.associate {
                    it.substringBefore("(") to json.decodeFromString<String>(
                        it.substringAfter("(").substringAfter("defaultValue").substringAfter("=").trim()
                            .replace("\\'", "'")
                    ).replace("\${LinkoraPlaceHolder.First.value}", "{#LINKORA_PLACE_HOLDER_1#}")
                        .replace("\${LinkoraPlaceHolder.Second.value}", "{#LINKORA_PLACE_HOLDER_2#}")
                }
        }
    }

    override suspend fun getLatestKeyValuePairsForALanguage(languageCode: String): Result<Map<String, String>> {
        return HttpClient(CIO).use { httpClient ->
            try {
                httpClient.get("https://raw.githubusercontent.com/LinkoraApp/localization-server/master/src/main/resources/raw/$languageCode.json")
                    .bodyAsText().run {
                        Result.success(Json.decodeFromString(this.substringAfter("---").trim()))
                    }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
