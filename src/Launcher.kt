/**
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

 * http://aws.amazon.com/apache2.0/

 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

import com.amazon.speech.Sdk
import com.amazon.speech.speechlet.Speechlet
import com.amazon.speech.speechlet.servlet.SpeechletServlet
import housepricer.EnvService
import housepricer.HousePricerSpeechlet
import housepricer.ZestimateService
import okhttp3.OkHttpClient
import org.eclipse.jetty.server.*
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

/**
 * Shared launcher for executing all sample skills within a single servlet container.
 */
object Launcher {
    /**
     * port number for the jetty server.
     */
    private val PORT = 8888

    /**
     * Security scheme to use.
     */
    private val HTTPS_SCHEME = "https"

    /**
     * Main entry point. Starts a Jetty server.

     * @param args
     * *            ignored.
     * *
     * @throws Exception
     * *             if anything goes wrong.
     */
    @Throws(Exception::class)
    @JvmStatic fun main(args: Array<String>) {
        // Configure server and its associated servlets
        val server = Server()
        val sslConnectionFactory = SslConnectionFactory()
        val sslContextFactory = sslConnectionFactory.sslContextFactory
        sslContextFactory.keyStorePath = System.getProperty("javax.net.ssl.keyStore")
        sslContextFactory.setKeyStorePassword(System.getProperty("javax.net.ssl.keyStorePassword"))
        sslContextFactory.setIncludeCipherSuites(*Sdk.SUPPORTED_CIPHER_SUITES)

        val httpConf = HttpConfiguration()
        httpConf.securePort = PORT
        httpConf.secureScheme = HTTPS_SCHEME
        httpConf.addCustomizer(SecureRequestCustomizer())
        val httpConnectionFactory = HttpConnectionFactory(httpConf)

        val serverConnector = ServerConnector(server, sslConnectionFactory, httpConnectionFactory)
        serverConnector.port = PORT

        val connectors = arrayOfNulls<Connector>(1)
        connectors[0] = serverConnector
        server.connectors = connectors

        val context = ServletContextHandler(ServletContextHandler.SESSIONS)
        context.contextPath = "/"
        server.handler = context
        val envService = EnvService()
        context.addServlet(ServletHolder(createServlet(HousePricerSpeechlet(ZestimateService(OkHttpClient(), envService)))), "/src/housepricer")
        server.start()
        server.join()
    }

    private fun createServlet(speechlet: Speechlet): SpeechletServlet {
        val servlet = SpeechletServlet()
        servlet.speechlet = speechlet
        return servlet
    }
}
/**
 * default constructor.
 */
