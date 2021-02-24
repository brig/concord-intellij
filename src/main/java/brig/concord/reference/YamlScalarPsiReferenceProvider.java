package brig.concord.reference;

import brig.concord.model.Schema;
import brig.concord.model.SchemaProvider;
import brig.concord.model.SchemaUtils;
import brig.concord.psi.ConcordYamlPsiUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.List;

public class YamlScalarPsiReferenceProvider extends PsiReferenceProvider {

    @Override
    public @NotNull PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        Schema schema = ConcordYamlPsiUtils.schema(SchemaProvider.INSTANCE, element);
        if (schema == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        List<YamlScalarReferenceProvider> providers = SchemaUtils.scalarReferencesProvider(schema);
        if (providers.isEmpty()) {
            return PsiReference.EMPTY_ARRAY;
        }

        return providers.stream()
                .map(p -> p.get((YAMLScalar) element))
                .toArray(PsiReference[]::new);
    }
}
