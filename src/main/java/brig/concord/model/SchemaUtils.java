package brig.concord.model;

import brig.concord.completion.SchemaCompletionProvider;
import brig.concord.reference.YamlScalarReferenceProvider;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class SchemaUtils {

    private SchemaUtils() {
    }

    public static Set<String> valueType(Schema schema) {
        ValueTypeVisitor visitor = new ValueTypeVisitor();
        visitor.visit(schema);
        return visitor.types();
    }

    public static <T extends Schema> SchemaCompletionProvider<T> completionProvider(T schema) {
        return SchemaCompletionMapper.INSTANCE.get(schema);
    }

    public static List<YamlScalarReferenceProvider> scalarReferencesProvider(Schema schema) {
        if (schema.id() == null) {
            return Collections.emptyList();
        }
        return SchemaRefProviderMapper.INSTANCE.get(schema.id());
    }
}
