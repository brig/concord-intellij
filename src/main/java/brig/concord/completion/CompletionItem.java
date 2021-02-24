package brig.concord.completion;

import brig.concord.model.Schema;
import org.immutables.value.Value;

@Value.Immutable
public abstract class CompletionItem {

    public static CompletionItem of(String value, Schema schema) {
        return ImmutableCompletionItem.builder()
                .value(value)
                .schema(schema)
                .build();
    }

    public abstract String value();

    public abstract Schema schema();

    @Override
    public String toString() {
        return value();
    }
}
