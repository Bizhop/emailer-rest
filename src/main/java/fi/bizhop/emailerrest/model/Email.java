package fi.bizhop.emailerrest.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.ZonedDateTime;

@Data
@Builder
public class Email {
    @NonNull String from;
    @NonNull String to;
    @NonNull String subject;
    String content;
    ZonedDateTime timestamp;
}
