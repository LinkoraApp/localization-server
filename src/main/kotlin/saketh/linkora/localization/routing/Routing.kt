package saketh.linkora.localization.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import kotlinx.html.unsafe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import saketh.linkora.localization.DefaultJSONConfig
import saketh.linkora.localization.availableLanguages
import saketh.linkora.localization.domain.repository.LocalizationRepo
import sakethh.kapsule.*
import sakethh.kapsule.utils.*

fun Application.configureRouting(localizationRepo: LocalizationRepo) {
    routing {
        get("/") {
            call.respondRedirect("https://github.com/sakethpathike/Linkora")
        }

        get("/info") {
            call.respond(
                DefaultJSONConfig.encodeToString(
                    localizationRepo.getLocalizedInfo()
                )
            )
        }
        get("/contribute") {
            if (call.parameters.entries().size > 1) {
                call.respond("It seems you've provided multiple parameters. Only one is allowed — either `lang` or `newLang`.")
                return@get
            }
            val latestKeysWithDefaultValuesFromTheApp = localizationRepo.getLatestKeysWithDefaultValues()
            val forANewLanguage = try {
                call.parameters["newLang"].toBoolean()
            } catch (_: Exception) {
                false
            }
            if (forANewLanguage.not()) {
                localizationRepo.getLatestKeyValuePairsForALanguage(call.parameters["lang"].toString())
            } else {
                Result.success(emptyMap())
            }.onSuccess {
                call.respondText(contentType = ContentType.Text.Html, text = createHTML().html {
                    Surface(
                        onTheHeadElement = {
                            unsafe {
                                raw(
                                    """
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                """.trimIndent()
                                )
                            }
                        },
                        fonts = listOf("https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600&display=swap"),
                        style = {
                            unsafe {
                                +"""
                #textInputField:focus {
                    border-color: #D0BCFF;
                    box-shadow: 0 0 0 2px rgba(208, 188, 255, 0.2);
                    outline: none;
                }
                #copy-btn:hover {
                    background-color: #635387 !important;
                }
                """.trimIndent()
                            }
                        },
                        modifier = Modifier().backgroundColor("#1C1B1F").padding(2.rem)
                    ) {
                        latestKeysWithDefaultValuesFromTheApp.forEach { (key, value) ->
                            Box(modifier = Modifier(), className = "translate-component") {
                                Text(
                                    text = key,
                                    color = "#CAC4D0",
                                    fontSize = 1.12.rem,
                                    fontWeight = "400",
                                    fontFamily = "Poppins",
                                    modifier = Modifier().display(Display.Block)
                                )

                                Text(
                                    text = value,
                                    color = "#D0BCFF",
                                    fontSize = 2.rem,
                                    fontWeight = "420",
                                    fontFamily = "Poppins",
                                    modifier = Modifier().display(Display.Block)
                                )

                                TextInputField(
                                    id = "textInputField",
                                    value = it[key] ?: "",
                                    fontSize = 1.rem,
                                    fontFamily = "Poppins",
                                    fontWeight = 1.px,
                                    modifier = Modifier().margin(top = 1.rem, bottom = 0.px, start = 0.px, end = 0.px)
                                        .cursor(Cursor.Pointer).boxSizing(BoxSizing.BorderBox)
                                        .border(width = 1, radius = 4, color = "rgba(208, 188, 255, 0.3)")
                                        .backgroundColor("#2D2A31").width("100%").padding(0.75.rem)
                                        .transition(TransitionBuilder().custom("all 0.3s ease")).color("white"),
                                    onThisElement = {
                                        name = key
                                    })
                            }
                            Spacer(modifier = Modifier().height(50.px))
                        }
                        Spacer(modifier = Modifier().height(150.px))
                        Button(
                            id = "copy-btn",
                            modifier = Modifier().zIndex(1000).cursor(Cursor.Pointer).padding("0.75rem 1.5rem;")
                                .border(radius = 8.px, color = "", width = 0.px).backgroundColor("#4F378B").position(
                                    Position.Fixed
                                ).transform(TransformBuilder().translate(x = 50.unaryMinus().percent)).custom(
                                    """
                                     bottom: 2rem;
                                     left: 50%;
                                     """.trimIndent()
                                ),
                            onClick = {
                                """
                                     function collectFormData() {
                                         const formData = {};

                                         document.querySelectorAll('.translate-component').forEach(component => {
                                             const keyDiv = component.querySelector('div');
                                             const field = component.querySelector('input, textarea');

                                             if (keyDiv && field) {
                                                 const key = keyDiv.textContent.trim();
                                                 formData[key] = field.value;
                                             }
                                         });

                                         return formData;
                                     }


                                     function copyToClipboard(text) {
                                         if (!navigator.clipboard) {
                                             const textarea = document.createElement('textarea');
                                             textarea.value = text;
                                             document.body.appendChild(textarea);
                                             textarea.select();
                                             try {
                                                 document.execCommand('copy');
                                                 alert('Data copied! (Fallback method)');
                                             } catch (err) {
                                                 console.log(err);
                                             }
                                             document.body.removeChild(textarea);
                                             return;
                                         }

                                         navigator.clipboard.writeText(text)
                                             .then(() => alert('Data copied to clipboard!'))
                                             .catch(() => alert('Failed to copy - please try again'));
                                     }

                                     copyToClipboard(JSON.stringify(collectFormData(), null, 2));
                                """.trimIndent()
                            }) {
                            Text(text = "Copy JSON", fontFamily = "Poppins", color = "white", fontWeight = 12.px)
                        }
                    }
                })
            }.onFailure {
                call.respond(message = it.message as Any)
            }
        }
        get("/keys") {
            call.respond(localizationRepo.getLatestKeysWithDefaultValues())
        }
        availableLanguages.map {
            it.substringBefore(".").trim()
        }.forEach { languageCode ->
            get(languageCode) {
                call.respondText(localizationRepo.getTranslationsFor(languageCode))
            }
        }
    }
}