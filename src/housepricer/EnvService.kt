package housepricer

open class EnvService {
    open fun getZwsId() : String? {
        return System.getenv()["ZWS_ID"]
    }
}