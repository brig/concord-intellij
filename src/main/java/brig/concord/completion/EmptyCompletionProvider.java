package brig.concord.completion;

import brig.concord.model.Schema;
import com.intellij.codeInsight.completion.CompletionResultSet;

public class EmptyCompletionProvider implements SchemaCompletionProvider<Schema> {

    public static final EmptyCompletionProvider INSTANCE = new EmptyCompletionProvider();

    @Override
    public void addCompletions(CompletionContext ctx, Schema schema, CompletionResultSet result) {
        // do nothing
    }
}
