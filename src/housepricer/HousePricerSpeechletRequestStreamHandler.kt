package housepricer

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler
import okhttp3.OkHttpClient
import java.util.*

class HousePricerSpeechletRequestStreamHandler :
        SpeechletRequestStreamHandler(HousePricerSpeechlet(ZestimateService(OkHttpClient(),
                                                                            EnvService())),
                                      HousePricerSpeechletRequestStreamHandler.supportedApplicationIds) {
    companion object {
        private val supportedApplicationIds: Set<String>

        init {
            /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
            supportedApplicationIds = HashSet<String>()
            // supportedApplicationIds.add("amzn1.echo-sdk-ams.app.[unique-value-here]");
        }
    }

}
