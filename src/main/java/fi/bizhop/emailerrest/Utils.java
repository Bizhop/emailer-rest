package fi.bizhop.emailerrest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class Utils {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDate(TemporalAccessor input) {
        return formatter.format(input);
    }

    public static long toEpoch(LocalDateTime ltd) {
        return ltd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
