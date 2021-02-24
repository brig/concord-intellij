package brig.concord.completion;

import brig.concord.log.Logger;
import brig.concord.model.Schema;
import brig.concord.model.SchemaMatcherVisitor;
import brig.concord.model.SchemaProvider;
import brig.concord.model.SchemaUtils;
import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.Optional;

public class ConcordYamlCompletionContributor extends CompletionContributor {

    public ConcordYamlCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(YAMLLanguage.INSTANCE),
                new Provider());
    }

    private static class Provider extends CompletionProvider<CompletionParameters> {

        private static PsiElement getYamlElement(@Nullable PsiElement element) {
            if (element == null) {
                return null;
            }

            PsiElement current = element;
            while (current != null && !(current instanceof PsiFile)) {
                if (current instanceof YAMLValue || current instanceof YAMLKeyValue) {
                    return current;
                }
                current = current.getParent();
            }
            return null;
        }

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {

            PsiElement element = getYamlElement(parameters.getPosition());

            if (!ConcordYamlPsiUtils.isConcordFile(element) || element instanceof PsiComment) {
                return;
            }

            // skip current
            element = skip(element);
            if (element == null) {
                return;
            }

            Schema schema = ConcordYamlPsiUtils.schema(SchemaProvider.INSTANCE,
                    p -> new SchemaMatcherVisitor(p, new CombinedSchemaCompletionMatcherVisitor(p)),
                    element);
            {
                Logger.out("completions for schema2: '{}'", schema != null ? Optional.ofNullable(schema.description()).orElse("n/a") : null);
            }
            if (schema == null) {
                return;
            }

            CompletionContext ctx = CompletionContext.builder()
                    .parameters(parameters)
                    .build();

            SchemaUtils.completionProvider(schema)
                    .addCompletions(ctx, schema, result);
        }

        private PsiElement skip(PsiElement element) {
            if (element instanceof YAMLScalar) {
                String text = element.getText();
                if (text != null) {
                    int newLinePos = text.indexOf('\n');
                    if (newLinePos > 0) {
                        int caretPos = text.indexOf(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED);
                        if (caretPos > newLinePos) {
                            return null;
                        }
                    }
                }
            }

            element = ConcordYamlPsiUtils.getParentOfType(element, YAMLPsiElement.class, false);
            if (element == null) {
                return null;
            }

            if (element.getText().startsWith(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)) {
                return ConcordYamlPsiUtils.getParentOfType(element, YAMLPsiElement.class, false);
            }

            return element;
        }
    }
}
