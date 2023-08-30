package fi.bizhop.emailerrest;

import fi.bizhop.emailerrest.model.Code;
import fi.bizhop.emailerrest.model.SheetsRequest;

import java.time.ZonedDateTime;
import java.util.List;

import static fi.bizhop.emailerrest.model.SheetsRequest.Status.REQUESTED;
import static fi.bizhop.emailerrest.model.Store.NBDG;
import static fi.bizhop.emailerrest.model.Store.PG;

public class TestObjects{
    public static final List<Code> PG_CODES = List.of(
            new Code(1L, PG, "ABCD", "11/2023", false),
            new Code(2L, PG, "EFGH", "11/2023", false));
    public static final List<Code> NBDG_CODES = List.of(
            new Code(3L, NBDG, "abcd", "1.11.2023", false),
            new Code(4L, NBDG, "efgh", "1.11.2023", false));

    public static final List<SheetsRequest> REQUESTS = List.of(
            SheetsRequest.builder()
                    .id(1L)
                    .timestamp(ZonedDateTime.now())
                    .name("Erkki Esimerkki")
                    .email("erkki@example.com")
                    .competitionInfo("Kivikon viikkokisat")
                    .competitionDate("6.6.2023")
                    .store(PG)
                    .status(REQUESTED)
                    .build(),
            SheetsRequest.builder()
                    .id(2L)
                    .timestamp(ZonedDateTime.now())
                    .name("Matti Meikäläinen")
                    .email("matti@example.com")
                    .competitionInfo("")
                    .competitionDate("7.7.2023")
                    .store(NBDG)
                    .status(REQUESTED)
                    .build()
    );
}
