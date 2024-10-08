package fi.bizhop.emailerrest;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import fi.bizhop.emailerrest.model.Code;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static fi.bizhop.emailerrest.model.Store.NBDG;
import static fi.bizhop.emailerrest.model.Store.PG;

public class Utils {
    private static final Logger LOG = LogManager.getLogger(Utils.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SIMPLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter SHEETS_IMPORT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy H.m.s");

    private static final DateTimeFormatter SHEETS_COMPETITION_DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy");
    private static final Pattern PG_CODE_PATTERN = Pattern.compile("^[0-9A-Z]{4}-[0-9A-Z]{4}-[0-9A-Z]{4}$");
    private static final Pattern NBDG_CODE_PATTERN = Pattern.compile("^[0-9a-z]{16}$");

    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES = List.of(SheetsScopes.SPREADSHEETS_READONLY, GmailScopes.GMAIL_MODIFY);

    public static String formatDateTime(TemporalAccessor input) {
        return DATE_TIME_FORMATTER.format(input);
    }

    public static TemporalAccessor parseSimpleDate(String input) { return SIMPLE_DATE_FORMATTER.parse(input); }

    public static ZonedDateTime parseCompetitionDate(String input) {
        var ta = SHEETS_COMPETITION_DATE_FORMATTER.parse(input);
        var localDate = LocalDate.from(ta);
        return ZonedDateTime.of(localDate.atTime(0, 0), ZoneId.systemDefault());
    }

    public static ZonedDateTime parseSheetsImportDateTime(Object input) {
        if(input instanceof String inputString) {
            var cleared = inputString.replace("klo ", "").trim();
            var ta = SHEETS_IMPORT_DATE_TIME_FORMATTER.parse(cleared);
            var localDateTime = LocalDateTime.from(ta);
            return ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        }

        return null;
    }

    public static List<Code> parsePgCodes(String input, String valid) {
        var scanner = new Scanner(input);
        var codes = new ArrayList<Code>();
        while(scanner.hasNextLine()) {
            var code = scanner.nextLine();
            if(PG_CODE_PATTERN.matcher(code).matches()) {
                codes.add(new Code(null, PG, code, valid, false));
            } else {
                LOG.error("Invalid PG code: {}", code);
            }
        }

        return codes;
    }

    public static List<Code> parseNbdgCodes(String input, String valid) {
        var scanner = new Scanner(input);
        var codes = new ArrayList<Code>();
        while(scanner.hasNextLine()) {
            var code = scanner.nextLine();
            if(NBDG_CODE_PATTERN.matcher(code).matches()) {
                codes.add(new Code(null, NBDG, code, valid, false));
            } else {
                LOG.error("Invalid NBDG code: {}", code);
            }
        }

        return codes;
    }

    public static GoogleCredentials getCredentialsFromToken(final String token) {
        var accessToken = new AccessToken(token.replace("Bearer ", ""), null);
        return GoogleCredentials.create(accessToken)
                .createScoped(SCOPES);
    }
}
