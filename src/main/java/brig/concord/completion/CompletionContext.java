package brig.concord.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import org.immutables.value.Value;

@Value.Immutable
public interface CompletionContext {

    static ImmutableCompletionContext.Builder builder() {
        return ImmutableCompletionContext.builder();
    }

    static CompletionContext from(CompletionParameters parameters) {
        return builder()
                .parameters(parameters)
                .build();
    }

    CompletionParameters parameters();

    default LookupElementBuilder element(String lookupString) {
        return LookupElementBuilder.create(lookupString).bold();
    }

    default LookupElementBuilder element(CompletionOption lookupOption) {
        LookupElementBuilder builder = element(lookupOption.value());
        if (lookupOption.icon() != null) {
            builder = builder.withIcon(lookupOption.icon());
        }
        return builder;
    }
}
