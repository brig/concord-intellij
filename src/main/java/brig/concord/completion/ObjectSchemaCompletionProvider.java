package brig.concord.completion;

import brig.concord.model.*;
import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;

import javax.swing.*;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectSchemaCompletionProvider implements SchemaCompletionProvider<ObjectSchema> {

    public static final ObjectSchemaCompletionProvider INSTANCE = new ObjectSchemaCompletionProvider();

    private static Icon getIcon(Schema schema) {
        SchemaIconVisitor visitor = new SchemaIconVisitor();
        visitor.visit(schema);
        return visitor.icon();
    }

    private static String getTypeText(Schema schema) {
        return SchemaUtils.valueType(schema).stream()
                .sorted()
                .collect(Collectors.joining(" | "));
    }

    private static InsertHandler<LookupElement> createPropertyInsertHandler(Schema valueSchema) {
        return (context, item) -> {
            ApplicationManager.getApplication().assertWriteAccessAllowed();

            Editor editor = context.getEditor();

            String currentPropIndent = CompletionUtils.contentIndent(CompletionUtils.currentLineContent(editor));
            ValueInsertHandlerVisitor visitor = new ValueInsertHandlerVisitor(context, false, currentPropIndent);
            visitor.visit(valueSchema);

            PsiDocumentManager.getInstance(context.getProject()).commitDocument(editor.getDocument());
        };
    }

    @Override
    public void addCompletions(CompletionContext ctx, ObjectSchema schema, CompletionResultSet result) {
        CompletionParameters parameters = ctx.parameters();
        PsiElement completionPosition = parameters.getOriginalPosition() != null ? parameters.getOriginalPosition() :
                parameters.getPosition();

        String propPrefix = "";
        Editor editor = ctx.parameters().getEditor();
        String line = CompletionUtils.currentLineContent(editor);
        if (line != null && line.contains(":")) {
            String currentPropIndent = CompletionUtils.contentIndent(line);
            propPrefix = "\n" + currentPropIndent + CompletionUtils.indent(ctx.parameters().getOriginalFile());
        }
        String postfix = ": ";

        Set<String> currentProps = ConcordYamlPsiUtils.getPropertyNamesOfParentObject(completionPosition, parameters.getPosition());
        for (Map.Entry<String, Schema> e : schema.properties().entrySet()) {
            if (currentProps.contains(e.getKey())) {
                continue;
            }

            result.addElement(LookupElementBuilder.create(CompletionItem.of(propPrefix + e.getKey() + postfix, e.getValue()))
                    .withPresentableText(e.getKey())
                    .withIcon(getIcon(e.getValue()))
                    .withTypeText(getTypeText(e.getValue()), true)
                    .withInsertHandler(createPropertyInsertHandler(e.getValue())));
        }
    }

    private static class SchemaIconVisitor extends AbstractVisitor {

        private Icon icon = AllIcons.Nodes.Property;

        public Icon icon() {
            return icon;
        }

        @Override
        protected void visitObjectSchema(ObjectSchema schema) {
            this.icon = AllIcons.Json.Object;
        }

        @Override
        protected void visitArraySchema(ArraySchema schema) {
            this.icon = AllIcons.Json.Array;
        }

        @Override
        protected void visitConstSchema(ConstSchema schema) {
            this.icon = null;
        }
    }
}
