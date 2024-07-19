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
                    .competitionDate("1.7.2024")
                    .store(PG)
                    .status(REQUESTED)
                    .build(),
            SheetsRequest.builder()
                    .id(2L)
                    .timestamp(ZonedDateTime.now())
                    .name("Matti Meikäläinen")
                    .email("matti@example.com")
                    .competitionInfo("")
                    .competitionDate("10.6.2024")
                    .store(NBDG)
                    .status(REQUESTED)
                    .build(),
            SheetsRequest.builder()
                    .id(3L)
                    .timestamp(ZonedDateTime.now())
                    .name("Ville Vanhanen")
                    .email("ville@example.com")
                    .competitionInfo("")
                    .competitionDate("15.7.2024")
                    .store(PG)
                    .status(REQUESTED)
                    .build()
    );
}
