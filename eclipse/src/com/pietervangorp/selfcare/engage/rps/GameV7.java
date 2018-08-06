package com.pietervangorp.selfcare.engage.rps;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import com.google.gson.Gson;
import com.pietervangorp.selfcare.engage.exceptions.NoConsentException;
import com.pietervangorp.selfcare.engage.rps.ai.AutomaticPlayer;
import com.pietervangorp.selfcare.engage.rps.ai.AutomaticPlayerV2;
import com.pietervangorp.selfcare.engage.rps.items.Item;
import com.pietervangorp.selfcare.engage.vo.Settings;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.ApiUrlResponse;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.AuthRequestusertokenResponse;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.AuthTokenBody;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.AuthTokenResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

/**
 * Reasonably realistic rock/paper/scissors game: taking one player input and
 * comparing it against a random computer pick until one of both wins.
 * 
 * Opposed to V3, in this variant the human is playing against the
 * AutomaticPlayerV2, so the human can predict the behavior of the bot (since
 * that one makes predictable counter-attacks)
 * 
 * Used as a demonstrator of https://integrationapi.selfcare4me.test.huss.nl/
 * 
 * @author pvgorp
 *
 */
public class GameV7 {

    private static final Logger logger = Logger.getLogger(GameV7.class.getName());

    private Settings settings;

    private String appKey = "";

