package brig.concord.codeinsight;

import brig.concord.model.Schema;
import brig.concord.model.SchemaProvider;
import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.YAMLValue;

public class ValueTypeCheckerAnnotator implements Annotator {

    private final SchemaProvider schemaProvider;

    public ValueTypeCheckerAnnotator() {
        this.schemaProvider = SchemaProvider.INSTANCE;
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        if (!ConcordYamlPsiUtils.isConcordFile(element)) {
            return;
        }

        if (!ConcordYamlPsiUtils.isValue(element)) {
            return;
        }

        Schema valueSchema = ConcordYamlPsiUtils.schema(schemaProvider, element);
        if (valueSchema != null) {
            if (ConcordYamlPsiUtils.isNullValue(element)) {
                TypeCheckVisitor visitor = new TypeCheckVisitor(null, annotationHolder);
                visitor.visit(valueSchema);
            }
            return;
        }

        YAMLPsiElement parent = ConcordYamlPsiUtils.getParentOfType(element, YAMLPsiElement.class, false);
        valueSchema = ConcordYamlPsiUtils.schema(schemaProvider, parent);

        if (valueSchema == null) {
            return;
        }

        YAMLValue value = ConcordYamlPsiUtils.asValue(element);
        TypeCheckVisitor visitor = new TypeCheckVisitor(value, annotationHolder);
        visitor.visit(valueSchema);
    }
}
