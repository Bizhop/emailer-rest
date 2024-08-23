package fi.bizhop.emailerrest;

import fi.bizhop.emailerrest.db.CodeRepository;
import fi.bizhop.emailerrest.db.SentRepository;
import fi.bizhop.emailerrest.db.SheetsRequestRepository;
import fi.bizhop.emailerrest.model.Email;
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fi.bizhop.emailerrest.model.SheetsRequest.Status.REQUESTED;
import static fi.bizhop.emailerrest.model.Store.NBDG;
import static fi.bizhop.emailerrest.model.Store.PG;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailerServiceTest {
    @Mock CodeRepository codeRepository;
    @Mock SentRepository sentRepository;
    @Mock GmailAPI gmailAPI;
    @Mock SheetsRequestRepository sheetsRequestRepository;
    @InjectMocks EmailerService service;

    @BeforeAll
    static void setErrorLogging() {
        LoggingSystem.get(ClassLoader.getSystemClassLoader()).setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.INFO);
    }

    @Test
    void requestPreview() {
        when(sheetsRequestRepository.findByIdInAndStatus(List.of(1L, 2L), REQUESTED)).thenReturn(TestObjects.REQUESTS.subList(0, 2));
        when(codeRepository.findAllByStoreAndUsedFalse(PG)).thenReturn(new ArrayList<>(TestObjects.PG_CODES));
        when(codeRepository.findAllByStoreAndUsedFalse(NBDG)).thenReturn(new ArrayList<>(TestObjects.NBDG_CODES));

        var response = service.completeSheetsRequests(List.of(1L, 2L), false, "Kivikon viikkokisat", null, null);

        verify(gmailAPI, never()).sendEmailWithAccessToken(any(), any());
        verify(codeRepository, never()).save(any());
        verify(sentRepository, never()).save(any());

        assertEquals(2, response.size());

        var erkki = response.get(0);
        assertEquals("erkki@example.com", erkki.getEmail().getTo());
        assertNull(erkki.getError());

        var matti = response.get(1);
        assertEquals("matti@example.com", matti.getEmail().getTo());
        assertNull(matti.getError());
    }

    @Test
    void requestPreviewNotEnoughCodes() {
        when(sheetsRequestRepository.findByIdInAndStatus(List.of(1L, 2L), REQUESTED)).thenReturn(TestObjects.REQUESTS.subList(0, 2));
        when(codeRepository.findAllByStoreAndUsedFalse(PG)).thenReturn(new ArrayList<>(List.of(TestObjects.PG_CODES.get(0))));
        when(codeRepository.findAllByStoreAndUsedFalse(NBDG)).thenReturn(Collections.emptyList());

        var response = service.completeSheetsRequests(List.of(1L, 2L), false, "Kivikon viikkokisat", null, null);

        verify(gmailAPI, never()).sendEmailWithAccessToken(any(), any());
        verify(codeRepository, never()).save(any());
        verify(sentRepository, never()).save(any());

        assertEquals(2, response.size());

        var successes = response.stream()
                .filter(wrapper -> wrapper.getEmail() != null)
                .toList();
        assertEquals(1, successes.size());
        var success = successes.get(0);
        assertEquals("erkki@example.com", success.getEmail().getTo());
        assertNull(success.getError());

        var failures = response.stream()
                .filter(wrapper -> wrapper.getError() != null)
                .toList();
        assertEquals(1, failures.size());
        var failure = failures.get(0);
        assertNull(failure.getEmail());
        assertEquals("Not enough codes for nbdg", failure.getError());
    }

    @Test
    void requestSend() {
        when(sheetsRequestRepository.findByIdInAndStatus(List.of(1L, 2L), REQUESTED)).thenReturn(TestObjects.REQUESTS.subList(0, 2));
        when(codeRepository.findAllByStoreAndUsedFalse(PG)).thenReturn(new ArrayList<>(TestObjects.PG_CODES));
        when(codeRepository.findAllByStoreAndUsedFalse(NBDG)).thenReturn(new ArrayList<>(TestObjects.NBDG_CODES));

        when(gmailAPI.sendEmailWithAccessToken(any(Email.class), any())).thenAnswer((Answer<Email>) invocation -> {
            var email = (Email)invocation.getArguments()[0];
            email.setTimestamp(ZonedDateTime.now());
            return email;
        });

        var response = service.completeSheetsRequests(List.of(1L, 2L), true, "Kivikon viikkokisat", null, null);

        verify(gmailAPI, times(2)).sendEmailWithAccessToken(any(), any());
        verify(codeRepository, times(2)).save(any());
        verify(sentRepository, times(2)).save(any());

        assertEquals(2, response.size());
        response.forEach(wrapper -> {
            assertNotNull(wrapper.getEmail());
            assertNull(wrapper.getError());
        });
    }

    @Test
    void requestIdsNotMatchingDb() {
        when(sheetsRequestRepository.findByIdInAndStatus(List.of(1L, 2L, 3L, 4L), REQUESTED)).thenReturn(TestObjects.REQUESTS);

        var response = service.completeSheetsRequests(List.of(1L, 2L, 3L, 4L), false, "Kivikon viikkokisat", null, null);

        assertEquals(1, response.size());
        var error = response.get(0);
        assertNull(error.getEmail());
        assertEquals("Number of ids and requests don't match", error.getError());
    }

    @Test
    void testGetRequests() {
        when(sheetsRequestRepository.findByStatus(REQUESTED)).thenReturn(TestObjects.REQUESTS);

        var response = service.getSheetRequests(REQUESTED);

        assertEquals(3, response.size());

        assertEquals("matti@example.com", response.get(0).getEmail());
        assertEquals("erkki@example.com", response.get(1).getEmail());
        assertEquals("ville@example.com", response.get(2).getEmail());
    }
}
