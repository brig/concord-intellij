package brig.concord.completion;

import brig.concord.model.Schema;
import com.intellij.codeInsight.completion.CompletionResultSet;

public interface SchemaCompletionProvider<T extends Schema> {

    void addCompletions(CompletionContext ctx, T schema, CompletionResultSet result);
}
