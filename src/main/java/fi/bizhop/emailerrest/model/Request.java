package fi.bizhop.emailerrest.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Request {
    String email;
    String date;
    String store;

    public static Request fromSheetsRequest(SheetsRequest sheetsRequest) {
        return builder()
                .email(sheetsRequest.getEmail())
                .date(sheetsRequest.getCompetitionDate())
                .store(sheetsRequest.getStore().getEmailName())
                .build();
    }
}
