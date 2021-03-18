package brig.concord.model;

import java.time.Duration;

public class DurationValueHandler implements ValueTypeHandler<String> {

    public static final DurationValueHandler INSTANCE = new DurationValueHandler();

    @Override
    public Result canHandle(String value) {
        try {
            Duration.parse(value);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
