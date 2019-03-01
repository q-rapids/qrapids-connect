/* Copyright (C) 2019 Fraunhofer IESE
 * 
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class ConnectorAPI {
	
	public static void delete(String host, int port, String connectorName) throws IOException {
		URL url = new URL("http",host,port,connectorName);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestProperty( "Content-Type", "application/json" );
		httpCon.setRequestMethod("DELETE");
		httpCon.connect();
	}
}
