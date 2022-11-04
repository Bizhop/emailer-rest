package fi.bizhop.emailerrest;

import fi.bizhop.emailerrest.db.CodeRepository;
import fi.bizhop.emailerrest.db.SentRepository;
import fi.bizhop.emailerrest.model.Email;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static fi.bizhop.emailerrest.model.Store.PG;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailerServiceTest {
    @Mock CodeRepository codeRepository;
    @Mock SentRepository sentRepository;
    @Mock GmailAPI gmailAPI;
    @InjectMocks EmailerService service;

    @BeforeAll
    static void setErrorLogging() {
        LoggingSystem.get(ClassLoader.getSystemClassLoader()).setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.INFO);
    }

    @Test
    void requestPreview() throws IOException {
        var request = FileUtils.readFileToString(new File("src/test/resources/request.csv"), Charset.defaultCharset());

        when(codeRepository.findAllByStoreAndUsedFalse(PG)).thenReturn(new ArrayList<>(TestObjects.PG_CODES));

        var response = service.processRequests(request, false);

        verify(gmailAPI, never()).sendEmail(any());
        verify(codeRepository, never()).save(any());
        verify(sentRepository, never()).save(any());

        assertEquals(2, response.size());
        response.forEach(wrapper -> {
            assertNotNull(wrapper.getEmail());
            assertNull(wrapper.getError());
        });
    }

    @Test
    void requestPreviewNotEnoughCodes() throws IOException {
        var request = FileUtils.readFileToString(new File("src/test/resources/request4.csv"), Charset.defaultCharset());

        when(codeRepository.findAllByStoreAndUsedFalse(PG)).thenReturn(new ArrayList<>(TestObjects.PG_CODES));

        var response = service.processRequests(request, false);

        verify(gmailAPI, never()).sendEmail(any());
        verify(codeRepository, never()).save(any());
        verify(sentRepository, never()).save(any());

        assertEquals(4, response.size());

        var successes = response.stream()
                .filter(wrapper -> wrapper.getEmail() != null)
                .count();
        assertEquals(2, successes);

        var failures = response.stream()
                .filter(wrapper -> wrapper.getError() != null)
                .count();
        assertEquals(2, failures);
    }

    @Test
    void requestSend() throws IOException {
        var request = FileUtils.readFileToString(new File("src/test/resources/request.csv"), Charset.defaultCharset());


        when(codeRepository.findAllByStoreAndUsedFalse(PG)).thenReturn(new ArrayList<>(TestObjects.PG_CODES));

        when(gmailAPI.sendEmail(any(Email.class))).thenAnswer((Answer<Email>) invocation -> {
            var email = (Email)invocation.getArguments()[0];
            email.setTimestamp(ZonedDateTime.now());
            return email;
        });

        var response = service.processRequests(request, true);

        verify(gmailAPI, times(2)).sendEmail(any());
        verify(codeRepository, times(2)).save(any());
        verify(sentRepository, times(2)).save(any());

        assertEquals(2, response.size());
        response.forEach(wrapper -> {
            assertNotNull(wrapper.getEmail());
            assertNull(wrapper.getError());
        });
    }
}
