package session

import com.amazon.speech.speechlet.SpeechletException

open class EnvService {
    open fun getZwsId() : String? {
        return System.getenv()["ZWS_ID"]
    }
}