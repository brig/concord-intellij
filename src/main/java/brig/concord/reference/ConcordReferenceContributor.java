package brig.concord.reference;

import brig.concord.model.Schema;
import brig.concord.model.SchemaProvider;
import brig.concord.model.SchemaUtils;
import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.YAMLScalar;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class ConcordReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(psiElement(YAMLScalar.class).with(new ConcordFilePatternCondition())
                .with(new HasConcordReference()), new YamlScalarPsiReferenceProvider());
    }

    static class ConcordFilePatternCondition extends PatternCondition<YAMLPsiElement> {

        ConcordFilePatternCondition() {
            super("ConcordFileCheck");
        }

        @Override
        public boolean accepts(@NotNull YAMLPsiElement yamlScalar, ProcessingContext context) {
            return ConcordYamlPsiUtils.isConcordFile(yamlScalar);
        }
    }

    static class HasConcordReference extends PatternCondition<YAMLPsiElement> {

        HasConcordReference() {
            super("ConcordReferenceCheck");
        }

        @Override
        public boolean accepts(@NotNull YAMLPsiElement element, ProcessingContext context) {
            Schema schema = ConcordYamlPsiUtils.schema(SchemaProvider.INSTANCE, element);
            return schema != null && !SchemaUtils.scalarReferencesProvider(schema).isEmpty();
        }
    }
}