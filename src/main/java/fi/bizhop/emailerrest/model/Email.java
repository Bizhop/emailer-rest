package fi.bizhop.emailerrest.model;

import fi.bizhop.emailerrest.Utils;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

@Data
@Builder
public class Email {
    @ToString.Exclude
    String from;

    String to;

    @ToString.Exclude
    String subject;

    @ToString.Exclude
    String content;

    @ToString.Exclude
    long timestamp;

    @ToString.Include
    String store() {
        if(content == null) return "";

        var store = "No store found";
        if(content.contains("Powergrip")) {
            store = "PG";
        } else if(content.contains("NBDG")) {
            store = "NBDG";
        }

        return store;
    }

    @ToString.Include
    String code() {
        if(content == null) return "";

        var lines = content.lines().collect(Collectors.toList());

        return lines.size() < 7 ? "No code found" : lines.get(6);
    }

    @ToString.Include
    String sent() {
        if(timestamp == 0) return "Not sent";

        var ztd = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return Utils.formatDate(ztd);
    }
}
