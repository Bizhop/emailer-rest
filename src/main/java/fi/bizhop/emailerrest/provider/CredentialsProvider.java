package fi.bizhop.emailerrest.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CredentialsProvider {
    private final EnvProvider envProvider;

    public String getCredentials() {
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
}
