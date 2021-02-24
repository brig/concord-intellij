package brig.concord.model;

import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
public interface BooleanSchema extends Schema {

    static ImmutableBooleanSchema.Builder builder() {
        return ImmutableBooleanSchema.builder();
    }

    @Nullable
    Boolean defaultValue();

    default boolean canHandle(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    @Override
    default void accept(Visitor visitor) {
        visitor.visitBooleanSchema(this);
    }
}
