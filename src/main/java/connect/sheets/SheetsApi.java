package connect.sheets;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class SheetsApi {

	private static Sheets sheetsService;

	/**
	 * Creates a new google sheet spreadsheet
	 * @param title spreadsheet title
	 * @return spreadsheet ID
	 * @throws IOException
	 * @throws AuthorizationCredentialsException
	 */
	public static String createSpreadsheet(String title) throws IOException, AuthorizationCredentialsException {
		Sheets service = getSheetsService();
		Spreadsheet spreadsheet = new Spreadsheet()
				.setProperties(new SpreadsheetProperties().setTitle(title));
		spreadsheet = service.spreadsheets().create(spreadsheet)
				.setFields("spreadsheetId")
				.execute();
		return spreadsheet.getSpreadsheetId();
	}


	public static void createSheets(final String spreadSheetId, final List<String> sheetTitles) throws IOException, AuthorizationCredentialsException {
		BatchUpdateSpreadsheetRequest requestBody = new BatchUpdateSpreadsheetRequest();
		requestBody.setIncludeSpreadsheetInResponse(false);
		List<Request> requestList= new ArrayList<>();
		for(int i = 1; i < sheetTitles.size() + 1; ++i) {
			String sheetTitle = sheetTitles.get(i-1);
			Request request = createSheetRequest(sheetTitle, i);
			requestList.add(request);
		}
		requestBody.setRequests(requestList);
		doBatchRequest(spreadSheetId, requestBody);
	}

	private static Request createSheetRequest(final String sheetTitle, final Integer sheetId) {
		Request request = new Request();
		AddSheetRequest addSheetRequest = new AddSheetRequest();
		SheetProperties sheetProperties = new SheetProperties();
		sheetProperties.setSheetId(sheetId);
		sheetProperties.setTitle(sheetTitle);
		addSheetRequest.setProperties(sheetProperties);
		request.setAddSheet(addSheetRequest);
		return request;
	}

	private static void doBatchRequest(String spreadSheetId, BatchUpdateSpreadsheetRequest requestBody) throws IOException, AuthorizationCredentialsException {
		Sheets service = getSheetsService();
		Sheets.Spreadsheets.BatchUpdate request = service.spreadsheets().batchUpdate(spreadSheetId, requestBody);
		BatchUpdateSpreadsheetResponse response = request.execute();
	}
	private static Sheets getSheetsService() throws AuthorizationCredentialsException, IOException {
		if (sheetsService == null) {
			String jsonCredentials = getJson(AuthorizationCredentials.getInstance());
			return createSheetsApiClient(jsonCredentials);
		} else {
			return sheetsService;
		}
	}

	/**
	 *
	 * @param authorizationCredentials
	 * @return
	 * @throws AuthorizationCredentialsException
	 */
	private static String getJson(final AuthorizationCredentials authorizationCredentials) throws AuthorizationCredentialsException {
		if(authorizationCredentials != null) {
			Gson gsonCredentials = new GsonBuilder()
					.excludeFieldsWithoutExposeAnnotation()
					.create();
			return gsonCredentials.toJson(authorizationCredentials);
		} else {
			throw new AuthorizationCredentialsException("No authorization credentials detected");
		}
	}

	/**
	 * Create the sheets API client
	 * @param credentials
	 * @return sheets api client
	 * @throws IOException
	 */
	private static Sheets createSheetsApiClient(String credentials) throws IOException {
		GoogleCredentials googleCredentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credentials.getBytes()))
				.createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
		HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(googleCredentials);
		return new Sheets.Builder(new NetHttpTransport(),
				GsonFactory.getDefaultInstance(),
				requestInitializer)
				.setApplicationName("Sheets samples")
				.build();
	}

	/**
	 * Returns a range of values from a spreadsheet.
	 *
	 * @param spreadsheetId - Id of the spreadsheet.
	 * @param range         - Range of cells of the spreadsheet.
	 * @return Values in the range
	 * @throws IOException - if credentials file not found.
	 */
	public static ValueRange getValues(String spreadsheetId, String range) throws AuthorizationCredentialsException, IOException {
		Sheets service = getSheetsService();
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

	public static String getSheetId(final String sprintName, final String spreadSheetId) {
		// TODO investigate API
		return "0";
	}


	public static Double getHoursDone(final Integer developerPosition,
									   final String sprint,
									   final String spreadSheetId) throws AuthorizationCredentialsException, IOException {
		Sheets service = getSheetsService();
		ValueRange result;
		Double hours = 0.0;
		Integer position = developerPosition + 10;
		try {
			// Gets the values of the cells in the specified range.
			String range = sprint+"!"+"J"+Integer.toString(position);
			result = service.spreadsheets().values().get(spreadSheetId, range).execute();
			int numRows = result.getValues() != null ? result.getValues().size() : 0;
			if (numRows != 0) {
				String hoursString = result.getValues().get(0).toString();
				hoursString = hoursString.substring(1, result.getValues().get(0).toString().length() - 1);
				hoursString = hoursString.replace(",", ".");
				hours = Double.parseDouble(hoursString);
			}
		} catch (GoogleJsonResponseException e) {
			// TODO(developer) - handle error appropriately
			GoogleJsonError error = e.getDetails();
			if (error.getCode() == 404) {
				System.out.printf("Spreadsheet not found with id '%s'.\n", spreadSheetId);
			} else {
				throw e;
			}
		}
		return hours;
	}


	public static String getDeveloperName(final Integer developerPosition, final String spreadSheetId) throws AuthorizationCredentialsException, IOException {
		Sheets service = getSheetsService();
		ValueRange result;
		Integer position = developerPosition + 10;
		try {
			// Gets the values of the cells in the specified range.
			String range = "I"+Integer.toString(position);
			result = service.spreadsheets().values().get(spreadSheetId, range).execute();
			int numRows = result.getValues() != null ? result.getValues().size() : 0;
			if (numRows != 0) {
				String developerName = result.getValues().get(0).toString();
				developerName = developerName.substring(1, result.getValues().get(0).toString().length() - 1);
				return developerName;
			}
		} catch (GoogleJsonResponseException e) {
			// TODO(developer) - handle error appropriately
			GoogleJsonError error = e.getDetails();
			if (error.getCode() == 404) {
				System.out.printf("Spreadsheet not found with id '%s'.\n", spreadSheetId);
			} else {
				throw e;
			}
		}
		return "anonymous";
	}
}


