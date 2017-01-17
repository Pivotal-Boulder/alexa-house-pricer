package session

import com.amazon.speech.slu.Intent
import com.amazon.speech.speechlet.*
import com.amazon.speech.ui.PlainTextOutputSpeech
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.whenever
import io.damo.aspen.Test
import okhttp3.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import java.util.*


class ZestimateServiceTest : Test({
    val mockHttpClient: OkHttpClient = mock()
    val mockEnvService : EnvService = mock()
    val mockHttpCall : Call = mock()

    before {
        reset(mockEnvService, mockHttpClient)
    }

    describe("fetch") {
        test("happy path") {
            val xml = """<?xml version="1.0" encoding="utf-8"?>
<SearchResults:searchresults
        xsi:schemaLocation="http://www.zillow.com/static/xsd/SearchResults.xsd http://www.zillowstatic.com/vstatic/33d8134/static/xsd/SearchResults.xsd"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:SearchResults="http://www.zillow.com/static/xsd/SearchResults.xsd">
    <request>
        <address>2503 9th Street</address>
        <citystatezip>80304</citystatezip>
    </request>
    <message>
        <text>Request successfully processed</text>
        <code>0</code>
    </message>
    <response>
        <results>
            <result>
                <zpid>13180479</zpid>
                <links>
                    <homedetails>http://www.zillow.com/homedetails/2503-9th-St-Boulder-CO-80304/13180479_zpid/
                    </homedetails>
                    <graphsanddata>
                        http://www.zillow.com/homedetails/2503-9th-St-Boulder-CO-80304/13180479_zpid/#charts-and-data
                    </graphsanddata>
                    <mapthishome>http://www.zillow.com/homes/13180479_zpid/</mapthishome>
                    <comparables>http://www.zillow.com/homes/comps/13180479_zpid/</comparables>
                </links>
                <address>
                    <street>2503 9th St</street>
                    <zipcode>80304</zipcode>
                    <city>Boulder</city>
                    <state>CO</state>
                    <latitude>40.023413</latitude>
                    <longitude>-105.285906</longitude>
                </address>
                <zestimate>
                    <amount currency="USD">925719</amount>
                    <last-updated>01/14/2017</last-updated>
                    <oneWeekChange deprecated="true"></oneWeekChange>
                    <valueChange duration="30" currency="USD">-4050</valueChange>
                    <valuationRange>
                        <low currency="USD">833147</low>
                        <high currency="USD">1009034</high>
                    </valuationRange>
                    <percentile>0</percentile>
                </zestimate>
                <localRealEstate>
                    <region name="Mapleton Hill" id="416092" type="neighborhood">
                        <zindexValue>984,700</zindexValue>
                        <links>
                            <overview>http://www.zillow.com/local-info/CO-Boulder/Mapleton-Hill/r_416092/</overview>
                            <forSaleByOwner>http://www.zillow.com/mapleton-hill-boulder-co/fsbo/</forSaleByOwner>
                            <forSale>http://www.zillow.com/mapleton-hill-boulder-co/</forSale>
                        </links>
                    </region>
                </localRealEstate>
            </result>
        </results>
    </response>
</SearchResults:searchresults>"""

            val responseBody = ResponseBody.create(MediaType.parse("application/xml"), xml)
            val request = Request.Builder().get().url("http://example.com").build()
            val response : Response = Response.Builder()
                    .code(200)
                    .body(responseBody)
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .build()
            whenever(mockEnvService.getZwsId()).thenReturn("zws-id-1")
            whenever(mockHttpClient.newCall(anyOrNull())).thenReturn(mockHttpCall)
            whenever(mockHttpCall.execute()).thenReturn(response)

            val service : ZestimateService = ZestimateService(mockHttpClient, mockEnvService)

            assertThat(service.fetch("2503 9th St", "80304")).isEqualTo(925719)
        }

        test("no home value") {
            val xml = """<?xml version="1.0" encoding="utf-8"?>
<SearchResults:searchresults
        xsi:schemaLocation="http://www.zillow.com/static/xsd/SearchResults.xsd http://www.zillowstatic.com/vstatic/33d8134/static/xsd/SearchResults.xsd"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:SearchResults="http://www.zillow.com/static/xsd/SearchResults.xsd">
    <request>
        <address>2503 9th Street</address>
        <citystatezip>80304</citystatezip>
    </request>
    <message>
        <text>Request successfully processed</text>
        <code>0</code>
    </message>
    <response>
        <results>
            <result>
                <zpid>13180479</zpid>
                <links>
                    <homedetails>http://www.zillow.com/homedetails/2503-9th-St-Boulder-CO-80304/13180479_zpid/
                    </homedetails>
                    <graphsanddata>
                        http://www.zillow.com/homedetails/2503-9th-St-Boulder-CO-80304/13180479_zpid/#charts-and-data
                    </graphsanddata>
                    <mapthishome>http://www.zillow.com/homes/13180479_zpid/</mapthishome>
                    <comparables>http://www.zillow.com/homes/comps/13180479_zpid/</comparables>
                </links>
                <address>
                    <street>2503 9th St</street>
                    <zipcode>80304</zipcode>
                    <city>Boulder</city>
                    <state>CO</state>
                    <latitude>40.023413</latitude>
                    <longitude>-105.285906</longitude>
                </address>
                <zestimate>
                    <amount currency="USD"></amount>
                    <last-updated>01/14/2017</last-updated>
                    <oneWeekChange deprecated="true"></oneWeekChange>
                    <valueChange duration="30" currency="USD">-</valueChange>
                    <valuationRange>
                        <low currency="USD"></low>
                        <high currency="USD"></high>
                    </valuationRange>
                    <percentile>0</percentile>
                </zestimate>
                <localRealEstate>
                    <region name="Mapleton Hill" id="416092" type="neighborhood">
                        <zindexValue>984,700</zindexValue>
                        <links>
                            <overview>http://www.zillow.com/local-info/CO-Boulder/Mapleton-Hill/r_416092/</overview>
                            <forSaleByOwner>http://www.zillow.com/mapleton-hill-boulder-co/fsbo/</forSaleByOwner>
                            <forSale>http://www.zillow.com/mapleton-hill-boulder-co/</forSale>
                        </links>
                    </region>
                </localRealEstate>
            </result>
        </results>
    </response>
</SearchResults:searchresults>"""

            val responseBody = ResponseBody.create(MediaType.parse("application/xml"), xml)
            val request = Request.Builder().get().url("http://example.com").build()
            val response : Response = Response.Builder()
                    .code(200)
                    .body(responseBody)
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .build()
            whenever(mockEnvService.getZwsId()).thenReturn("zws-id-1")
            whenever(mockHttpClient.newCall(anyOrNull())).thenReturn(mockHttpCall)
            whenever(mockHttpCall.execute()).thenReturn(response)

            val service : ZestimateService = ZestimateService(mockHttpClient, mockEnvService)

            assertThat(service.fetch("2503 9th St", "80304")).isEqualTo(null)
        }
    }
})