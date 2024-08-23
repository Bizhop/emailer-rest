package fi.bizhop.emailerrest;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import fi.bizhop.emailerrest.db.SheetsRequestRepository;
import fi.bizhop.emailerrest.model.SheetsRequest;
import fi.bizhop.emailerrest.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static fi.bizhop.emailerrest.Utils.JSON_FACTORY;
import static fi.bizhop.emailerrest.Utils.getCredentialsFromToken;
import static fi.bizhop.emailerrest.model.SheetsRequest.Status.REQUESTED;

@Service
@RequiredArgsConstructor
public class SheetsAPI {
    private final SheetsRequestRepository sheetsRequestRepository;

    private final String APPLICATION_NAME = "Emailer";
    private final String SPREADSHEET_ID = System.getenv("EMAILER_SPREADSHEET_ID");

    public List<SheetsRequest> getRequests(String token) throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        var service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(getCredentialsFromToken(token)))
                .setApplicationName(APPLICATION_NAME)
                .build();

        var response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, "Vastauksista 1!A2:F")
                .execute();
        var values = response.getValues();

        var requestsInDb = sheetsRequestRepository.findAll().stream()
                .map(request -> String.format("%s-%s", request.getEmail(), request.getCompetitionDate()))
                .collect(Collectors.toSet());

        if(values == null || values.isEmpty()) {
            System.out.println("No data");
            return null;
        } else {
            var newRequests = new ArrayList<SheetsRequest>();
            for(var row: values) {
                if(row.size() < 6) {
                    System.out.println("Row size less than 6");
                }
                else {
                    var identifier = String.format("%s-%s", row.get(2), row.get(3));
                    if(!requestsInDb.contains(identifier)) {
                        var name = (String)row.get(1);
                        var email = (String)row.get(2);
                        var competitionDate = (String)row.get(3);
                        var competitionInfo = (String)row.get(4);

                        Store store = null;
                        var storeInput = (String)row.get(5);
                        if(storeInput.contains("PowerGrip")) {
                            store = Store.PG;
                        }
                        else if(storeInput.contains("NBDG")) {
                            store = Store.NBDG;
                        }

                        if(store != null) {
                            var request = SheetsRequest.builder()
                                    .timestamp(ZonedDateTime.from(Utils.parseSheetsImportDateTime(row.get(0))))
                                    .name(name)
                                    .email(email)
                                    .competitionInfo(competitionInfo)
                                    .store(store)
                                    .competitionDate(competitionDate)
                                    .status(REQUESTED)
                                    .build();

                            newRequests.add(sheetsRequestRepository.save(request));
                        } else {
                            System.out.println("Invalid store: " + storeInput);
                        }
                    }
                }
            }
            return newRequests;
        }
    }
}
