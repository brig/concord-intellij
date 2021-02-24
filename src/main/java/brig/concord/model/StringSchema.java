package brig.concord.model;

import org.immutables.value.Value;

import javax.annotation.Nullable;
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

    default boolean canHandle(String value) {
        if (pattern() == null) {
            return true;
        }

        return Objects.requireNonNull(pattern()).matcher(value).matches();
    }

    class Builder extends ImmutableStringSchema.Builder {

        public Builder pattern(String pattern) {
            pattern(Pattern.compile(pattern));
            return this;
        }
    }
}
