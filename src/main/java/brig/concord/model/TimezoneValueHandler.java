package brig.concord.model;

import java.util.Arrays;
import java.util.TimeZone;

public class TimezoneValueHandler implements ValueTypeHandler<String> {

    public static final TimezoneValueHandler INSTANCE = new TimezoneValueHandler();

    @Override
    public Result canHandle(String value) {
        boolean valid = Arrays.asList(TimeZone.getAvailableIDs()).contains(value);
        if (valid) {
            return Result.success();
        } else {
            return Result.fail("Unknown timezone: " + value);
        }
    }
}
