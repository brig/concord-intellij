package brig.concord.model;

import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Value.Immutable
public interface StringSchema extends Schema {

    static Builder builder() {
        return new Builder();
    }

    @Nullable
    Pattern pattern();

    @Nullable
    String customType();

    @Override
    default void accept(Visitor visitor) {
        visitor.visitStringSchema(this);
    }

    default ValueTypeHandler.Result canHandle(String value) {
        if (pattern() != null) {
            return ValueTypeHandler.Result.of(Objects.requireNonNull(pattern()).matcher(value).matches());
        }

        if (customType() != null) {
            return StringValueTypeHandlers.getInstance().get(customType()).canHandle(value);
        }

        return ValueTypeHandler.Result.success();
    }

    class Builder extends ImmutableStringSchema.Builder {

        public Builder pattern(String pattern) {
            pattern(Pattern.compile(pattern));
            return this;
        }
    }

    class StringValueTypeHandlers extends ValueTypeHandlers<String> {

        public static StringValueTypeHandlers getInstance() {
            return INSTANCE;
        }

        private static final StringValueTypeHandlers INSTANCE = new StringValueTypeHandlers();

        public StringValueTypeHandlers() {
            super(initHandlers());
        }

        private static Map<String, ValueTypeHandler<String>> initHandlers() {
            Map<String, ValueTypeHandler<String>> m = new HashMap<>();
            m.put(ValueTypes.DURATION, DurationValueHandler.INSTANCE);
            m.put(ValueTypes.REGEXP, RegexpValueHandler.INSTANCE);
            return m;
        }
    }
}
