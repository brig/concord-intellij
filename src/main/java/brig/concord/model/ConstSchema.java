package brig.concord.model;

import org.immutables.value.Value;

@Value.Immutable
public interface ConstSchema extends Schema {

    static Builder builder() {
        return new Builder();
    }

    Object value();

    ValueType valueType();

    default boolean canHandle(String value) {
        switch (valueType()) {
            case STRING:
                return value.equals(value());
            case INT:
                try {
                    return value().equals(Integer.valueOf(value));
                } catch (Exception e) {
                    return false;
                }
            default:
                throw new IllegalArgumentException("Unknown value type: " + valueType());
        }
    }

    @Override
    default void accept(Visitor visitor) {
        visitor.visitConstSchema(this);
    }

    enum ValueType {
        STRING,
        INT
    }

    class Builder extends ImmutableConstSchema.Builder {

        public Builder value(String value) {
            return super.value(value)
                    .valueType(ValueType.STRING);
        }

        public Builder value(int value) {
            return super.value(value)
                    .valueType(ValueType.INT);
        }
    }
}
