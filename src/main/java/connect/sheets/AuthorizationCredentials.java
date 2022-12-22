package connect.sheets;


import com.google.gson.annotations.Expose;
import connect.sheets.googlesheets.SheetsSourceConfig;
import org.apache.kafka.connect.errors.ConnectException;

import java.util.Map;
import java.util.Objects;


/**
 * Authorization Credentials Singleton Class Container
 * @author Max Tiessler
 */
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




    public AuthorizationCredentials(Map<String, String> properties) {
        this.type = properties.get(SheetsSourceConfig.ACCOUNT_TYPE);
        if (this.type == null || Objects.equals(properties.get(SheetsSourceConfig.ACCOUNT_TYPE), "")) {
            throw new ConnectException("SheetsConnector configuration must include account.type setting");
        }

        this.project_id = properties.get(SheetsSourceConfig.SHEETS_PROJECT_ID);
        if (this.project_id == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_PROJECT_ID), "")) {
            throw new ConnectException("SheetsConnector configuration must include project.id setting");
        }

        this.private_key_id = properties.get(SheetsSourceConfig.SHEETS_PRIVATE_KEY_ID);
        if (this.private_key_id == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_PRIVATE_KEY_ID), "")) {
            throw new ConnectException("SheetsConnector configuration must include private.key.id setting");
        }

        this.private_key = properties.get(SheetsSourceConfig.SHEETS_PRIVATE_KEY);
        if (this.private_key == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_PRIVATE_KEY), "")) {
            throw new ConnectException("SheetsConnector configuration must include private.key setting");
        }

        this.client_email = properties.get(SheetsSourceConfig.SHEETS_CLIENT_EMAIL);
        if (this.client_email == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_CLIENT_EMAIL), "")) {
            throw new ConnectException("SheetsConnector configuration must include client.email setting");
        }

        this.client_id = properties.get(SheetsSourceConfig.SHEETS_CLIENT_ID);
        if (this.client_id == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_CLIENT_ID), "")) {
            throw new ConnectException("SheetsConnector configuration must include client.id setting");
        }

        this.auth_uri = properties.get(SheetsSourceConfig.SHEETS_AUTH_URI);
        if (this.auth_uri == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_AUTH_URI), "")) {
            throw new ConnectException("SheetsConnector configuration must include auth.uri setting");
        }

        this.token_uri = properties.get(SheetsSourceConfig.SHEETS_TOKEN_URI);
        if (this.token_uri == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_TOKEN_URI), "")) {
            throw new ConnectException("SheetsConnector configuration must include token.uri setting");
        }

        this.auth_provider_x509_cert_url = properties.get(SheetsSourceConfig.SHEETS_AUTH_PROVIDER_URL);
        if (this.auth_provider_x509_cert_url == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_AUTH_PROVIDER_URL), "")) {
            throw new ConnectException("SheetsConnector configuration must include auth.provider.x509.cert.url setting");
        }

        this.client_x509_cert_url = properties.get(SheetsSourceConfig.SHEETS_CLIENT_CERTIFICATION_URL);
        if (this.client_x509_cert_url == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_CLIENT_CERTIFICATION_URL), "")) {
            throw new ConnectException("SheetsConnector configuration must include client.x509.cert.url setting");
        }
    }

    public static AuthorizationCredentials getInstance(Map<String, String> properties) {


        if (instance == null) {
            instance = new AuthorizationCredentials(properties);
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