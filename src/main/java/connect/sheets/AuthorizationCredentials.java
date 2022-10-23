package connect.sheets;


import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public final class AuthorizationCredentials {
    private static AuthorizationCredentials instance;
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public String getProjectId() {
        return projectId;
    }

    public URI getAuthorizationUri() {
        return authorizationUri;
    }

    public URI getTokenUri() {
        return tokenUri;
    }

    public URL getAuthorizationProvider() {
        return authorizationProvider;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public URI getRedirectUris() {
        return redirectUris;
    }

    private String projectId;
    private URI authorizationUri;
    private URI tokenUri;
    private URL authorizationProvider;
    private String clientSecret;
    private URI redirectUris;
    
    private AuthorizationCredentials(final String clientId,
                                     final String projectId,
                                     final String authorizationUri,
                                     final String tokenUri,
                                     final String authorizationProvider,
                                     final String clientSecret,
                                     final String redirectUris) throws MalformedURLException {
        this.clientId = clientId;
        this.projectId = projectId;
        this.authorizationUri = URI.create(authorizationUri);
        this.tokenUri = URI.create(tokenUri);
        this.authorizationProvider = new URL(authorizationProvider);
        this.clientSecret = clientSecret;
        this.redirectUris = URI.create(redirectUris);
    }

    public static AuthorizationCredentials getInstance(final String clientId,
                                                       final String projectId,
                                                       final String authorizationUri,
                                                       final String tokenUri,
                                                       final String authorizationProvider,
                                                       final String clientSecret,
                                                       final String redirectUris) throws MalformedURLException {
        if (instance == null) {
            instance = new AuthorizationCredentials(
                    clientId,
                    projectId,
                    authorizationUri,
                    tokenUri,
                    authorizationProvider,
                    clientSecret,
                    redirectUris);
        }
        return instance;
    }
    public static AuthorizationCredentials getInstance() {
        return instance;
    }
}