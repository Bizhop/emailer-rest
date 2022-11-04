package fi.bizhop.emailerrest.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class EmailWrapper {
    Email email;
    String error;
}
