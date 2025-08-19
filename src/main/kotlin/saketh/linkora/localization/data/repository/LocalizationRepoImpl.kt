package saketh.linkora.localization.data.repository

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import saketh.linkora.localization.DefaultJSONConfig
import saketh.linkora.localization.availableLanguages
import saketh.linkora.localization.domain.model.info.AvailableLanguageDTO
import saketh.linkora.localization.domain.model.info.LocalizedInfoDTO
import saketh.linkora.localization.domain.repository.LocalizationRepo

class LocalizationRepoImpl : LocalizationRepo {

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
                val localizedStringsCount = DefaultJSONConfig.decodeFromString<Map<String, String>>(
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

    override suspend fun getLatestKeysWithDefaultValues(): Map<String, String> {
        return HttpClient(CIO).use { httpClient ->
            DefaultJSONConfig.decodeFromString(
                httpClient.get("https://raw.githubusercontent.com/LinkoraApp/Linkora/master/locales/default_en.json")
                    .bodyAsText()
            )
        }
    }

    override suspend fun getLatestKeyValuePairsForALanguage(languageCode: String): Result<Map<String, String>> {
        return HttpClient(CIO).use { httpClient ->
            try {
                httpClient.get("https://raw.githubusercontent.com/LinkoraApp/localization-server/master/src/main/resources/raw/$languageCode.json")
                    .bodyAsText().run {
                        Result.success(DefaultJSONConfig.decodeFromString(this.substringAfter("---").trim()))
                    }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
