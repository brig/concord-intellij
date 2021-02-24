package brig.concord.search;

import brig.concord.model.ProcessDefinition;
import brig.concord.model.ProcessDefinitionProvider;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;

import java.util.ArrayList;
import java.util.List;

public class FlowDefinitionSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {

    @Override
    public void processQuery(ReferencesSearch.@NotNull SearchParameters queryParameters, @NotNull Processor<? super PsiReference> consumer) {
        PsiElement element = queryParameters.getElementToSearch();

        if (!(element instanceof YAMLKeyValue)) {
            return;
        }

        YAMLKeyValue flowDefinitionElement = (YAMLKeyValue) element;

        ProcessDefinition pd = ReadAction.compute(() -> ProcessDefinitionProvider.getInstance().get(flowDefinitionElement));
        if (pd == null) {
            return;
        }

        String flowName = ReadAction.compute(flowDefinitionElement::getKeyText);

        List<PsiElement> result = new ArrayList<>();

        // TODO: better check kv types
        //  ignore this:
        //  configuration:
        //    arguments:
        //      call: "boo"
        ReadAction.run(() -> pd.allDefinitions().forEach(d -> d.accept(new YamlRecursivePsiElementVisitor() {
            @Override
            public void visitKeyValue(@NotNull YAMLKeyValue keyValue) {
                String k = ReadAction.compute(keyValue::getKeyText);
                String v = ReadAction.compute(keyValue::getValueText);
                if ("call".equals(k) && flowName.equals(v)) {
                    result.add(ReadAction.compute(keyValue::getValue));
                }
                super.visitKeyValue(keyValue);
            }
        })));

        DumbService.getInstance(element.getProject()).runReadActionInSmartMode(() -> {
            for (PsiElement flowElement : result) {
                PsiReference ref = new PsiReferenceBase<>(flowElement) {

                    @Override
                    public PsiElement resolve() {
                        return flowElement;
                    }
                };
                consumer.process(ref);
            }
        });
    }
}
