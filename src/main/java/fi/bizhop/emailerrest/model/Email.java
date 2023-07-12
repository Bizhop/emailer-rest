package fi.bizhop.emailerrest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    ZonedDateTime timestamp;
}
