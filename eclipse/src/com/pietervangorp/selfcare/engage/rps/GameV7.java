package com.pietervangorp.selfcare.engage.rps;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import com.google.gson.Gson;
import com.pietervangorp.selfcare.engage.rps.ai.AutomaticPlayer;
import com.pietervangorp.selfcare.engage.rps.ai.AutomaticPlayerV2;
import com.pietervangorp.selfcare.engage.rps.items.Item;
import com.pietervangorp.selfcare.engage.vo.Settings;
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
 * Reasonably realistic rock/paper/scissors game: taking one player input 
 * and comparing it against a random computer pick until one of both wins.
 * 
 * Opposed to V3, in this variant the human is playing against the AutomaticPlayerV2,
 * so the human can predict the behavior of the bot (since that one makes predictable counter-attacks)
 * 
 * @author pvgorp
 *
 */
public class GameV7 {

  private static final Logger logger = Logger.getLogger(GameV7.class.getName());
  
  private Settings settings;
  
  private String appKey="";
  
  public GameV7() {
      try {
          settings = new Gson().fromJson(
                  new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("Settings.json"))),
                  Settings.class);
          logger.info("API base: "+settings.getSelfcareApiBaseURL());
          
          logger.info("Getting app key");
          
          if ("".equals(settings.getSelfcareApiUser())) { // no API usn/pass, then use statically provided app key
              appKey=settings.getSelfcareAppKey();
              if ("".equals(appKey)) {
                  logger.log(Level.SEVERE, "No API credentials, so cannot connect to Selfcare Engage");
                  return; // or system exit, or throw run-time error
              }
          } else {
              AuthTokenBody body= new AuthTokenBody();
              body.setUsername(settings.getSelfcareApiUser());
              body.setPassword(settings.getSelfcareApiPassword());
              
              HttpPost request = new HttpPost(settings.getSelfcareApiBaseURL()+"/api/auth/token");
              request.addHeader("content-type", "application/json");

              try {
                  StringEntity entity = new StringEntity(new Gson().toJson(body), "utf-8");
                  entity.setContentEncoding("UTF-8");
                  entity.setContentType("application/json");
                  request.setEntity(entity);
                  
                  // HttpClient httpClient = HttpClientBuilder.create().build();
                  HttpClient httpClient= hack.httpClientTrustingAllSSLCerts();
                  HttpResponse httpResponse = httpClient.execute(request);
                  if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    String minitorJson = EntityUtils.toString(httpResponse.getEntity());
                    AuthTokenResponse response = new Gson().fromJson(minitorJson, AuthTokenResponse.class);
                    logger.info("Token: "+response.getAccess_token());
                  }
                } catch (Exception e) {
                  throw e;
                }
          }
          
          if (settings.getConsent()==null || !settings.getConsent().isApproved() || "".equals(settings.getConsent().getConsentURL()) ) {              
              // TODO: get URL via CLI (preferably via helper method)
              // TODO: insert here
              // Settings.Consent c= new Settings.Consent(false, url);
              // settings.setConsent(c);
          }
              
              
      } catch (Exception e) {
          logger.log(Level.SEVERE, "Other error", e);
      }
  }
  
  public void doGame() {
      String howto = "Please enter once one of {paper,rock,scissors} to play"; 
      System.out.println(howto);
      Scanner s = new Scanner(System.in);
      Item i1 = null;
      Item i2 = null;
      AutomaticPlayer player2= new AutomaticPlayerV2(); // ONLY DIFFERENCE TO V3 
      GameResult result = GameResult.UNDECIDED;
      int round= 1;
      do {
        System.out.println("Round "+round++);
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
  }
  
  private enum GameResult {
    UNDECIDED, P1WON, P2WON, TIE
  }

  public static void main(String[] args) {
    GameV7 game= new GameV7();
    game.doGame();
  }
  
  
  /**
   * Insecure work-around for bypassing the Selfcare SSL certificate issue on the test server
   * Source: https://stackoverflow.com/questions/2308774/httpget-with-https-sslpeerunverifiedexception
   */
  private HttpClientTrustingAllCertsTest hack= new HttpClientTrustingAllCertsTest();
  
  public class HttpClientTrustingAllCertsTest {

      @Test
      public void shouldAcceptUnsafeCerts() throws Exception {
          DefaultHttpClient httpclient = httpClientTrustingAllSSLCerts();
          HttpGet httpGet = new HttpGet("https://host_with_self_signed_cert");
          HttpResponse response = httpclient.execute( httpGet );
          assertEquals("HTTP/1.1 200 OK", response.getStatusLine().toString());
      }

      private DefaultHttpClient httpClientTrustingAllSSLCerts() throws NoSuchAlgorithmException, KeyManagementException {
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