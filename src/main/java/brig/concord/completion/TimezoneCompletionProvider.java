package brig.concord.completion;

import brig.concord.model.StringSchema;
import com.intellij.codeInsight.completion.CompletionResultSet;

import java.util.TimeZone;

public class TimezoneCompletionProvider implements SchemaCompletionProvider<StringSchema> {

    @Override
    public void addCompletions(CompletionContext ctx, StringSchema schema, CompletionResultSet result) {
        for (String tz : TimeZone.getAvailableIDs()) {
            result.addElement(ctx.element(tz));
        }
    }
}
