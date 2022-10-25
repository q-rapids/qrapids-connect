package connect.sheets;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/* Class to demonstrate the use of Spreadsheet Get Values API */
public class SheetsApi {

	public static String convert(Map<String, String> map) {
		Gson gson = new Gson();
		String json = gson.toJson(map);
		return json;
	}
	/**
	 * Returns a range of values from a spreadsheet.
	 *
	 * @param spreadsheetId - Id of the spreadsheet.
	 * @param range         - Range of cells of the spreadsheet.
	 * @return Values in the range
	 * @throws IOException - if credentials file not found.
	 */
	public static ValueRange getValues(String spreadsheetId, String range) throws Exception {

		AuthorizationCredentials authorizationCredentials = AuthorizationCredentials.getInstance();
		String jsonCredentials;
		if(authorizationCredentials != null) {
			Gson gsonCredentials = new GsonBuilder()
					.excludeFieldsWithoutExposeAnnotation()
					.create();
			jsonCredentials = gsonCredentials.toJson(authorizationCredentials);

		} else {
			throw new Exception("No authorization credentials");
		}
		GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(jsonCredentials.getBytes()))
				.createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
		HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
				credentials);


		// Create the sheets API client
		Sheets service = new Sheets.Builder(new NetHttpTransport(),
				GsonFactory.getDefaultInstance(),
				requestInitializer)
				.setApplicationName("Sheets samples")
				.build();

		ValueRange result = null;
		try {
			// Gets the values of the cells in the specified range.
			result = service.spreadsheets().values().get(spreadsheetId, range).execute();
			int numRows = result.getValues() != null ? result.getValues().size() : 0;
			System.out.printf("%d rows retrieved.", numRows);
		} catch (GoogleJsonResponseException e) {
			// TODO(developer) - handle error appropriately
			GoogleJsonError error = e.getDetails();
			if (error.getCode() == 404) {
				System.out.printf("Spreadsheet not found with id '%s'.\n", spreadsheetId);
			} else {
				throw e;
			}
		}
		return result;
	}
}


