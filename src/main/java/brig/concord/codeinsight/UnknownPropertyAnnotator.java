package brig.concord.codeinsight;

import brig.concord.ConcordBundle;
import brig.concord.codeinsight.action.DeletePropertyIntentionAction;
import brig.concord.model.Schema;
import brig.concord.model.SchemaProvider;
import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

public class UnknownPropertyAnnotator implements Annotator {

    private final SchemaProvider schemaProvider;

    public UnknownPropertyAnnotator() {
        this.schemaProvider = SchemaProvider.INSTANCE;
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        if (!ConcordYamlPsiUtils.isConcordFile(element)) {
            return;
        }

        YAMLKeyValue kv = ConcordYamlPsiUtils.asKv(element);
        if (kv == null) {
            return;
        }

        Schema schema = ConcordYamlPsiUtils.schema(schemaProvider, element);
        if (schema != null) {
            return;
        }

        YAMLMapping elementObject = ConcordYamlPsiUtils.getParentYamlOfType(element, YAMLMapping.class, false);
        schema = ConcordYamlPsiUtils.schema(schemaProvider, elementObject);
        if (schema == null) {
            return;
        }

        // TODO: add hint about properties and pattern properties

        annotationHolder.newAnnotation(HighlightSeverity.ERROR,
                ConcordBundle.message("annotator.unknown.property", kv.getKeyText()))
                .range(element)
                .withFix(new DeletePropertyIntentionAction())
                .create();
    }
}
