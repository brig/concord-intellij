package brig.concord.completion;

import brig.concord.model.*;
import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.util.ThreeState;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLPsiElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static brig.concord.model.SchemaMatcherVisitor.clearText;

public class CombinedSchemaCompletionMatcherVisitor extends SchemaMatcherVisitor.CombinedSchemaMatcherVisitor {

    private ThreeState match;

    public CombinedSchemaCompletionMatcherVisitor(YAMLPsiElement element) {
        super(element);
    }

    @Override
    public void visit(Schema schema) {
        this.schema = null;
        this.match = null;

        super.visit(schema);

        if (this.schema != null && this.match == null) {
            this.match = ThreeState.YES;
        }
    }

    private ThreeState match() {
        return match;
    }

    @Override
    protected void visitCombinedSchema(CombinedSchema schema) {
        this.schema = null;

        if (schema.criterion() != CombinedSchema.Criterion.ONE) {
            throw new RuntimeException("Unsupported criterion:" + schema.criterion());
        }

        List<Schema> partialMatch = new ArrayList<>();
        CombinedSchemaCompletionMatcherVisitor visitor = new CombinedSchemaCompletionMatcherVisitor(element);
        for (Schema s : schema.subSchemas()) {
            visitor.visit(s);
            if (visitor.match() == ThreeState.YES) {
                this.schema = visitor.schema;
                return;
            } else if (visitor.match() == ThreeState.UNSURE) {
                partialMatch.add(visitor.schema);
            }
        }

        if (!partialMatch.isEmpty()) {
            if (partialMatch.size() == 1) {
                this.schema = partialMatch.get(0);
            } else {
                this.schema = CombinedSchema.oneOf(partialMatch).id(schema.id()).build();
            }
        }
    }

    @Override
    protected void visitObjectSchema(ObjectSchema schema) {
        this.schema = null;
        this.match = ThreeState.NO;

        YAMLPsiElement effectiveElement = element;
        if (!(effectiveElement instanceof YAMLMapping)) {
            effectiveElement = ConcordYamlPsiUtils.getParentOfType(this.element, YAMLPsiElement.class, false);
        }

        if (!(effectiveElement instanceof YAMLMapping)) {
            return;
        }

        if (schema.requiredProperties().isEmpty() && !schema.additionalProperties()) {
            throw new RuntimeException("Object Schema without required properties: " + schema);
        }

        YAMLMapping object = (YAMLMapping) effectiveElement;
        List<String> currentProperties = object.getKeyValues().stream()
                .map(k -> clearText(k.getKeyText()))
                .collect(Collectors.toList());

        if (!schema.requiredProperties().isEmpty()) {
            Set<String> requiredProps = new HashSet<>(schema.requiredProperties());
            boolean removed = requiredProps.removeAll(currentProperties);
            if (removed) {
                this.schema = schema;
                this.match = ThreeState.YES;
                return;
            }
        }

        for (String currentProp : currentProperties) {
            if (schema.property(currentProp) != null) {
                this.schema = schema;
                this.match = ThreeState.UNSURE;
                return;
            }
        }
    }

    @Override
    protected void visitStringSchema(StringSchema schema) {
        super.visitStringSchema(schema);
    }
}
