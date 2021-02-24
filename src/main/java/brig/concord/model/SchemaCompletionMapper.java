package brig.concord.model;

import brig.concord.completion.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SchemaCompletionMapper {

    public static final SchemaCompletionMapper INSTANCE = new SchemaCompletionMapper();
    private final Map<String, SchemaCompletionProvider<? extends Schema>> customProviders = new HashMap<>();

    public static <T extends Schema> void register(T schema, SchemaCompletionProvider<T> provider) {
        Objects.requireNonNull(schema.id(), "schema.id must not be null");

        register(schema.id(), provider);
    }

    public static <T extends Schema> void register(String schemaId, SchemaCompletionProvider<T> provider) {
        INSTANCE.customProviders.put(schemaId, provider);
    }

    @SuppressWarnings("unchecked")
    public <T extends Schema> SchemaCompletionProvider<T> get(T schema) {
        SchemaCompletionProvider<T> result = null;
        if (schema.id() != null) {
            result = (SchemaCompletionProvider<T>) customProviders.get(schema.id());
        }
        if (result != null) {
            return result;
        }
        DefaultProviderVisitor visitor = new DefaultProviderVisitor();
        visitor.visit(schema);
        return (SchemaCompletionProvider<T>) visitor.get();
    }

    static class DefaultProviderVisitor extends Visitor {

        private SchemaCompletionProvider<? extends Schema> result = EmptyCompletionProvider.INSTANCE;

        public SchemaCompletionProvider<? extends Schema> get() {
            return result;
        }

        @Override
        protected void visitBooleanSchema(BooleanSchema schema) {
            result = BooleanSchemaCompletionProvider.INSTANCE;
        }

        @Override
        protected void visitIntSchema(IntSchema schema) {
            // do nothing
        }

        @Override
        protected void visitStringSchema(StringSchema schema) {
            // do nothing
        }

        @Override
        protected void visitConstSchema(ConstSchema schema) {
            result = ConstSchemaCompletionProvider.INSTANCE;
        }

        @Override
        protected void visitObjectSchema(ObjectSchema schema) {
            result = ObjectSchemaCompletionProvider.INSTANCE;
        }

        @Override
        protected void visitArraySchema(ArraySchema schema) {
            // do nothing
        }

        @Override
        protected void visitCombinedSchema(CombinedSchema schema) {
            result = CombinedSchemaCompletionProvider.INSTANCE;
        }

        @Override
        protected void visitAnyValueSchema(Schema schema) {
        }
    }
}
