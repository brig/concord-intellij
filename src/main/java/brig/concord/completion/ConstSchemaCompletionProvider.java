package brig.concord.completion;

import brig.concord.model.ConstSchema;
import com.intellij.codeInsight.completion.CompletionResultSet;

public class ConstSchemaCompletionProvider implements SchemaCompletionProvider<ConstSchema> {

    public static final ConstSchemaCompletionProvider INSTANCE = new ConstSchemaCompletionProvider();

    @Override
    public void addCompletions(CompletionContext ctx, ConstSchema schema, CompletionResultSet result) {
        result.addElement(ctx.element(schema.value().toString()));
    }
}
