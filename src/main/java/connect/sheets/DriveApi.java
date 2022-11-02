package connect.sheets;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DriveApi {

	private static Drive driveService;

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

	private static Drive createDriveApiClient(String credentials) throws IOException {
		GoogleCredentials googleCredentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credentials.getBytes()))
				.createScoped(Arrays.asList(DriveScopes.DRIVE_FILE));
		HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
				googleCredentials);
		return new Drive.Builder(new NetHttpTransport(),
				GsonFactory.getDefaultInstance(),
				requestInitializer)
				.setApplicationName("Drive samples")
				.build();
	}

	private static Drive getDriveService() throws AuthorizationCredentialsException, IOException {
		if (driveService == null) {
			String jsonCredentials = getJson(AuthorizationCredentials.getInstance());
			return createDriveApiClient(jsonCredentials);
		} else {
			return driveService;
		}
	}
	public static List<String> shareFile(String realFileId, String realUser, String realDomain)
			throws IOException, AuthorizationCredentialsException {
		// Build a new authorized API client service.
		Drive service = getDriveService();
		Permission permission = new Permission();
		permission.setRole("reader");
		permission.setType("anyone");
		permission.setAllowFileDiscovery(true);
				service.permissions().create(realFileId, permission);
		final List<String> ids = new ArrayList<>();

		JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
			@Override
			public void onFailure(GoogleJsonError e,
								  HttpHeaders responseHeaders)
					throws IOException {
				// Handle error
				System.err.println(e.getMessage());
			}

			@Override
			public void onSuccess(Permission permission,
								  HttpHeaders responseHeaders)
					throws IOException {
				System.out.println("Permission ID: " + permission.getId());

				ids.add(permission.getId());

			}
		};
		BatchRequest batch = service.batch();
		Permission userPermission = new Permission()
				.setType("anyone")
				.setRole("reader");

		userPermission.setEmailAddress(realUser);
		try {
			service.permissions().create(realFileId, userPermission)
					.setFields("id")
					.queue(batch, callback);

			Permission domainPermission = new Permission()
					.setType("anyone")
					.setRole("reader");

			domainPermission.setDomain(realDomain);

			service.permissions().create(realFileId, domainPermission)
					.setFields("id")
					.queue(batch, callback);

			batch.execute();

			return ids;
		} catch (GoogleJsonResponseException e) {
			// TODO(developer) - handle error appropriately
			System.err.println("Unable to modify permission: " + e.getDetails());
			throw e;
		}
	}
}


