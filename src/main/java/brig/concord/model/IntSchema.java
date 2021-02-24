package brig.concord.model;

import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
public interface IntSchema extends Schema {

    static ImmutableIntSchema.Builder builder() {
        return ImmutableIntSchema.builder();
    }

    @Nullable
    Integer defaultValue();

    default boolean canHandle(String value) {
        try {
            Integer.valueOf(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    default void accept(Visitor visitor) {
        visitor.visitIntSchema(this);
    }
}
