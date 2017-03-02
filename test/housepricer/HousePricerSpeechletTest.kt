package housepricer

import com.amazon.speech.slu.Intent
import com.amazon.speech.slu.Slot
import com.amazon.speech.speechlet.*
import com.amazon.speech.ui.PlainTextOutputSpeech
import com.amazon.speech.ui.SimpleCard
import com.amazon.speech.ui.SsmlOutputSpeech
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.damo.aspen.Test
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.helpers.NOPLogger
import java.util.*


class HousePricerSpeechletTest : Test({
    val mockZestimateService: ZestimateService = mock()
    val housePricerSpeechlet: HousePricerSpeechlet = HousePricerSpeechlet(mockZestimateService, NOPLogger.NOP_LOGGER)
    val session: Session = Session.builder()
            .withSessionId("session-id-1")
            .withUser(User
                    .builder()
                    .withUserId("user-id-1")
                    .build())
            .withIsNew(true)
            .build()

    before {
        reset(mockZestimateService)
    }

    describe("welcome") {
        test("launchSession") {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, 1988)
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val timestamp = cal.time

            val speechletResponse: SpeechletResponse = housePricerSpeechlet.onLaunch(LaunchRequest.builder()
                    .withRequestId("request-id-1")
                    .withTimestamp(timestamp)
                    .build(), session)

            val repromptText: SsmlOutputSpeech = speechletResponse.reprompt.outputSpeech as SsmlOutputSpeech
            val outputText: SsmlOutputSpeech = speechletResponse.outputSpeech as SsmlOutputSpeech

            assertThat(outputText.ssml).contains("Welcome to House Pricer")
            assertThat(outputText.ssml).contains("my zip code is")
            assertThat(outputText.ssml).contains("<say-as interpret-as=\"digits\">55434</say-as>")

            assertThat(repromptText.ssml).doesNotContain("Welcome")
            assertThat(repromptText.ssml).contains("I need a few pieces of information")
            assertThat(repromptText.ssml).contains("my zip code is")
            assertThat(repromptText.ssml).contains("<say-as interpret-as=\"digits\">55434</say-as>")

            val simpleCard: SimpleCard = speechletResponse.card as SimpleCard
            assertThat(simpleCard.content).doesNotContain("<speak")
        }

        test("welcome intent") {
            val intent: Intent = Intent.builder()
                    .withName("WelcomeIntent")
                    .build()
            val intentRequest: IntentRequest = IntentRequest.builder()
                    .withRequestId("request-id-1")
                    .withIntent(intent)
                    .build()

            val speechletResponse: SpeechletResponse = housePricerSpeechlet.onIntent(intentRequest, session)

            val repromptText: SsmlOutputSpeech = speechletResponse.reprompt.outputSpeech as SsmlOutputSpeech
            val outputText: SsmlOutputSpeech = speechletResponse.outputSpeech as SsmlOutputSpeech

            assertThat(outputText.ssml).contains("Welcome to House Pricer")
            assertThat(outputText.ssml).contains("my zip code is")
            assertThat(outputText.ssml).contains("<say-as interpret-as=\"digits\">55434</say-as>")

            assertThat(repromptText.ssml).doesNotContain("Welcome")
            assertThat(repromptText.ssml).contains("I need a few pieces of information")
            assertThat(repromptText.ssml).contains("my zip code is")
            assertThat(repromptText.ssml).contains("<say-as interpret-as=\"digits\">55434</say-as>")

            val simpleCard: SimpleCard = speechletResponse.card as SimpleCard
            assertThat(simpleCard.content).doesNotContain("<speak")
        }

        test("stop and cancel commands") {
            val intent: Intent = Intent.builder()
                    .withName("AMAZON.StopIntent")
                    .build()
            val intentRequest: IntentRequest = IntentRequest.builder()
                    .withRequestId("request-id-1")
                    .withIntent(intent)
                    .build()

            val speechletResponse: SpeechletResponse = housePricerSpeechlet.onIntent(intentRequest, session)

            val outputText: PlainTextOutputSpeech = speechletResponse.outputSpeech as PlainTextOutputSpeech

            assertThat(speechletResponse.shouldEndSession).isTrue()
            assertThat(outputText.text).contains("Goodbye!")
        }
    }

    describe("zip code") {

        test("happy path") {
            val zipSlot = Slot.builder().withName("Zip").withValue("55434").build()
            val slots: MutableMap<String, Slot> = mapOf("Zip" to zipSlot) as MutableMap<String, Slot>
            val intent: Intent = Intent.builder()
                    .withName("MyZipCodeIsIntent")
                    .withSlots(slots)
                    .build()
            val intentRequest: IntentRequest = IntentRequest.builder()
                    .withRequestId("request-id-1")
                    .withIntent(intent)
                    .build()

            val speechletResponse: SpeechletResponse = housePricerSpeechlet.onIntent(intentRequest, session)

            val repromptText: PlainTextOutputSpeech = speechletResponse.reprompt.outputSpeech as PlainTextOutputSpeech
            val outputText: SsmlOutputSpeech = speechletResponse.outputSpeech as SsmlOutputSpeech

            assertThat(session.attributes).containsKey("ZIP")
            assertThat(session.attributes["ZIP"]).isEqualTo(zipSlot.value)

            assertThat(outputText.ssml).contains("Thanks for that")
            assertThat(outputText.ssml).contains("please tell me your street address")
            assertThat(outputText.ssml).contains("my address is 101 Fairfield Drive")

            assertThat(repromptText.text).doesNotContain("Thanks for that")
            assertThat(repromptText.text).contains("I need your address to get your house price")
            assertThat(repromptText.text).contains("my address is 101 Fairfield Drive")

            val simpleCard: SimpleCard = speechletResponse.card as SimpleCard
            assertThat(simpleCard.content).doesNotContain("<speak")
        }

        test("no zip code failure") {
            val sessionNoZip: Session = Session.builder()
                    .withSessionId("session-id-2")
                    .withUser(User
                            .builder()
                            .withUserId("user-id-2")
                            .build())
                    .withIsNew(true)
                    .build()

            val zipSlot = Slot.builder().withName("Zip").build()
            val slots: MutableMap<String, Slot> = mapOf("Zip" to zipSlot) as MutableMap<String, Slot>

            val intent: Intent = Intent.builder()
                    .withName("MyZipCodeIsIntent")
                    .withSlots(slots)
                    .build()

            val intentRequest: IntentRequest = IntentRequest.builder()
                    .withRequestId("request-id-2")
                    .withIntent(intent)
                    .build()

            val speechletResponse: SpeechletResponse = housePricerSpeechlet.onIntent(intentRequest, sessionNoZip)

            val repromptText: SsmlOutputSpeech = speechletResponse.reprompt.outputSpeech as SsmlOutputSpeech
            val outputText: SsmlOutputSpeech = speechletResponse.outputSpeech as SsmlOutputSpeech

            assertThat(sessionNoZip.attributes).doesNotContainKey("ZIP")

            assertThat(outputText.ssml).contains("I didn't understand that")
            assertThat(outputText.ssml).contains("You can tell me your zip")
            assertThat(outputText.ssml).contains("my zip code is")
            assertThat(outputText.ssml).contains("<say-as interpret-as=\"digits\">55434</say-as>")

            assertThat(repromptText.ssml).doesNotContain("I didn't understand that")
            assertThat(repromptText.ssml).contains("I'm not sure what your zip code is")
            assertThat(repromptText.ssml).contains("You can tell me your zip")
            assertThat(repromptText.ssml).contains("my zip code is")
            assertThat(repromptText.ssml).contains("<say-as interpret-as=\"digits\">55434</say-as>")

            val simpleCard: SimpleCard = speechletResponse.card as SimpleCard
            assertThat(simpleCard.content).doesNotContain("<say-as")
        }
    }

    describe("address") {

        test("happy path") {
            whenever(mockZestimateService.fetch("9901 Polk St", "55434")).thenReturn(100000)

            val addressSlot = Slot.builder().withName("Address").withValue("9901 Polk St").build()
            val slots: MutableMap<String, Slot> = mapOf("Address" to addressSlot) as MutableMap<String, Slot>
            val intent: Intent = Intent.builder()
                    .withName("MyAddressIsIntent")
                    .withSlots(slots)
                    .build()
            val intentRequest: IntentRequest = IntentRequest.builder()
                    .withRequestId("request-id-1")
                    .withIntent(intent)
                    .build()

            val speechletResponse: SpeechletResponse = housePricerSpeechlet.onIntent(intentRequest, session)

            val outputText: PlainTextOutputSpeech = speechletResponse.outputSpeech as PlainTextOutputSpeech

            verify(mockZestimateService).fetch("9901 Polk St", "55434")

            assertThat(session.attributes).containsKey("ADDRESS")
            assertThat(session.attributes["ADDRESS"]).isEqualTo(addressSlot.value)

            assertThat(outputText.text).contains("Thanks for that")
            assertThat(outputText.text).contains("According to zillow, your house is worth")
            assertThat(outputText.text).contains("Goodbye")
        }

        test("no price available on zillow") {
            whenever(mockZestimateService.fetch("9901 Polk St", "55434")).thenReturn(null)

            val addressSlot = Slot.builder().withName("Address").withValue("9901 Polk St").build()
            val slots: MutableMap<String, Slot> = mapOf("Address" to addressSlot) as MutableMap<String, Slot>
            val intent: Intent = Intent.builder()
                    .withName("MyAddressIsIntent")
                    .withSlots(slots)
                    .build()
            val intentRequest: IntentRequest = IntentRequest.builder()
                    .withRequestId("request-id-1")
                    .withIntent(intent)
                    .build()

            val speechletResponse: SpeechletResponse = housePricerSpeechlet.onIntent(intentRequest, session)

            val outputText: PlainTextOutputSpeech = speechletResponse.outputSpeech as PlainTextOutputSpeech

            verify(mockZestimateService).fetch("9901 Polk St", "55434")

            assertThat(session.attributes).containsKey("ADDRESS")
            assertThat(session.attributes["ADDRESS"]).isEqualTo(addressSlot.value)

            assertThat(outputText.text).contains("Thanks for that")
            assertThat(outputText.text).contains("Unfortunately zillow does not have an estimate for the address 9901 Polk St.")
            assertThat(outputText.text).contains("Goodbye")
        }

        test("no address failure") {
            val sessionNoAddress: Session = Session.builder()
                    .withSessionId("session-id-2")
                    .withUser(User
                            .builder()
                            .withUserId("user-id-2")
                            .build())
                    .withIsNew(true)
                    .build()

            val addressSlot = Slot.builder().withName("Address").build()
            val slots: MutableMap<String, Slot> = mapOf("Address" to addressSlot) as MutableMap<String, Slot>
            val intent: Intent = Intent.builder()
                    .withName("MyAddressIsIntent")
                    .withSlots(slots)
                    .build()
            val intentRequest: IntentRequest = IntentRequest.builder()
                    .withRequestId("request-id-1")
                    .withIntent(intent)
                    .build()

            val speechletResponse: SpeechletResponse = housePricerSpeechlet.onIntent(intentRequest, sessionNoAddress)

            val outputText: PlainTextOutputSpeech = speechletResponse.outputSpeech as PlainTextOutputSpeech

            assertThat(sessionNoAddress.attributes).doesNotContainKey("ADDRESS")

            assertThat(outputText.text).contains("I didn't understand that")
            assertThat(outputText.text).contains("You can tell me your address")
            assertThat(outputText.text).contains("my address is 101 Fairfield Drive")

            assertThat(speechletResponse.reprompt).isNotNull()

            val repromptText: PlainTextOutputSpeech = speechletResponse.reprompt.outputSpeech as PlainTextOutputSpeech

            assertThat(repromptText.text).doesNotContain("I didn't understand that")
            assertThat(repromptText.text).contains("I'm not sure what your address is")
            assertThat(repromptText.text).contains("You can tell me your address")
            assertThat(repromptText.text).contains("my address is 101 Fairfield Drive")
        }
    }
})
