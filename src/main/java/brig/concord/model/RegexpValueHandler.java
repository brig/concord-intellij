package brig.concord.model;

import java.util.regex.Pattern;

public class RegexpValueHandler implements ValueTypeHandler<String> {

    public static final RegexpValueHandler INSTANCE = new RegexpValueHandler();

    @Override
    public Result canHandle(String value) {
        try {
            Pattern.compile(value);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
