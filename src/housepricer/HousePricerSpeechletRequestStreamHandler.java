package housepricer;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import okhttp3.OkHttpClient;

import java.util.HashSet;
import java.util.Set;

public class HousePricerSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds;

    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds = new HashSet<String>();
        // supportedApplicationIds.add("amzn1.echo-sdk-ams.app.[unique-value-here]");
    }

    public HousePricerSpeechletRequestStreamHandler() {
        super(new HousePricerSpeechlet(new ZestimateService(new OkHttpClient(), new EnvService())), supportedApplicationIds);
    }

}
