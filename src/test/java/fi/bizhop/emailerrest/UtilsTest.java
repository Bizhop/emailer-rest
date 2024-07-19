package fi.bizhop.emailerrest;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {
    @Test
    void parsePgCodesTest() throws IOException {
        var pgCodes = FileUtils.readFileToString(new File("src/test/resources/pgCodes.txt"), Charset.defaultCharset());
        var codes = Utils.parsePgCodes(pgCodes, "07/2024");
        assertEquals(20, codes.size());
    }

    @Test
    void parsePgCodesDiscardNonMatchingByRexepTest() throws IOException {
        var pgCodes = FileUtils.readFileToString(new File("src/test/resources/pgCodesInvalid.txt"), Charset.defaultCharset());
        var codes = Utils.parsePgCodes(pgCodes, "07/2024");
        assertEquals(1, codes.size());
    }

    @Test
    void parseNbdgCodesTest() throws IOException {
        var nbdgCodes = FileUtils.readFileToString(new File("src/test/resources/nbdgCodes.txt"), Charset.defaultCharset());
        var codes = Utils.parseNbdgCodes(nbdgCodes, "30.9.2022");
        assertEquals(10, codes.size());
    }

    @Test
    void parseNbdgCodesDiscardNonMatchingByRexepTest() throws IOException {
        var nbdgCodes = FileUtils.readFileToString(new File("src/test/resources/nbdgCodesInvalid.txt"), Charset.defaultCharset());
        var codes = Utils.parseNbdgCodes(nbdgCodes, "30.9.2022");
        assertEquals(1, codes.size());
    }

    @Test
    void parseCompetitionDate() {
        var date = Utils.parseCompetitionDate("30.9.2022");
        assertEquals(ZonedDateTime.of(2022, 9, 30, 0, 0, 0, 0, ZoneId.systemDefault()), date);
    }
}
