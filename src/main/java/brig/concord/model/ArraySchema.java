package brig.concord.model;

import org.immutables.value.Value;

@Value.Immutable
public interface ArraySchema extends Schema {

    static ImmutableArraySchema.Builder builder() {
        return ImmutableArraySchema.builder();
    }

    static ImmutableArraySchema.Builder withItem(Schema schema) {
        return builder()
                .itemSchema(schema);
    }

    static Schema anyArray(String description) {
        return ArraySchema.builder()
                .description(description)
                .itemSchema(ANY)
                .build();
    }

    Schema itemSchema();

    @Override
    default void accept(Visitor visitor) {
        visitor.visitArraySchema(this);
    }
}
