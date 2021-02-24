package brig.concord.completion;

import brig.concord.model.StringSchema;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;

public class ExpressionCompletionProvider implements SchemaCompletionProvider<StringSchema> {

    public static final ExpressionCompletionProvider INSTANCE = new ExpressionCompletionProvider();

    @Override
    public void addCompletions(CompletionContext context, StringSchema schema, CompletionResultSet result) {
        result.addElement(context.element("${...}")
                .withInsertHandler((ctx, item) -> {
                    Editor editor = ctx.getEditor();
                    editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() - "...}".length());
                    int start = editor.getCaretModel().getCurrentCaret().getOffset();
                    SelectionModel model = editor.getSelectionModel();
                    model.setSelection(start, start + "...".length());
                }));
    }
}
