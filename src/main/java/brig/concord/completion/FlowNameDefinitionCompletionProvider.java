package brig.concord.completion;

import brig.concord.model.ObjectSchema;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.SelectionModel;

public class FlowNameDefinitionCompletionProvider implements SchemaCompletionProvider<ObjectSchema> {

    public static final FlowNameDefinitionCompletionProvider INSTANCE = new FlowNameDefinitionCompletionProvider();

    @Override
    public void addCompletions(CompletionContext ctx, ObjectSchema schema, CompletionResultSet result) {
        result.addElement(ctx.element("default")
                .withInsertHandler((context, item) -> {
                    Editor editor = context.getEditor();

                    EditorModificationUtil.insertStringAtCaret(editor, ":",
                            false, false, 0);

                    editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() - "default".length());
                    int start = editor.getCaretModel().getCurrentCaret().getOffset();
                    SelectionModel model = editor.getSelectionModel();
                    model.setSelection(start, start + "default".length());
                }));
    }
}
