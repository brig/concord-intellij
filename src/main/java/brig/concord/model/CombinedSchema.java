package brig.concord.model;

import org.immutables.value.Value;

import java.util.Arrays;
import java.util.Collection;

@Value.Immutable
public interface CombinedSchema extends Schema {

    static ImmutableCombinedSchema.Builder builder() {
        return ImmutableCombinedSchema.builder();
    }

    static ImmutableCombinedSchema.Builder oneOf(Schema... schemas) {
        return oneOf(Arrays.asList(schemas));
    }

    static ImmutableCombinedSchema.Builder oneOf(Collection<Schema> schemas) {
        return builder().subSchemas(schemas).criterion(Criterion.ONE);
    }

    Collection<Schema> subSchemas();

    Criterion criterion();

    @Override
    default void accept(Visitor visitor) {
        visitor.visitCombinedSchema(this);
    }

    enum Criterion {
        ONE
    }
}
