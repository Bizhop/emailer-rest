package fi.bizhop.emailerrest;

import fi.bizhop.emailerrest.model.Code;

import java.util.List;

import static fi.bizhop.emailerrest.model.Store.NBDG;
import static fi.bizhop.emailerrest.model.Store.PG;

public class TestObjects{
    public static final List<Code> PG_CODES = List.of(
            new Code(1L, PG, "ABCD", "11/2023", false),
            new Code(2L, PG, "EFGH", "11/2023", false));
    public static final List<Code> NBDG_CODES = List.of(
            new Code(3L, NBDG, "abcd", "1.11.2023", false),
            new Code(4L, NBDG, "efgh", "1.11.2023", false));
}
