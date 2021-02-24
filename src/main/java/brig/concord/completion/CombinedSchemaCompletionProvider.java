package brig.concord.completion;

import brig.concord.model.*;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;

import java.util.*;

public class CombinedSchemaCompletionProvider implements SchemaCompletionProvider<CombinedSchema> {

    public static final CombinedSchemaCompletionProvider INSTANCE = new CombinedSchemaCompletionProvider();

    private final Map<Schema, SchemaCompletionProvider<Schema>> customProviders = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Schema> CombinedSchemaCompletionProvider withCustomCompletion(T schema, SchemaCompletionProvider<T> provider) {
        customProviders.put(schema, (SchemaCompletionProvider<Schema>) provider);
        return this;
    }

    @Override
    public void addCompletions(CompletionContext ctx, CombinedSchema schema, CompletionResultSet result) {
        new CombinedSchemaCompletionVisitor(customProviders, ctx, result)
                .visit(schema);
    }

    static class CombinedSchemaCompletionVisitor extends AbstractVisitor {

        private final Map<Schema, SchemaCompletionProvider<Schema>> customProviders;
        private final CompletionContext ctx;
        private final CompletionResultSet result;

        public CombinedSchemaCompletionVisitor(Map<Schema, SchemaCompletionProvider<Schema>> customProviders, CompletionContext ctx, CompletionResultSet result) {
            this.customProviders = customProviders;
            this.ctx = ctx;
            this.result = result;
        }

        @Override
        protected void visitCombinedSchema(CombinedSchema combinedSchema) {
            if (combinedSchema.criterion() != CombinedSchema.Criterion.ONE) {
                throw new RuntimeException("Unsupported criterion:" + combinedSchema.criterion());
            }

            for (Schema s : combinedSchema.subSchemas()) {
                SchemaCompletionProvider<Schema> provider = customProviders.get(s);
                if (provider != null) {
                    provider.addCompletions(ctx, s, result);
                } else {
                    visit(s);
                }
            }
        }

        @Override
        protected void visitConstSchema(ConstSchema schema) {
            SchemaUtils.completionProvider(schema)
                    .addCompletions(ctx, schema, result);
        }

        @Override
        protected void visitStringSchema(StringSchema schema) {
            SchemaUtils.completionProvider(schema)
                    .addCompletions(ctx, schema, result);
        }

        @Override
        public void visitAnyValueSchema(Schema schema) {
            // do nothing
        }

        @Override
        public void visitIntSchema(IntSchema schema) {
            // do nothing
        }

        @Override
        protected void visitObjectSchema(ObjectSchema schema) {
            ObjectCompletionProvider.INSTANCE
                    .addCompletions(ctx, schema, result);
        }
    }

    public static class ObjectCompletionProvider implements SchemaCompletionProvider<ObjectSchema> {

        public static final ObjectCompletionProvider INSTANCE = new ObjectCompletionProvider();

        @Override
        public void addCompletions(CompletionContext ctx, ObjectSchema schema, CompletionResultSet result) {
            Collection<String> options = schema.requiredProperties();
            for (String p : options) {
                result.addElement(ctx.element(p).withInsertHandler(OptionsCompletionProvider.createPropertyInsertHandler(p, schema)));
            }
        }
    }

    public static class OptionsCompletionProvider implements SchemaCompletionProvider<ObjectSchema> {

        private final List<CompletionOption> options;

        public OptionsCompletionProvider(List<CompletionOption> options) {
            this.options = options;
        }

        public static SchemaCompletionProvider<ObjectSchema> from(CompletionOption option) {
            return new OptionsCompletionProvider(Collections.singletonList(option));
        }

        private static InsertHandler<LookupElement> createPropertyInsertHandler(String prop, Schema valueSchema) {
            return (context, item) -> {
                ApplicationManager.getApplication().assertWriteAccessAllowed();

                Editor editor = context.getEditor();

                context.getDocument().deleteString(editor.getCaretModel().getOffset() - prop.length(), editor.getCaretModel().getOffset());

                String currentPropIndent = CompletionUtils.contentIndent(CompletionUtils.currentLineContent(editor));
                ValueInsertHandlerVisitor visitor = new ValueInsertHandlerVisitor(context, false, currentPropIndent);
                visitor.visit(valueSchema);

                PsiDocumentManager.getInstance(context.getProject()).commitDocument(editor.getDocument());
            };
        }

        @Override
        public void addCompletions(CompletionContext ctx, ObjectSchema schema, CompletionResultSet result) {
            for (CompletionOption p : options) {
                result.addElement(ctx.element(p).withInsertHandler(createPropertyInsertHandler(p.value(), schema)));
            }
        }
    }
}
