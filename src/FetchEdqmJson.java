/* Rob Wynne, MSC
 * Fetch all EDQM data in JSON
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


@SuppressWarnings("deprecation")
public class FetchEdqmJson {
	
	public static void main(String args[]) {
		FetchEdqmJson fetcher = new FetchEdqmJson();
		fetcher.run();		
	}
	
	public void run() {
		try {
			//roll your own
			String secret = "********";
			Vector<String> codes = new Vector<String>();
			
			//TODO: Make the call to get all codes
			codes.add("SOM");
			codes.add("BDF");
			codes.add("RCA");
			codes.add("TRA");
			codes.add("ISI");
			codes.add("AME");
			codes.add("PDF");
			codes.add("UOP");
			codes.add("ROA");
			codes.add("PAC");
			codes.add("CON");
			codes.add("CLO");
			codes.add("DEV");
			codes.add("CDF");
			codes.add("PFT");
			codes.add("CMT");
			codes.add("CMP");
			codes.add("MAP");
			codes.add("FIL");

			//TODO: generate date without param
			//		 and method/postfix via config file
			String httpVerb = "GET";
			String method = "/standardterms/api/v1/basic_data_by_class/";
			String methodPostfix = "/1/1";
			String host = "standardterms.edqm.eu";
			String date = getServerTime();			
			System.out.print("[");
			for(int i=0; i < codes.size(); i++) {

				String message = httpVerb + "&" + method + codes.elementAt(i) + methodPostfix + "&" + host + "&" + date;

				Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
				SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
				sha512_HMAC.init(secret_key);

				String hash = Base64.encodeBase64String(sha512_HMAC.doFinal(message.getBytes()));
				int hashEnd = hash.length();
				hash = hash.substring(66, hashEnd);		    	 
				
				@SuppressWarnings("resource")
				HttpClient client = new DefaultHttpClient();
				HttpUriRequest request = RequestBuilder.get().setUri("https://standardterms.edqm.eu/standardterms/api/v1/basic_data_by_class/"+ codes.elementAt(i) + "/1/1")
						.setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
						.setHeader("X-STAPI-KEY","wynner@mail.nih.gov|" + hash)
						.setHeader("Accept-Charset", "utf-8")
						.setHeader("Date", date)
						.build();
//				StringHttpMessageConverter converter = new StringHttpMessageConverter();
				HttpResponse response = client.execute(request);
				HttpEntity entity = response.getEntity();
				
				String responseString = EntityUtils.toString(entity, "UTF-8");
				if( i < codes.size()-1) {
//					System.out.print(StringEscapeUtils.unescapeJava(responseString) + ",");
					System.out.print(responseString + ",");
				}
				else {
//					System.out.print(StringEscapeUtils.unescapeJava(responseString));
					System.out.print(responseString);
				}

				// Don't stress them
				Thread.sleep(2000);
			}
			System.out.print("]");
		}
		catch(Exception e) {
			System.out.println("Error");
			e.printStackTrace();
		}		
	}
	
	/* Credit to Hannes R.
	 * https://stackoverflow.com/questions/7707555/getting-date-in-http-format-in-java 
	 */
	String getServerTime() {
	    Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat dateFormat = new SimpleDateFormat(
	        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return dateFormat.format(calendar.getTime());
	}
}
