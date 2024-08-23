package fi.bizhop.emailerrest;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import fi.bizhop.emailerrest.model.Email;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Properties;

import static fi.bizhop.emailerrest.Utils.JSON_FACTORY;
import static fi.bizhop.emailerrest.Utils.getCredentialsFromToken;
import static javax.mail.Message.RecipientType.TO;

@Service
public class GmailAPI {
    private final String APPLICATION_NAME = "Emailer";

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
     * @param token google access token
     * @return email if sent, null if unsuccessful
     */
    public Email sendEmailWithAccessToken(Email email, String token) {
        try {
            final var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            var service = new Gmail.Builder(
                    HTTP_TRANSPORT,
                    JSON_FACTORY,
                    new HttpCredentialsAdapter(getCredentialsFromToken(token))
            )
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            var message = createMessageWithEmail(email);

            try {
                // Create send message
                service.users().messages().send("me", message).execute();
                email.setTimestamp(ZonedDateTime.now());
                return email;
            } catch (GoogleJsonResponseException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
