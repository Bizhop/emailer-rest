package fi.bizhop.emailerrest.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Value
@Builder
@Jacksonized
public class Report {
    List<ReportEmail> emails;
    Map<Store, Integer> codesRemaining;
    String error;
}
