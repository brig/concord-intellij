package brig.concord.codeinsight;

import brig.concord.ConcordBundle;
import brig.concord.model.*;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import org.jetbrains.yaml.psi.*;

import java.util.stream.Collectors;

public class TypeCheckVisitor extends DefaultFailVisitor {

    private final YAMLValue value;
    private final AnnotationHolder annotationHolder;

    public TypeCheckVisitor(YAMLValue value, AnnotationHolder annotationHolder) {
        this.value = value;
        this.annotationHolder = annotationHolder;
    }

    @Override
    protected void visitBooleanSchema(BooleanSchema schema) {
        if (value instanceof YAMLScalar) {
            YAMLScalar scalar = (YAMLScalar) value;
            if (schema.canHandle(scalar.getTextValue())) {
                return;
            }
        }

        annotationHolder.newAnnotation(HighlightSeverity.ERROR,
                ConcordBundle.message("annotator.type.check.expected.boolean"))
                .create();
    }

    @Override
    protected void visitStringSchema(StringSchema schema) {
        ValueTypeHandler.Result result = null;
        if (value instanceof YAMLScalar) {
            result = schema.canHandle(((YAMLScalar) value).getTextValue());
            if (result.ok()) {
                return;
            }
        }

        String type = schema.customType() != null ? schema.customType() : ValueTypes.STRING;

        if (result != null && result.error() != null) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR,
                    ConcordBundle.message("annotator.type.check.expected.typed.string.error", type, result.error()))
                    .create();
        } else {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR,
                    ConcordBundle.message("annotator.type.check.expected.typed.string", type))
                    .create();
        }
    }

    @Override
    public void visitIntSchema(IntSchema schema) {
        if (value instanceof YAMLScalar) {
            YAMLScalar scalar = (YAMLScalar) value;
            if (schema.canHandle(scalar.getTextValue())) {
                return;
            }
        }

        annotationHolder.newAnnotation(HighlightSeverity.ERROR,
                ConcordBundle.message("annotator.type.check.expected.int"))
                .create();
    }

    @Override
    protected void visitArraySchema(ArraySchema arraySchema) {
        if (value instanceof YAMLSequence) {
            // TODO
//            for (YAMLSequenceItem item : ((YAMLSequence) value).getItems()) {
//            }

            return;
        }

        annotationHolder.newAnnotation(HighlightSeverity.ERROR,
                ConcordBundle.message("annotator.type.check.expected.array"))
                .create();
    }

    @Override
    protected void visitConstSchema(ConstSchema constSchema) {
        if (value instanceof YAMLScalar) {
            YAMLScalar scalar = (YAMLScalar) value;
            if (constSchema.canHandle(scalar.getTextValue())) {
                return;
            }
        }

        annotationHolder.newAnnotation(HighlightSeverity.ERROR,
                ConcordBundle.message("annotator.type.check.expected.const", constSchema.value()))
                .create();
    }

    @Override
    protected void visitObjectSchema(ObjectSchema objectSchema) {
        if (value instanceof YAMLMapping) {
            return;
        }

        annotationHolder.newAnnotation(HighlightSeverity.ERROR,
                ConcordBundle.message("annotator.type.check.expected.object"))
                .create();
    }

    @Override
    protected void visitCombinedSchema(CombinedSchema schema) {
        String types = SchemaUtils.valueType(schema).stream()
                .sorted()
                .collect(Collectors.joining(" or "));

        annotationHolder.newAnnotation(HighlightSeverity.ERROR,
                ConcordBundle.message("annotator.type.check.expected.oneOf", types))
                .create();
    }

    @Override
    public void visitAnyValueSchema(Schema schema) {
        // do nothing
    }
}
