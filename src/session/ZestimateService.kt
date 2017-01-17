package session

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory


open class ZestimateService(val client : OkHttpClient, val env : EnvService) {

    open fun fetch(address: String, zip: String) : Int? {
        val url : HttpUrl = HttpUrl.Builder()
                .scheme("http")
                .host("www.zillow.com")
                .addEncodedPathSegments("webservice/GetSearchResults.htm")
                .addEncodedQueryParameter("zws-id", env.getZwsId())
                .addEncodedQueryParameter("address", address)
                .addEncodedQueryParameter("citystatezip", zip)
                .build()

        println(url.toString())

        val request : Request = Request.Builder()
                .url(url)
                .build()


        val response : Response = client.newCall(request).execute()

        val responseString = response.body().string()

        val factory : DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(responseString.byteInputStream())

        val xPathfactory = XPathFactory.newInstance()
        val xpath = xPathfactory.newXPath()
        val xpathStr = "//zestimate/amount/text()"
        val expr = xpath.compile(xpathStr)
        val node : Any = expr.evaluate(doc, XPathConstants.NODE) ?: return null

        val nodeStruct : Node = node as Node
        val homeValue : Int? = nodeStruct.textContent.toInt()

        if(homeValue == null) {
            println("Zip code used: $zip")
            println("Address used: $address")
            println("Logging response body: $responseString")
        }

        return homeValue
    }
}