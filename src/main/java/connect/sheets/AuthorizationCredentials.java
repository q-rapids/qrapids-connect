package connect.sheets;


import com.google.gson.annotations.Expose;

public final class AuthorizationCredentials {
    private static AuthorizationCredentials instance;

    @Expose
    private String type;
    @Expose
    private String project_id;
    @Expose
    private String private_key_id;
    @Expose
    private String private_key;
    @Expose
    private String client_email;
    @Expose
    private String client_id;
    @Expose
    private String auth_uri;
    @Expose
    private String token_uri;
    @Expose
    private String auth_provider_x509_cert_url;
    @Expose
    private String client_x509_cert_url;

    public String getType() {
        return type;
    }

    public String getProject_id() {
        return project_id;
    }

    public String getPrivate_key_id() {
        return private_key_id;
    }

    public String getPrivate_key() {
        return private_key;
    }

    public String getClient_email() {
        return client_email;
    }

    public String getClient_id() {
        return client_id;
    }

    public String getAuth_uri() {
        return auth_uri;
    }

    public String getToken_uri() {
        return token_uri;
    }

    public String getAuth_provider_x509_cert_url() {
        return auth_provider_x509_cert_url;
    }

    public String getClient_x509_cert_url() {
        return client_x509_cert_url;
    }




    public AuthorizationCredentials(final String type,
                                    final String project_id,
                                    final String privateKeyID,
                                    final String privateKey,
                                    final String clientEmail,
                                    final String clientID,
                                    final String authURI,
                                    final String tokenURI,
                                    final String authProvider,
                                    final String clientCert) {
        this.type = type;
        this.project_id = project_id;
        this.private_key_id = privateKeyID;
        this.private_key = privateKey;
        this.client_email = clientEmail;
        this.client_id = clientID;
        this.auth_uri = authURI;
        this.token_uri = tokenURI;
        this.auth_provider_x509_cert_url = authProvider;
        this.client_x509_cert_url = clientCert;
    }

    public static AuthorizationCredentials getInstance(final String type,
                                                       final String projectId,
                                                       final String privateKeyID,
                                                       final String privateKey,
                                                       final String clientEmail,
                                                       final String clientID,
                                                       final String authURI,
                                                       final String tokenURI,
                                                       final String authProvider,
                                                       final String clientCert) {
        if (instance == null) {
            instance = new AuthorizationCredentials(type,
             projectId,
             privateKeyID,
             privateKey,
             clientEmail,
             clientID,
             authURI,
             tokenURI,
             authProvider,
             clientCert);
        }
        return instance;
    }

    public static AuthorizationCredentials getInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }
}