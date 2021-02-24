package brig.concord.documentation;

import brig.concord.completion.CompletionItem;
import brig.concord.model.Schema;
import brig.concord.model.SchemaProvider;
import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.lang.Language;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.YAMLScalar;

import static com.intellij.lang.documentation.DocumentationMarkup.*;

public class ConcordDocumentationProvider extends AbstractDocumentationProvider {

    private final SchemaProvider schemaProvider;

    public ConcordDocumentationProvider() {
        this.schemaProvider = SchemaProvider.INSTANCE;
    }

    private static String docTitle(PsiElement element) {
        YAMLKeyValue kv = ConcordYamlPsiUtils.asKv(element);
        if (kv != null) {
            return kv.getKeyText();
        }

        YAMLScalar scalar = ConcordYamlPsiUtils.asScalar(element);
        if (scalar != null) {
            return scalar.getTextValue();
        }

        return null;
    }

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (!(element instanceof DocElement) && !ConcordYamlPsiUtils.isConcordFile(element)) {
            return null;
        }

        CompletionItem targetProperty = null;
        if (element instanceof DocElement) {
            targetProperty = ((DocElement) element).targetProperty();
        } else {
            Schema schema = ConcordYamlPsiUtils.schema(schemaProvider, element);
            if (schema != null) {
                String title = docTitle(element);
                if (title != null) {
                    targetProperty = CompletionItem.of(docTitle(element), schema);
                }
            }
        }

        if (targetProperty != null) {
            String name = targetProperty.value();
            String description = targetProperty.schema().description();
            if (description != null) {
                return DEFINITION_START + name + DEFINITION_END +
                        CONTENT_START + description + CONTENT_END;
            }
        }
        return null;
    }

    @Override
    public @Nullable PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        if (element != null && object instanceof CompletionItem) {
            return new DocElement(psiManager, element.getLanguage(), (CompletionItem) object);
        }
        return null;
    }

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(@NotNull Editor editor, @NotNull PsiFile file, @Nullable PsiElement contextElement, int targetOffset) {
        if (contextElement == null) {
            return null;
        }

        if (contextElement instanceof YAMLPsiElement) {
            return contextElement;
        } else if (contextElement.getParent() instanceof YAMLScalar) {
            return contextElement.getParent();
        }

        return null;
    }

    private static class DocElement extends LightElement {

        private final CompletionItem targetProperty;

        protected DocElement(@NotNull final PsiManager manager, @NotNull final Language language, @NotNull final CompletionItem targetProperty) {
            super(manager, language);
            this.targetProperty = targetProperty;
        }

        public CompletionItem targetProperty() {
            return targetProperty;
        }

        @Override
        public String toString() {
            return "DocElement for " + targetProperty;
        }
    }
}
