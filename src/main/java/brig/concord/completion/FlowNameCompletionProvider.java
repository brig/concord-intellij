package brig.concord.completion;

import brig.concord.model.ProcessDefinition;
import brig.concord.model.ProcessDefinitionProvider;
import brig.concord.model.StringSchema;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.AstLoadingFilter;

public class FlowNameCompletionProvider implements SchemaCompletionProvider<StringSchema> {

    private final ProcessDefinitionProvider processDefinitionProvider;

    public FlowNameCompletionProvider(ProcessDefinitionProvider processDefinitionProvider) {
        this.processDefinitionProvider = processDefinitionProvider;
    }

    @Override
    public void addCompletions(CompletionContext ctx, StringSchema schema, CompletionResultSet result) {
        ProcessDefinition process = processDefinitionProvider.get(ctx.parameters().getOriginalFile());
        if (process == null) {
            return;
        }

        for (String flowName : process.flowNames()) {
            result.addElement(ctx.element(flowName));
        }
    }
}
