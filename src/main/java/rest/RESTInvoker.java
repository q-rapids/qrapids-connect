/* Copyright (C) 2019 Fraunhofer IESE
 * 
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package rest;
import org.apache.commons.codec.binary.Base64;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Basic REST Reader
 * @author Axel Wickenkamp
 *
 */
 
public class RESTInvoker {
    private final String baseUrl;
    private final String username;
    private final String password;
    private String secret = null;
 
    public RESTInvoker(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        disableSSL();
    }

    public RESTInvoker(String baseUrl, String secret) {
    	this(baseUrl,"","");
    	this.secret = secret;	
    }

    
    public String getDataFromServer(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(baseUrl + path);

            URLConnection urlConnection = setUsernamePassword(url);
            
            if(secret != null){
        		urlConnection.setRequestProperty("Authorization","Bearer " + secret);
        	}

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
 
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
 
    private URLConnection setUsernamePassword(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        if ( username != null && ! username.isEmpty() ) {
	        String authString = username + ":" + password;
	        String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
	        urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
        }
        return urlConnection;
    }

    private void disableSSL() {
    	TrustManager[] trustAllCerts = new TrustManager[] {
    		    new X509TrustManager() {

    		        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
    		            return null;
    		        }
    		        public void checkClientTrusted(
    		            java.security.cert.X509Certificate[] certs, String authType) {
    		        }
    		        public void checkServerTrusted(
    		            java.security.cert.X509Certificate[] certs, String authType) {
    		        }
    		    }
    		};

    		// Install the all-trusting trust manager
    		try {
    		    SSLContext sc = SSLContext.getInstance("SSL");
    		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

    }
}