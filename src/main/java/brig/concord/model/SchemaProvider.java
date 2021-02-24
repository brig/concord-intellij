package brig.concord.model;

import brig.concord.ConcordBundle;
import brig.concord.completion.*;
import brig.concord.language.ConcordIcons;
import brig.concord.reference.ConcordFlowReference;

import java.util.Arrays;

import static brig.concord.model.ArraySchema.anyArray;
import static brig.concord.model.ObjectSchema.anyObject;
import static brig.concord.model.SchemaCompletionMapper.register;
import static brig.concord.model.SchemaRefProviderMapper.register;

public class SchemaProvider {

    public static final String FLOW_DEF_ID = "flowDef";
    public static final String TASK_DEF_ID = "taskStepDef";

    public static final SchemaProvider INSTANCE = new SchemaProvider();
    private final Schema root = buildSchema();

    public static SchemaProvider getInstance() {
        return INSTANCE;
    }

    private static Schema buildSchema() {
        StringSchema flowName = StringSchema.builder()
                .id("flowName")
                .description("Flow name")
                .build();
        register(flowName, new FlowNameCompletionProvider(ProcessDefinitionProvider.getInstance()));
        register(flowName, ConcordFlowReference::new);

        StringSchema profileName = StringSchema.builder()
                .id("profileName")
                .description("Profile name")
                .build();

        Schema eventsSchema = ObjectSchema.builder()
                .description("events schema descr")
                .property("recordTaskInVars", BooleanSchema.builder().description("recordTaskInVars descr").build())
                .property("truncateInVars", BooleanSchema.builder().description("truncateInVars descr").build())
                .property("recordTaskOutVars", BooleanSchema.builder().description("recordTaskOutVars descr").build())
                .property("truncateOutVars", BooleanSchema.builder().description("truncateOutVars descr").build())
                .property("truncateMaxStringLength", IntSchema.builder().description("truncateMaxStringLength descr").build())
                .property("truncateMaxArrayLength", IntSchema.builder().description("truncateMaxArrayLength descr").build())
                .property("truncateMaxDepth", IntSchema.builder().description("truncateMaxDepth descr").build())
                .property("inVarsBlacklist", ArraySchema.withItem(StringSchema.builder().description("inVarsBlacklist item descr").build()).description("inVarsBlacklist descr").build())
                .property("outVarsBlacklist", ArraySchema.withItem(StringSchema.builder().description("outVarsBlacklist item descr").build()).description("outVarsBlacklist descr").build())
                .build();

        Schema exclusiveSchema = ObjectSchema.builder()
                .description("exclusive schema descr")
                .property("group")
                .schema(StringSchema.builder()
                        .description("group descr")
                        .build())
                .required()
                .build()
                .property("mode")
                .schema(CombinedSchema.oneOf(
                        ConstSchema.builder().value("cancel").description("cancel value descr").build(),
                        ConstSchema.builder().value("wait").description("wait value descr").build())
                        .description("combined mode descr")
                        .build())
                .build()
                .build();

        Schema cfg = ObjectSchema.builder()
                .description(ConcordBundle.message("model.configuration.description"))
                .property("debug")
                .schema(BooleanSchema.builder()
                        .description(ConcordBundle.message("model.configuration.debug.description"))
                        .defaultValue(true)
                        .build())
                .build()
                .property("runtime")
                .schema(ConstSchema.builder()
                        .value("concord-v2")
                        .description(ConcordBundle.message("model.configuration.runtime.version.description"))
                        .build())
                .required()
                .build()

//                .property("test-object")
//                .schema(ObjectSchema.builder()
//                        .property("test", StringSchema.builder().build(), true)
//                        .property("test-object1")
//                        .schema(ArraySchema.withItem(StringSchema.builder().build()).build())
//                        .required()
//                        .build()
//                        .build())
//                .required()
//                .build()

                .property("entryPoint", flowName)
                .property("activeProfiles")
                .schema(ArraySchema.withItem(profileName)
                        .description("activeProfiles array descr")
                        .build())
                .build()
                .property("dependencies")
                .schema(ArraySchema.withItem(StringSchema.builder().build())
                        .description("Process dependencies")
                        .build())
                .build()
                .anyObjectProperty("arguments", "Process default variables")
                .anyObjectProperty("requirements", "Process requirements")
                .anyObjectProperty("meta", "Process metadata")
                .property("processTimeout", StringSchema.builder().description("processTimeout value descr").build()) // TODO: check value
                .property("exclusive", exclusiveSchema)
                .property("events", eventsSchema)
                .property("out", ArraySchema.withItem(StringSchema.builder().description("out value descr").build()).description("out arr descr").build())
                .build();

        Schema parallelWithItems = CombinedSchema.oneOf(
                StringSchema.builder().build(),
                anyObject("asd"), anyArray("arr"))
                .description("parallel withitems description")
                .build();

        StringSchema expression = StringSchema.builder()
                .id("expression")
                .description("Expression")
                .pattern("^\\$\\{.*}$")
                .customType(ValueTypes.EXPRESSION)
                .build();
        register(expression, ExpressionCompletionProvider.INSTANCE);

        Schema retrySchema = ObjectSchema.builder()
                .description("Retry options")
                .property("times", CombinedSchema.oneOf(
                        IntSchema.builder()
                                .description("count")
                                .build(),
                        expression)
                        .description("the number of times a task/flow can be retried")
                        .build())
                .property("delay", CombinedSchema.oneOf(
                        IntSchema.builder()
                                .description("seconds")
                                .build(),
                        expression)
                        .description("the time span after which it retries")
                        .build())
                .anyObjectProperty("in", "additional parameters for the retry")
                .build();

        Schema.Ref steps = new Schema.Ref();

        Schema checkpointStep = ObjectSchema.builder()
                .description("Checkpoint step description")
                .property("checkpoint")
                .schema(StringSchema.builder().description("checkpoint name").build())
                .required()
                .build()
                .build();

        Schema logStep = ObjectSchema.builder()
                .description("Log task step description")
                .property("name")
                .schema(StringSchema.builder().description("Task name").build())
                .build()
                .property("log")
                .schema(StringSchema.builder().description("log value").build())
                .required()
                .build()
                .build();

        ObjectSchema taskStep = ObjectSchema.builder()
                .id(TASK_DEF_ID)
                .description("Task Step description")
                .property("name")
                .schema(StringSchema.builder().description("Step ask name").build())
                .build()
                .property("task")
                .schema(StringSchema.builder().description("Task identifier").build())
                .required()
                .build()
                .anyObjectProperty("in", "Task input parameters")
                .property("out")
                .schema(CombinedSchema.oneOf(
                        StringSchema.builder().description("out string").build(),
                        anyObject("out any")).description("task out variables").build())
                .build()
                .property("parallelWithItems", parallelWithItems)
                .property("withItems", parallelWithItems)
                .property("retry", retrySchema)
                .property("error", steps)
                .anyObjectProperty("meta", "Task meta")
                .build();

        Schema expressionStep = ObjectSchema.builder()
                .property("expr", expression, true)
                .property("name")
                .schema(StringSchema.builder().description("Step ask name").build())
                .build()
                .property("out")
                .schema(CombinedSchema.oneOf(
                        StringSchema.builder().description("out string").build(),
                        anyObject("out any")).description("expr out variables").build())
                .build()
                .property("error", steps)
                .build();

        Schema shortExpressionStep = StringSchema.builder().from(expression)
                .description("Short expression")
                .build();

        Schema returnStep = ConstSchema.builder()
                .description("The return command can be used to stop the execution of the current (sub) flow.")
                .value("return")
                .build();

        Schema exitStep = ConstSchema.builder()
                .description("Exit step description")
                .value("exit")
                .build();

        Schema throwStep = ObjectSchema.builder()
                .description("Throw step description")
                .anyProperty("throw", "throw object", true)
                .build();

        ObjectSchema ifStep = ObjectSchema.builder()
                .property("if", shortExpressionStep, true)
                .property("then", steps, true)
                .property("else", steps)
                .build();

        ObjectSchema switchStep = ObjectSchema.builder()
                .property("switch", shortExpressionStep, true)
                .property("default", steps)
                .additionalProperties(true)
                .schemaOfAdditionalProperties(steps)
                .build();

        ObjectSchema tryStep = ObjectSchema.builder()
                .property("try", steps, true)
                .property("error", steps)
                .property("name")
                .schema(StringSchema.builder().description("Step name").build())
                .build()
                .build();

        ObjectSchema blockStep = ObjectSchema.builder()
                .property("block", steps, true)
                .property("error", steps)
                .build();

        Schema callStep = ObjectSchema.builder()
                .description("Flow Call Step description")
                .property("call", flowName, true)
                .property("name")
                .schema(StringSchema.builder().description("Step name").build())
                .build()
                .anyObjectProperty("in", "Flow call input parameters")
                .property("out")
                .schema(CombinedSchema.oneOf(
                        StringSchema.builder().description("out string").build(),
                        anyObject("out any"),
                        ArraySchema.withItem(StringSchema.builder().description("out variable").build()).description("out variables").build())
                        .description("call out variables").build())
                .build()
                .property("parallelWithItems", parallelWithItems)
                .property("withItems", parallelWithItems)
                .property("retry", retrySchema)
                .property("error", steps)
                .anyObjectProperty("meta", "Call meta")
                .build();

        Schema scriptStep = ObjectSchema.builder()
                .description("Script Step description")
                .property("script", StringSchema.builder().description("script engine or script name").build(), true)
                .property("body", StringSchema.builder().description("script body").build())
                .anyObjectProperty("in", "Script call input parameters")
                .property("parallelWithItems", parallelWithItems)
                .property("withItems", parallelWithItems)
                .property("retry", retrySchema)
                .property("error", steps)
                .build();

        Schema setStep = ObjectSchema.builder()
                .description("Set Variables step")
                .anyObjectProperty("set", "Flow variables", true)
                .build();

        steps.set(ArraySchema.builder()
                .description("Flow steps")
                .itemSchema(CombinedSchema.oneOf(
                        Arrays.asList(taskStep, callStep, logStep, ifStep, switchStep, returnStep, exitStep, throwStep, expressionStep, shortExpressionStep, setStep, tryStep, checkpointStep, blockStep, scriptStep))
                        .id("flowSteps")
                        .description("flow step")
                        .build())
                .build());
        register("flowSteps", new CombinedSchemaCompletionProvider()
                .withCustomCompletion(ifStep, CombinedSchemaCompletionProvider.OptionsCompletionProvider.from(CompletionOption.from("if", null)))
                .withCustomCompletion(taskStep, CombinedSchemaCompletionProvider.OptionsCompletionProvider.from(CompletionOption.from("task", ConcordIcons.TASK))));

        ObjectSchema flows = ObjectSchema.builder()
                .id(FLOW_DEF_ID)
                .description("Concord flows")
                .patternProperty("^[a-zA-Z0-9_]*$", steps)
                .build();
        register(flows, FlowNameDefinitionCompletionProvider.INSTANCE);

        Schema publicFlows = ArraySchema.builder()
                .description("Flows listed in the publicFlows section are the only flows allowed as entry point values. This also limits the flows listed in the repository run dialog. When the publicFlows is omitted, Concord considers all flows as public.")
                .itemSchema(flowName)
                .build();

        Schema secretSchema = ObjectSchema.builder()
                .property("name", StringSchema.builder().description("secret name descr").build(), true)
                .property("password", StringSchema.builder().description("secret password descr").build())
                .property("org", StringSchema.builder().description("secret org descr").build())
                .build();

        Schema gitImportOptions = ObjectSchema.builder()
                .description("git import description")
                .property("name", StringSchema.builder().description("name descr").build())
                .property("url", StringSchema.builder().description("url descr").build())
                .property("version", StringSchema.builder().description("version descr").build())
                .property("path", StringSchema.builder().description("path descr").build())
                .property("dest", StringSchema.builder().description("dest descr").build())
                .property("exclude", ArraySchema.withItem(StringSchema.builder().description("exclude descr").build()).description("exclude array schema").build())
                .property("secret", secretSchema)
                .build();

        Schema gitImport = ObjectSchema.builder()
                .property("git", gitImportOptions, true)
                .build();

        Schema mvnImportOptions = ObjectSchema.builder()
                .description("mvn import description")
                .property("url", StringSchema.builder().description("url descr").build())
                .property("dest", StringSchema.builder().description("dest descr").build())
                .build();

        Schema mvnImport = ObjectSchema.builder()
                .property("mvn", mvnImportOptions, true)
                .build();

        Schema dirImportOptions = ObjectSchema.builder()
                .description("dir import description")
                .property("src", StringSchema.builder().description("src descr").build(), true)
                .property("dest", StringSchema.builder().description("dest descr").build())
                .build();

        Schema dirImport = ObjectSchema.builder()
                .property("dir", dirImportOptions, true)
                .build();

        Schema imports = ArraySchema.withItem(CombinedSchema.oneOf(gitImport, mvnImport, dirImport).description("ONE OF import").build())
                .description("imports arr descr")
                .build();

        return ObjectSchema.builder()
                .property("configuration", cfg)
                .property("publicFlows", publicFlows)
                .property("flows", flows)
                .property("imports", imports)
//                .property("triggers", triggers)
//                .property("profiles", profiles)
//                .property("forms", forms)
//                .property("resources", resources)
                .build();
    }

    public Schema get() {
        return root;
    }
}
