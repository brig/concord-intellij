package brig.concord.codeinsight;

import brig.concord.ConcordBundle;
import brig.concord.codeinsight.action.CreateMissingPropertiesIntentionAction;
import brig.concord.model.ObjectSchema;
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

import java.util.HashSet;
import java.util.Set;

public class MissingRequiredPropertiesAnnotator implements Annotator {

    private final SchemaProvider schemaProvider;

    public MissingRequiredPropertiesAnnotator() {
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
        if (!(schema instanceof ObjectSchema)) {
            return;
        }

        YAMLMapping value = ConcordYamlPsiUtils.asObject(((YAMLKeyValue) element).getValue());
        Set<String> current = ConcordYamlPsiUtils.properties(value);
        Set<String> requiredProps = new HashSet<>(((ObjectSchema) schema).requiredProperties());
        requiredProps.removeAll(current);
        if (!requiredProps.isEmpty()) {
            annotationHolder
                    .newAnnotation(HighlightSeverity.ERROR, ConcordBundle.message("annotator.missing.required.props", kv.getKeyText(), String.join(", ", requiredProps)))
                    .range(element)
                    .withFix(new CreateMissingPropertiesIntentionAction(requiredProps, element))
                    .create();
        }
    }
}



