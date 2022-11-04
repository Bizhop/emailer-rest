package fi.bizhop.emailerrest;

import fi.bizhop.emailerrest.provider.CredentialsProvider;
import fi.bizhop.emailerrest.provider.EnvProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CredentialsProviderTest {
    @Mock EnvProvider envProvider;
    @InjectMocks CredentialsProvider credentialsProvider;

    @Test
    void getCredentialsTest() {
        when(envProvider.getenv("EMAILER_CLIENT_ID")).thenReturn("test client id");
        when(envProvider.getenv("EMAILER_CLIENT_SECRET")).thenReturn("test client secret");
        when(envProvider.getenv("EMAILER_PROJECT_ID")).thenReturn("test project id");

        var credentials = credentialsProvider.getCredentials();

        System.out.println(credentials);
    }
}
