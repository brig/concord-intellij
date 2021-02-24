package brig.concord.completion;

import brig.concord.model.BooleanSchema;
import com.intellij.codeInsight.completion.CompletionResultSet;

public class BooleanSchemaCompletionProvider implements SchemaCompletionProvider<BooleanSchema> {

    public static final BooleanSchemaCompletionProvider INSTANCE = new BooleanSchemaCompletionProvider();

    @Override
    public void addCompletions(CompletionContext ctx, BooleanSchema schema, CompletionResultSet result) {
        result.addElement(ctx.element("true"));
        result.addElement(ctx.element("false"));
    }
}