    public GameV7() throws Exception {

        // String settingsFile= "Settings-Confidential.json";
        String settingsFile = "Settings.json";
        settings = new Gson().fromJson(
                new BufferedReader(new InputStreamReader(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(settingsFile))),
                Settings.class);
        logger.info("API base: " + settings.getSelfcareApiBaseURL());

        logger.info("Getting app key");

        if (settings.getSelfcareApiUser() != null && settings.getSelfcareApiUser() != "") {
            logger.info("using API usn/pass from settings file");
            AuthTokenBody body = new AuthTokenBody();
            body.setUsername(settings.getSelfcareApiUser());
            body.setPassword(settings.getSelfcareApiPassword());

            HttpPost request = new HttpPost(settings.getSelfcareApiBaseURL() + "/api/auth/token");
            request.addHeader("content-type", "application/json");

            StringEntity entity = new StringEntity(new Gson().toJson(body), "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            request.setEntity(entity);

            HttpClient httpClient = HttpClientBuilder.create().build(); //
            // FIXME: should use this one in production!!
            // HttpClient httpClient = hack.httpClientTrustingAllSSLCerts(); // FIXME:
                                                                          // never
                                                                          // use
                                                                          // this
                                                                          // in
                                                                          // production!!
            HttpResponse httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String res = EntityUtils.toString(httpResponse.getEntity());
                AuthTokenResponse response = new Gson().fromJson(res, AuthTokenResponse.class);
                appKey = response.getAccess_token();
            } else {
                logger.log(Level.SEVERE, "No App Key, so cannot connect to Selfcare Engage");
                throw new NoConsentException();
            }

        } else { // empty or null
            logger.info("no API usn/pass, then use statically provided app key");
            appKey = settings.getSelfcareAppKey();
            if ("".equals(appKey)) {
                logger.log(Level.SEVERE, "No API credentials, so cannot connect to Selfcare Engage");
                throw new NoConsentException();
            }
        }
        logger.info("App key: " + appKey);

        String consentUrl = "https://void";
        if (settings.getConsent() == null || !settings.getConsent().isApproved()
                || "".equals(settings.getConsent().getConsentURL())) {
            HttpGet request = new HttpGet(settings.getSelfcareApiBaseURL() + "/api/url");
            request.addHeader("Authorization", "Bearer " + appKey);

            HttpClient httpClient = HttpClientBuilder.create().build(); //
            // FIXME: should use this one in production!!
            // HttpClient httpClient = hack.httpClientTrustingAllSSLCerts(); // FIXME:
                                                                          // never
                                                                          // use
                                                                          // this
                                                                          // in
                                                                          // production!!
            String usercode="";
            
            HttpResponse httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String res = EntityUtils.toString(httpResponse.getEntity());
                ApiUrlResponse response = new Gson().fromJson(res, ApiUrlResponse.class);
                consentUrl = response.getUrl();
                usercode= response.getUsercode();
            } else {
                logger.log(Level.SEVERE,
                        "No consent URL, so cannot ask user to give permission to use Selfcare Engage");
                throw new NoConsentException();
            }

            System.out.println("Please go to " + consentUrl + " and give your consent via that webpage.");

            String isOK = "";
            do {
                System.out.println("Please type OK below when done.");
                Scanner sc = new Scanner(System.in);
                isOK = sc.nextLine();
            } while (!isOK.trim().equalsIgnoreCase("OK"));
            
            String url= settings.getSelfcareApiBaseURL() + "/api/auth/requestusertoken?usercode="+ usercode;
            logger.info("Getting access code via "+url);
            request = new HttpGet(url);
            request.addHeader("Authorization", "Bearer " + appKey);
       
            
            httpClient = HttpClientBuilder.create().build(); //
            // FIXME: should use this one in production!!
            // httpClient = hack.httpClientTrustingAllSSLCerts(); // FIXME: never
                                                               // use this in
                                                               // production!!
            httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String res = EntityUtils.toString(httpResponse.getEntity());
                AuthRequestusertokenResponse response = new Gson().fromJson(res, AuthRequestusertokenResponse.class);
                logger.info("Access code retrieved: "+response.getAccesstoken());
                
                settings.setSelfcareAccessToken(response.getAccesstoken());
                settings.setConsent(true, consentUrl);
            } else {
                logger.log(Level.SEVERE,
                        "You did not give proper consent. Please restart the app and do give explicit approval via the webpage at the given URL.");
                throw new NoConsentException();                
            }
        }

    }

    public void doGame() {
        
        String howto = "Please enter once one of {paper,rock,scissors} to play";
        
        // TODO compute start date/time here (to be used to compute duration of a game0
        
        System.out.println(howto);
        Scanner s = new Scanner(System.in);
        Item i1 = null;
        Item i2 = null;
        AutomaticPlayer player2 = new AutomaticPlayerV2(); // ONLY DIFFERENCE TO
                                                           // V3
        GameResult result = GameResult.UNDECIDED;
        int round = 1;
        do {
            System.out.println("Round " + round++);
            do {
                try {
                    System.out.println("Player 1: please enter your choice");
                    i1 = ItemFactory.toItem(s.next());
                    i2 = player2.play();
                    player2.considerOpponentItem(i1); // NEW
                } catch (InvalidInputException e) {
                    System.out.println(e);
                    System.out.println(howto);
                }
            } while (i1 == null || i2 == null);
            System.out.println("You selected " + i1);
            System.out.println("Your computer opponent selected " + i2);
            if (i1.beats(i2)) {
                System.out.println("You win");
                result = GameResult.P1WON;
            } else if (i2.beats(i1)) {
                System.out.println("You loose");
                result = GameResult.P2WON;
            } else {
                System.out.println("Nobody wins");
                result = GameResult.TIE;
            }
        } while (result != GameResult.P1WON && result != GameResult.P2WON);
        s.close();
        // TODO compute start date/time here (to be used to compute duration of a game0
        
        // TODO: submit duration to Selfcare engage
    }

    private enum GameResult {
        UNDECIDED, P1WON, P2WON, TIE
    }

    public static void main(String[] args) {
        try {
            GameV7 game = new GameV7();
            game.doGame();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Insecure work-around for bypassing the Selfcare SSL certificate issue on
     * the test server Source:
     * https://stackoverflow.com/questions/2308774/httpget-with-https-
     * sslpeerunverifiedexception
     */
    private HttpClientTrustingAllCertsTest hack = new HttpClientTrustingAllCertsTest();

    public class HttpClientTrustingAllCertsTest {

        @Test
        public void shouldAcceptUnsafeCerts() throws Exception {
            DefaultHttpClient httpclient = httpClientTrustingAllSSLCerts();
            HttpGet httpGet = new HttpGet("https://host_with_self_signed_cert");
            HttpResponse response = httpclient.execute(httpGet);
            assertEquals("HTTP/1.1 200 OK", response.getStatusLine().toString());
        }

        private DefaultHttpClient httpClientTrustingAllSSLCerts()
                throws NoSuchAlgorithmException, KeyManagementException {
            DefaultHttpClient httpclient = new DefaultHttpClient();

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, getTrustingManager(), new java.security.SecureRandom());

            SSLSocketFactory socketFactory = new SSLSocketFactory(sc);
            Scheme sch = new Scheme("https", 443, socketFactory);
            httpclient.getConnectionManager().getSchemeRegistry().register(sch);
            return httpclient;
        }

        private TrustManager[] getTrustingManager() {
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    // Do nothing
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    // Do nothing
                }

            } };
            return trustAllCerts;
        }
    }
}