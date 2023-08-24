package fi.bizhop.emailerrest.model;

import fi.bizhop.emailerrest.Utils;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
@Builder
public class ReportEmail {
    String to;
    String store;
    String code;
    String sent;

    public static ReportEmail fromSent(Sent email) {
        return ReportEmail.builder()
                .to(email.getTo())
                .store(store(email.getContent()))
                .code(code(email.getContent()))
                .sent(sent(email.getTimestamp()))
                .build();
    }

    private static String store(String content) {
        var store = "No store found";
        if(content.contains("PowerGrip")) {
            store = "PG";
        } else if(content.contains("NBDG")) {
            store = "NBDG";
        }

        return store;
    }

    private static String code(String content) {
        var lines = content.lines().toList();

        return lines.size() < 7 ? "No code found" : lines.get(6);
    }

    private static String sent(ZonedDateTime timestamp) {
        if(timestamp == null) return "Not sent";

        return Utils.formatDateTime(timestamp);
    }
}
