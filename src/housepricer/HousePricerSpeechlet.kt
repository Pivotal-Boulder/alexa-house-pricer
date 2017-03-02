package housepricer

import com.amazon.speech.slu.Intent
import com.amazon.speech.speechlet.*
import com.amazon.speech.ui.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class HousePricerSpeechlet(val zestService: ZestimateService,
                           val log : Logger = LoggerFactory.getLogger(HousePricerSpeechlet::class.java)) : Speechlet {

    @Throws(SpeechletException::class)
    override fun onSessionStarted(request: SessionStartedRequest, session: Session) {
        log.info("onSessionStarted requestId={}, sessionId={}", request.requestId,
                session.sessionId)
    }

    @Throws(SpeechletException::class)
    override fun onLaunch(request: LaunchRequest, session: Session): SpeechletResponse {
        log.info("onLaunch requestId={}, sessionId={}", request.requestId,
                session.sessionId)
        return welcomeResponse
    }

    @Throws(SpeechletException::class)
    override fun onIntent(request: IntentRequest, session: Session): SpeechletResponse {
        val intent = request.intent
        val intentName = intent?.name

        log.info("onIntent requestId={}, sessionId={}, intentName={}", request.requestId,
                session.sessionId, intentName)

        if ("MyZipCodeIsIntent" == intentName) {
            return setZipInSession(intent, session)
        } else if ("WelcomeIntent" == intentName) {
            return welcomeResponse
        } else if ("MyAddressIsIntent" == intentName) {
            return setAddressInSession(intent, session)
        } else if ("AMAZON.StopIntent" == intentName) {
            return stopResponse
        } else if ("AMAZON.CancelIntent" == intentName) {
            return stopResponse
        } else {
            println("Invalid intent: $intentName")
            throw SpeechletException("Invalid Intent")
        }
    }


    @Throws(SpeechletException::class)
    override fun onSessionEnded(request: SessionEndedRequest, session: Session) {
        log.info("onSessionEnded requestId={}, sessionId={}", request.requestId,
                session.sessionId)
    }


    private val welcomeResponse: SpeechletResponse
        get() {
            val speechText = """<speak>
                Welcome to House Pricer. Please tell me your house zip code by saying, my zip code is <say-as interpret-as="digits">55434</say-as>
                </speak>"""
            val repromptText = """<speak>
                            I need a few pieces of information to get your house price. Please tell me your house zip code by saying, my zip code is <say-as interpret-as="digits">55434</say-as>
                            </speak>"""

            return getSpeechletResponse(speechText, repromptText, true)
        }

    private val stopResponse: SpeechletResponse
        get() {
            val speechText = "Goodbye!"
            val speech = PlainTextOutputSpeech()
            speech.text = speechText

            val endSessionTell = SpeechletResponse.newTellResponse(speech)
            endSessionTell.shouldEndSession = true
            return endSessionTell
        }

    private fun setZipInSession(intent: Intent, session: Session): SpeechletResponse {

        fun generalFailure(): SpeechletResponse {
            val speechText = """<speak>
            I didn't understand that. You can tell me your zip code by saying, my zip code is <say-as interpret-as="digits">55434</say-as>
            </speak>"""
            val repromptText = """<speak>
                    I'm not sure what your zip code is. You can tell me your zip code by saying, my zip code is <say-as interpret-as="digits">55434</say-as>
                    </speak>"""
            return getSpeechletResponse(speechText, repromptText, true)
        }

        val slots = intent.slots
        val zipCodeSlot = slots[ZIP_SLOT]
        val speechText: String
        val repromptText: String

        if (zipCodeSlot != null && zipCodeSlot.value != null) {
            val zipCode = zipCodeSlot.value
            session.setAttribute(ZIP_KEY, zipCode)

            speechText = """<speak>
            Thanks for that. Now that I know your zip code is <say-as interpret-as="digits">$zipCode</say-as>,
            please tell me your street address.
            You can tell me your address by saying, my address is 101 Fairfield Drive
            </speak>
            """

            repromptText = "I need your address to get your house price. You can tell me your address by saying, my address is 101 Fairfield Drive"
        } else {
            return generalFailure()
        }

        return getSpeechletResponse(speechText, repromptText, true)
    }

    private fun setAddressInSession(intent: Intent, session: Session): SpeechletResponse {
        val slots = intent.slots
        val addressSlot = slots[ADDRESS_SLOT]
        val speechText: String

        val zipAny: Any? = session.getAttribute(ZIP_KEY)
        val zip: String? = zipAny as String?

        if (addressSlot != null && addressSlot.value != null) {
            val address = addressSlot.value
            session.setAttribute(ADDRESS_KEY, address)


            if (zip != null) {
                val houseValue: Int? = zestService.fetch(address, zip)

                if (houseValue == null) {
                    speechText = """
                            Thanks for that. Unfortunately zillow does not have an estimate for the address $address. Goodbye.
                            """
                } else {
                    speechText = """
                                Thanks for that. According to zillow, your house is worth $houseValue dollars. Goodbye.
                                """

                }
            } else {
                speechText = """<speak>
                            Thanks for that. Please tell me your house zip code by saying, my zip code is <say-as interpret-as="digits">55434</say-as>"
                            </speak>"""
                val repromptText: String = """<speak>
                I'm not sure what your zip code is. You can tell me your zip code by saying, my zip code is <say-as interpret-as="digits">55434</say-as>"
                </speak>
                """

                return getSpeechletResponse(speechText, repromptText, true)
            }
        } else {
            speechText = "I didn't understand that. You can tell me your address by saying, my address is 101 Fairfield Drive."
            val repromptText: String = "I'm not sure what your address is. You can tell me your address by saying, my address is 101 Fairfield Drive."
            return getSpeechletResponse(speechText, repromptText, true)
        }

        return getSpeechletResponse(speechText, "", false)
    }

    private fun getSpeechletResponse(speechText: String, repromptText: String,
                                     isAskResponse: Boolean): SpeechletResponse {
        // Create the Simple card content.
        val card = SimpleCard()
        card.title = "House Pricer"
        card.content = speechText.replace(Regex("<[^>]*>"), "")

        val speech: OutputSpeech
        if (speechText.contains("<speak>")) {
            speech = SsmlOutputSpeech()
            speech.ssml = speechText
        } else {
            speech = PlainTextOutputSpeech()
            speech.text = speechText
        }

        if (isAskResponse) {
            val repromptSpeech: OutputSpeech
            if (repromptText.contains("<speak>")) {
                repromptSpeech = SsmlOutputSpeech()
                repromptSpeech.ssml = repromptText
            } else {
                repromptSpeech = PlainTextOutputSpeech()
                repromptSpeech.text = repromptText
            }

            val reprompt = Reprompt()
            reprompt.outputSpeech = repromptSpeech


            return SpeechletResponse.newAskResponse(speech, reprompt, card)

        } else {
            return SpeechletResponse.newTellResponse(speech, card)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(HousePricerSpeechlet::class.java)

        private val ZIP_KEY = "ZIP"
        private val ADDRESS_KEY = "ADDRESS"
        private val ZIP_SLOT = "Zip"
        private val ADDRESS_SLOT = "Address"
    }
}
