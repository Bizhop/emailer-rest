package fi.bizhop.emailerrest.provider;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fi.bizhop.emailerrest.TokenDataStoreFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import static fi.bizhop.emailerrest.Utils.JSON_FACTORY;

@Service
@RequiredArgsConstructor
public class CredentialsProvider {
    private final EnvProvider envProvider;
    private final TokenDataStoreFactory tokenDataStoreFactory;

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = List.of(SheetsScopes.SPREADSHEETS_READONLY, GmailScopes.MAIL_GOOGLE_COM);

    public String getCredentialsString() {
        var installed = new JsonObject();
        installed.addProperty("client_id", envProvider.getenv("EMAILER_CLIENT_ID"));
        installed.addProperty("project_id", envProvider.getenv("EMAILER_PROJECT_ID"));
        installed.addProperty("auth_uri", "https://accounts.google.com/o/oauth2/auth");
        installed.addProperty("token_uri", "https://oauth2.googleapis.com/token");
        installed.addProperty("auth_provider_x509_cert_url", "https://www.googleapis.com/oauth2/v1/certs");
        installed.addProperty("client_secret", envProvider.getenv("EMAILER_CLIENT_SECRET"));

        var redirectUris = new JsonArray();
        redirectUris.add("http://localhost");
        installed.add("redirect_uris", redirectUris);

        var credentials = new JsonObject();
        credentials.add("installed", installed);

        return credentials.toString();
    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     */
    public Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        var credentials = getCredentialsString();
        var in = new ByteArrayInputStream(credentials.getBytes());
        var clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        var flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(tokenDataStoreFactory)
                .setAccessType("offline")
                .build();
        var receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
