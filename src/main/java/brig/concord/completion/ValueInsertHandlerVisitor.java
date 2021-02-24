package brig.concord.completion;

import brig.concord.model.*;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.SelectionModel;

import java.util.Iterator;

public class ValueInsertHandlerVisitor extends Visitor {

    private final InsertionContext context;
    private final boolean batchMode;
    private final String currentIndent;

    public ValueInsertHandlerVisitor(InsertionContext context, boolean batchMode, String currentIndent) {
        this.context = context;
        this.batchMode = batchMode;
        this.currentIndent = currentIndent;
    }

    @Override
    protected void visitBooleanSchema(BooleanSchema schema) {
        if (schema.defaultValue() == null) {
            return;
        }

        String stringToInsert = String.valueOf(schema.defaultValue());

        Editor editor = context.getEditor();
        EditorModificationUtil.insertStringAtCaret(editor, stringToInsert,
                false, true,
                stringToInsert.length());

        if (!batchMode) {
            int start = editor.getSelectionModel().getSelectionStart();
            SelectionModel model = editor.getSelectionModel();
            model.setSelection(start - stringToInsert.length(), start);
            AutoPopupController.getInstance(context.getProject()).autoPopupMemberLookup(context.getEditor(), null);
        }
    }

    @Override
    protected void visitConstSchema(ConstSchema schema) {
        Editor editor = context.getEditor();
        String stringToInsert = String.valueOf(schema.value());

        EditorModificationUtil.insertStringAtCaret(editor, stringToInsert,
                false, true,
                stringToInsert.length());
    }

    @Override
    protected void visitStringSchema(StringSchema schema) {
        // do nothing
    }

    @Override
    public void visitIntSchema(IntSchema schema) {
        // do nothing
    }

    @Override
    protected void visitObjectSchema(ObjectSchema schema) {
        insertRequiredProps(context, schema);
    }

    private void insertRequiredProps(InsertionContext context, ObjectSchema schema) {
        Editor editor = context.getEditor();

        String currentPropIndent = currentIndent + CompletionUtils.indent(context);
        String newLineToInsert = "\n" + currentPropIndent;

        String line = CompletionUtils.currentLineContent(editor);
        if (line != null && line.contains(":")) {
            EditorModificationUtil.insertStringAtCaret(editor, newLineToInsert,
                    false, true, newLineToInsert.length());
        }

        for (Iterator<String> it = schema.requiredProperties().iterator(); it.hasNext(); ) {
            String reqProp = it.next();
            Schema reqPropSchema = schema.property(reqProp);
            String stringToInsert = reqProp + ": ";

            EditorModificationUtil.insertStringAtCaret(editor, stringToInsert,
                    false, true, stringToInsert.length());

            ValueInsertHandlerVisitor propsVisitor = new ValueInsertHandlerVisitor(context, true, currentPropIndent);
            propsVisitor.visit(reqPropSchema);

            if (it.hasNext()) {
                EditorModificationUtil.insertStringAtCaret(editor, newLineToInsert,
                        false, true, newLineToInsert.length());
            }
        }
    }

    @Override
    protected void visitArraySchema(ArraySchema schema) {
        Editor editor = context.getEditor();

        String newLineToInsert = "\n" + currentIndent + CompletionUtils.indent(context);
        EditorModificationUtil.insertStringAtCaret(editor, newLineToInsert,
                false, true, newLineToInsert.length());
        EditorModificationUtil.insertStringAtCaret(editor, "- ");

        // TODO: process elements
    }

    @Override
    protected void visitCombinedSchema(CombinedSchema schema) {
        // do nothing
    }

    @Override
    public void visitAnyValueSchema(Schema schema) {
        // do nothing
    }
}