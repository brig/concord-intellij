package brig.concord.model;

import brig.concord.reference.YamlScalarReferenceProvider;

import java.util.*;

public class SchemaRefProviderMapper {

    public static final SchemaRefProviderMapper INSTANCE = new SchemaRefProviderMapper();
    private final Map<String, List<YamlScalarReferenceProvider>> scalarRefProviders = new HashMap<>();

    public static void register(Schema schema, YamlScalarReferenceProvider provider) {
        Objects.requireNonNull(schema.id(), "schema.id must not be null");

        register(schema.id(), provider);
    }

    public static void register(String schemaId, YamlScalarReferenceProvider provider) {
        INSTANCE.add(schemaId, provider);
    }

    public void add(String schemaId, YamlScalarReferenceProvider provider) {
        scalarRefProviders.computeIfAbsent(schemaId, k -> new ArrayList<>()).add(provider);
    }

    public List<YamlScalarReferenceProvider> get(String schemaId) {
        return scalarRefProviders.getOrDefault(schemaId, Collections.emptyList());
    }
}
