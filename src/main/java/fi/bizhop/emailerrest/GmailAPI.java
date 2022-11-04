package fi.bizhop.emailerrest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import fi.bizhop.emailerrest.model.Email;
import fi.bizhop.emailerrest.provider.CredentialsProvider;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static javax.mail.Message.RecipientType.TO;

@Service
@RequiredArgsConstructor
public class GmailAPI {
    private final CredentialsProvider credentialsProvider;
    private final TokenDataStoreFactory tokenDataStoreFactory;

    /** Application name. */
    private final String APPLICATION_NAME = "Emailer";
    /** Global instance of the JSON factory. */
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        var credentials = credentialsProvider.getCredentials();
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

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param email object with email details
     * @return the MimeMessage to be used to send email
     * @throws MessagingException - if a wrongly formatted address is encountered.
     */
    private MimeMessage createEmail(Email email)
            throws MessagingException {
        var session = Session.getDefaultInstance(new Properties(), null);

        var mimeMessage = new MimeMessage(session);

        mimeMessage.setFrom(new InternetAddress(email.getFrom()));
        mimeMessage.addRecipient(TO, new InternetAddress(email.getTo()));
        mimeMessage.setSubject(email.getSubject());
        mimeMessage.setText(email.getContent());
        return mimeMessage;
    }

    /**
     * Create a message with email.
     *
     * @param email object with email details
     * @return a message containing a base64url encoded email
     * @throws IOException - if service account credentials file not found.
     * @throws MessagingException - if a wrongly formatted address is encountered.
     */
    private Message createMessageWithEmail(Email email)
            throws MessagingException, IOException {
        var emailContent = createEmail(email);

        var buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        var bytes = buffer.toByteArray();
        var encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        var message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    /**
     * Send an email from the user's mailbox to its recipient.
     *
     * @param email object with email details
     * @return email if sent, null if unsuccessful
     */
    public Email sendEmail(Email email) {
        // Build a new authorized API client service.
        try {
            final var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            var service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            var message = createMessageWithEmail(email);

            try {
                // Create send message
                service.users().messages().send("me", message).execute();
                email.setTimestamp(ZonedDateTime.now());
                return email;
            } catch (GoogleJsonResponseException e) {
                // TODO(developer) - handle error appropriately
                GoogleJsonError error = e.getDetails();
                if (error.getCode() == 403) {
                    System.err.println("Unable to send message: " + e.getDetails());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
