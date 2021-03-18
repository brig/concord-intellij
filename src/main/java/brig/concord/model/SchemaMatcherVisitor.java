package brig.concord.model;

import com.intellij.codeInsight.completion.CompletionUtilCore;
import org.jetbrains.yaml.psi.*;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Match key-values and choose combined schema
 */
public class SchemaMatcherVisitor extends Visitor {

    private final CombinedSchemaMatcher combinedSchemaMatcher;
    private final YAMLPsiElement element;
    private Schema schema;
    public SchemaMatcherVisitor(YAMLPsiElement element) {
        this(element, new CombinedSchemaMatcherVisitor(element));
    }

    public SchemaMatcherVisitor(YAMLPsiElement element, CombinedSchemaMatcher combinedSchemaMatcher) {
        this.element = element;
        this.combinedSchemaMatcher = combinedSchemaMatcher;
    }

    private static String getText(YAMLScalar scalar) {
        return clearText(scalar.getTextValue());
    }

    public static String clearText(String str) {
        return str.replaceFirst(CompletionUtilCore.DUMMY_IDENTIFIER, "")
                .replaceFirst(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "")
                .trim();
    }

    @Override
    protected void visitObjectSchema(ObjectSchema schema) {
        this.schema = null;
        if (element instanceof YAMLDocument) {
            this.schema = schema;
        } else if (element instanceof YAMLMapping) {
            this.schema = schema;
        } else if (element instanceof YAMLKeyValue) {
            this.schema = schema.property(((YAMLKeyValue) element).getKeyText());
        } else if (element instanceof YAMLScalar) {
            if (((YAMLScalar) element).getTextValue().contains(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)) {
                this.schema = schema;
            }
        }
    }

    @Override
    protected void visitArraySchema(ArraySchema schema) {
        this.schema = null;
        if (element instanceof YAMLSequence) {
            this.schema = schema;
        } else if (element instanceof YAMLSequenceItem) {
            this.schema = schema.itemSchema();
        }
    }

    @Override
    protected void visitCombinedSchema(CombinedSchema schema) {
        if (element instanceof YAMLScalar) {
            if ("".equals(getText((YAMLScalar) element))) {
                this.schema = schema;
                return;
            }
        }

        combinedSchemaMatcher.visit(schema);
        this.schema = combinedSchemaMatcher.schema();
    }

    @Override
    protected void visitConstSchema(ConstSchema schema) {
        if (element instanceof YAMLScalar) {
            this.schema = schema;
        }
    }

    @Override
    protected void visitStringSchema(StringSchema schema) {
        if (element instanceof YAMLScalar) {
            this.schema = schema;
        }
    }

    @Override
    protected void visitBooleanSchema(BooleanSchema schema) {
        if (element instanceof YAMLScalar) {
            this.schema = schema;
        }
    }

    @Override
    public void visitIntSchema(IntSchema schema) {
        if (element instanceof YAMLScalar) {
            this.schema = schema;
        }
    }

    @Override
    public void visitAnyValueSchema(Schema schema) {
        this.schema = schema;
    }

    public Schema schema() {
        return schema;
    }

    public static abstract class CombinedSchemaMatcher extends Visitor {

        protected Schema schema;

        public abstract Schema schema();
    }

    public static class CombinedSchemaMatcherVisitor extends CombinedSchemaMatcher {

        protected final YAMLPsiElement element;

        protected CombinedSchemaMatcherVisitor(YAMLPsiElement element) {
            this.element = element;
        }

        @Override
        protected void visitCombinedSchema(CombinedSchema schema) {
            this.schema = null;

            if (schema.criterion() != CombinedSchema.Criterion.ONE) {
                throw new RuntimeException("Unsupported criterion:" + schema.criterion());
            }

            CombinedSchemaMatcherVisitor visitor = new CombinedSchemaMatcherVisitor(element);
            for (Schema s : schema.subSchemas()) {
                visitor.visit(s);
                if (visitor.schema() != null) {
                    this.schema = visitor.schema();
                    break;
                }
            }
        }

        @Override
        protected void visitAnyValueSchema(Schema schema) {
            this.schema = schema;
        }

        @Override
        protected void visitConstSchema(ConstSchema schema) {
            this.schema = null;

            if (element instanceof YAMLScalar) {
                if (schema.canHandle(getText((YAMLScalar) element))) {
                    this.schema = schema;
                }
            }
        }

        @Override
        public void visitIntSchema(IntSchema schema) {
            this.schema = null;

            if (element instanceof YAMLScalar) {
                if (schema.canHandle(getText((YAMLScalar) element))) {
                    this.schema = schema;
                }
            }
        }

        @Override
        protected void visitStringSchema(StringSchema schema) {
            this.schema = null;

            if (element instanceof YAMLScalar) {
                if (schema.canHandle(getText((YAMLScalar) element)).ok()) {
                    this.schema = schema;
                }
            }
        }

        @Override
        protected void visitObjectSchema(ObjectSchema schema) {
            this.schema = null;
            if (!(element instanceof YAMLMapping)) {
                return;
            }

            if (schema.requiredProperties().isEmpty() && !schema.additionalProperties()) {
                throw new RuntimeException("Object Schema without required properties: " + schema);
            }

            if (!schema.requiredProperties().isEmpty()) {
                YAMLMapping object = (YAMLMapping) element;
                List<String> currentProperties = object.getKeyValues().stream()
                        .map(k -> clearText(k.getKeyText()))
                        .collect(Collectors.toList());
                Set<String> requiredProps = new HashSet<>(schema.requiredProperties());
                requiredProps.removeAll(currentProperties);
                if (requiredProps.isEmpty()) {
                    this.schema = schema;
                    return;
                }
            }

            if (schema.schemaOfAdditionalProperties() != null) {
                this.schema = schema;
//                this.visit(Objects.requireNonNull(schema.schemaOfAdditionalProperties()));
            }
        }

        @Override
        protected void visitArraySchema(ArraySchema schema) {
            this.schema = null;
            if (!(element instanceof YAMLSequence)) {
                return;
            }

            this.schema = schema;
        }

        @Override
        protected void visitBooleanSchema(BooleanSchema schema) {
            // TODO
        }

        @Override
        public Schema schema() {
            return schema;
        }
    }
}
