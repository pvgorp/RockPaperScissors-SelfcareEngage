package com.pietervangorp.selfcare.engage.rps;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pietervangorp.selfcare.engage.exceptions.NoConsentException;
import com.pietervangorp.selfcare.engage.rps.ai.AutomaticPlayer;
import com.pietervangorp.selfcare.engage.rps.ai.AutomaticPlayerV2;
import com.pietervangorp.selfcare.engage.rps.items.Item;
import com.pietervangorp.selfcare.engage.rps.vo.RockPaperScissorsGame;
import com.pietervangorp.selfcare.engage.vo.GameSession;
import com.pietervangorp.selfcare.engage.vo.Settings;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.ApiSensortypesResponse;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.ApiUrlResponse;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.measuredvalues.ApiUserMeasuredvaluesResult;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.measuredvalues.SensorValue;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.AuthRequestusertokenResponse;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.AuthTokenBody;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.AuthTokenResponse;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.DateFormatter;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
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
 * @author git@pietervangorp.com
 *
 */
public class GameV7 {

	private static final Logger logger = Logger.getLogger(GameV7.class.getName());

	private Settings settings;

	private String appKey = "";

	public GameV7() throws Exception {

		String settingsFile = "Settings-RPS.json";
		String outputSettingsFile = "My-" + settingsFile;
		Reader reader;
		if (new File(outputSettingsFile).isFile()) {
			settingsFile = outputSettingsFile; // use file that was written by previous run
			reader = new InputStreamReader(new FileInputStream(settingsFile));
			logger.info("Using settings from " + new File(settingsFile).getAbsolutePath());
		} else {
			reader = new BufferedReader(new InputStreamReader( // read from resources folder
					Thread.currentThread().getContextClassLoader().getResourceAsStream(settingsFile)));
		}
		settings = new Gson().fromJson(reader, Settings.class);
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

			HttpClient httpClient = hack.getClient();

			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String res = EntityUtils.toString(httpResponse.getEntity());
				AuthTokenResponse response = new Gson().fromJson(res, AuthTokenResponse.class);
				appKey = response.getAccess_token();
				settings.setSelfcareAppKey(appKey);
				logger.info("App key retrieved and stored in settings file");
			} else {
				logger.log(Level.SEVERE, "No App Key, so cannot connect to Selfcare Engage");
				throw new NoConsentException();
			}

		} else { // empty or null
			logger.info("No API usn/pass, then use statically provided app key");
			appKey = settings.getSelfcareAppKey();
			if ("".equals(appKey)) {
				logger.log(Level.SEVERE, "No API credentials, so cannot connect to Selfcare Engage");
				throw new NoConsentException();
			}
		}

		String consentUrl = "https://void";
		if (settings.getConsent() == null || !settings.getConsent().isApproved()
				|| "".equals(settings.getConsent().getConsentURL())) {
			HttpGet request = new HttpGet(settings.getSelfcareApiBaseURL() + "/api/url");
			request.addHeader("Authorization", "Bearer " + appKey);

			HttpClient httpClient = hack.getClient();

			String usercode = "";

			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String res = EntityUtils.toString(httpResponse.getEntity());
				ApiUrlResponse response = new Gson().fromJson(res, ApiUrlResponse.class);
				consentUrl = response.getUrl();
				usercode = response.getUsercode();
			} else {
				logger.log(Level.SEVERE,
						"No consent URL, so cannot ask user to give permission to use Selfcare Engage");
				throw new NoConsentException();
			}

			System.out.println("Please go to " + consentUrl + " and give your consent via that webpage.");

			String isOK = "";
			Scanner sc = new Scanner(System.in);
			do {
				System.out.println("Please type OK below when done.");
				isOK = sc.nextLine();
			} while (!isOK.trim().equalsIgnoreCase("OK"));
			sc.close();

			String url = settings.getSelfcareApiBaseURL() + "/api/auth/requestusertoken?usercode=" + usercode;
			logger.info("Getting access code via " + url);
			request = new HttpGet(url);
			request.addHeader("Authorization", "Bearer " + appKey);

			httpClient = hack.getClient();

			httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String res = EntityUtils.toString(httpResponse.getEntity());
				AuthRequestusertokenResponse response = new Gson().fromJson(res, AuthRequestusertokenResponse.class);

				settings.setSelfcareAccessToken(response.getAccesstoken());
				logger.info("Access code retrieved and stored in settings file");

				settings.setConsent(true, consentUrl);

				// initializing Basic Sensor Type info				
				settings.setBasicSensorTypesOfApp(loadSensorTypesFrom(settings.getSelfcareApiBaseURL() + "/api/user/sensortypes"));
				logger.info("Basic sensor types retrieved and stored in settings file");
				
				// initializing Complex Sensor Type info
				settings.setComplexSensorTypesOfApp(loadSensorTypesFrom(settings.getSelfcareApiBaseURL() + "/api/user/datastoresensortypes"));
				logger.info("Complex sensor types retrieved and stored in settings file");			

				// now write back to file
				Writer writer = new FileWriter(outputSettingsFile);
				new Gson().toJson(settings, writer);
				writer.close();
			} else {
				logger.log(Level.SEVERE,
						"You did not give proper consent. Please restart the app and do give explicit approval via the webpage at the given URL.");
				throw new NoConsentException();
			}
		} else {
			logger.info("Leveraging previously given consent (and corresponding access code)");
		}
	}

	/**
	 * Helper method used for basic and complex sensor type loading
	 * @param url
	 * @return
	 * @throws Exception
	 */
	private ApiSensortypesResponse[] loadSensorTypesFrom(String url) throws Exception {		
		logger.info("Getting basic sensor types code via " + url);
		HttpGet request = new HttpGet(url);
		request.addHeader("Authorization", "Bearer " + settings.getSelfcareAccessToken());

		HttpClient httpClient = hack.getClient();

		HttpResponse httpResponse = httpClient.execute(request);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			String res = EntityUtils.toString(httpResponse.getEntity());
			ApiSensortypesResponse[] sensorTypes = new Gson().fromJson(res, ApiSensortypesResponse[].class);
			return sensorTypes;
		} else {
			logger.log(Level.SEVERE, "Reading of Sensor Config failed");
			throw new Exception();
		}
	}
	
	public void doGame() throws Exception {

		String howto = "Please enter once one of {paper,rock,scissors} to play";

		RockPaperScissorsGame rps = new RockPaperScissorsGame();
		rps.setStartTimeMS(System.currentTimeMillis());

		System.out.println(howto);
		Scanner s = new Scanner(System.in);
		Item i1 = null;
		Item i2 = null;
		AutomaticPlayer player2 = new AutomaticPlayerV2(); //  DIFFERENCE TO V3
		GameResult result = GameResult.UNDECIDED;
		int round = 1;

		do {
			System.out.println("Round " + round++);
			do {
				try {
					System.out.println("Player 1: please enter your choice");
					i1 = ItemFactory.toItem(s.next());
					i2 = player2.play();
					player2.considerOpponentItem(i1); // NEW IN V3
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
		Date endDate = new Date();

		rps.setEndTimeMS(System.currentTimeMillis());
		rps.setDurationMilliSeconds(rps.getEndTimeMS() - rps.getStartTimeMS());
		rps.setNumberOfIterations(round - 1);

		logger.info("Game Complete");
		// Note: do not get gameDataFileLocation from settings (it does not seem needed
		// as long as we do not want to failover potential connection problems of the
		// API)

		logger.info("Sending game result to Selfcare");
		SensorValue bodyArrElBasic = new SensorValue();
		bodyArrElBasic.setActivity("Game: Rock Paper Scissors");
		bodyArrElBasic.setSensorTypeId(settings.getSensorTypeIdByName("gewonnen?"));
		bodyArrElBasic.setTimestamp(DateFormatter.toSelfcareDateTime(endDate));

		if (result.equals(GameResult.P1WON)) {
			bodyArrElBasic.setValue("2");
		} else if (result.equals(GameResult.TIE)) {
			bodyArrElBasic.setValue("1");
		} else { // other won, or result undecided
			bodyArrElBasic.setValue("0");
		}
		SensorValue[] body = { bodyArrElBasic };
		String jsonPayload= new Gson().toJson(body);
		
		HttpResponse httpResponse = doPostForUser(jsonPayload, "/api/user/measuredvalues");
		
		switch (httpResponse.getStatusLine().getStatusCode()) {
		case 201:
			logger.info("Result sent to Selfcare");
			break;
		case 400:
			logger.log(Level.SEVERE, "Bad request (when 0 values are added)");
		case 500:
			logger.log(Level.SEVERE, "Error inside of Selfcare server...");
		default:
			throw new Exception();
		}
		
		logger.info("Sending complex game result to Selfcare");
		SensorValue bodyArrElComplex = new SensorValue();
		bodyArrElComplex.setActivity("Game: Rock Paper Scissors");
		bodyArrElComplex.setSensorTypeId(settings.getSensorTypeIdByName("Rock Paper Scissors Game Session"));
		bodyArrElComplex.setTimestamp(DateFormatter.toSelfcareDateTime(endDate));
		bodyArrElComplex.setValue(new Gson().toJson(rps));
		
		SensorValue[] bodyComplex = { bodyArrElComplex };
		String jsonPayloadComplex= new Gson().toJson(bodyComplex);
		
		HttpResponse httpResponseComplex = doPostForUser(jsonPayloadComplex, "/api/user/storedvalues");
		
		switch (httpResponseComplex.getStatusLine().getStatusCode()) {
		case 201:
			logger.info("Result sent to Selfcare");
			break;
		case 400:
			logger.log(Level.SEVERE, "Bad request (when 0 values are added)");
		case 500:
			logger.log(Level.SEVERE, "Error inside of Selfcare server...");
		default:
			throw new Exception();
		}

	}

	private HttpResponse doPostForUser(String jsonPayload, String relativeURL) throws Exception, IOException, ClientProtocolException {
		HttpPost request = new HttpPost(settings.getSelfcareApiBaseURL() + relativeURL);
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Authorization", "Bearer " + settings.getSelfcareAccessToken());		

		StringEntity entity = new StringEntity(jsonPayload, "utf-8");
		entity.setContentEncoding("UTF-8");
		entity.setContentType("application/json");
		request.setEntity(entity);

		HttpClient httpClient = hack.getClient();

		HttpResponse httpResponse = httpClient.execute(request);
		return httpResponse;
	}

	private enum GameResult {
		UNDECIDED, P1WON, P2WON, TIE
	}

	public static void main(String[] args) {
		try {
			GameV7 game = new GameV7();
			game.doGame();
			game.dumpUserData();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Dump user data to log (info level)
	 * @throws Exception 
	 */
	private void dumpUserData() throws Exception {
		final Calendar cal = Calendar.getInstance();
		
	    cal.add(Calendar.DATE, -1);	   	    
		Date yesterday= cal.getTime();
		
		cal.add(Calendar.DATE, +2);
		Date tomorrow= cal.getTime();
		
		String url= settings.getSelfcareApiBaseURL() 
					+ "/api/user/measuredvalues?startdate="+DateFormatter.toSelfcareDateTime(yesterday)
					+"&enddate="+DateFormatter.toSelfcareDateTime(tomorrow)
					+"&sensortypeid="+settings.getSensorTypeIdByName("gewonnen?");
		
		logger.info("Getting basic sensor data via " + url);
		HttpGet request = new HttpGet(url);
		request.addHeader("Authorization", "Bearer " + settings.getSelfcareAccessToken());
		HttpClient httpClient = hack.getClient();
		HttpResponse httpResponse = httpClient.execute(request);
		
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			String res = EntityUtils.toString(httpResponse.getEntity());
			ApiUserMeasuredvaluesResult measuredValues= new Gson().fromJson(res, ApiUserMeasuredvaluesResult.class);
			logger.info("User data available so far: \n"+new Gson().toJson(measuredValues.getValueList()));
		} else {
			logger.log(Level.SEVERE, "Reading of Sensor Data failed");
			throw new Exception();
		}
	}

	/**
	 * Insecure work-around for bypassing the Selfcare SSL certificate issue on the
	 * test server Source:
	 * https://stackoverflow.com/questions/2308774/httpget-with-https-sslpeerunverifiedexception
	 */
	private FlexibleHttpClient hack = new FlexibleHttpClient();

	public class FlexibleHttpClient {

		public HttpClient getClient() throws Exception {
			if (settings.isIgnoreSslCertificateValidity()) {
				return httpClientTrustingAllSSLCerts();
			} else {
				return HttpClientBuilder.create().build();
			}
		}

		@Test
		public void shouldAcceptUnsafeCerts() throws Exception {
			HttpClient httpclient = httpClientTrustingAllSSLCerts();
			HttpGet httpGet = new HttpGet("https://host_with_self_signed_cert");
			HttpResponse response = httpclient.execute(httpGet);
			assertEquals("HTTP/1.1 200 OK", response.getStatusLine().toString());
		}

		private HttpClient httpClientTrustingAllSSLCerts() throws NoSuchAlgorithmException, KeyManagementException {
			HttpClient httpclient = new DefaultHttpClient();

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