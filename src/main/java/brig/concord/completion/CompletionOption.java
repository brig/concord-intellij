package brig.concord.completion;

import org.immutables.value.Value;

import javax.annotation.Nullable;
import javax.swing.*;

@Value.Immutable
public interface CompletionOption {

    static ImmutableCompletionOption.Builder builder() {
        return ImmutableCompletionOption.builder();
    }

    static CompletionOption from(String value, Icon icon) {
        return builder()
                .value(value)
                .icon(icon)
                .build();
    }

    String value();

    @Nullable
    Icon icon();
}
