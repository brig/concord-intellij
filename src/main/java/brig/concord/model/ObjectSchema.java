package brig.concord.model;

import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Value.Immutable
public interface ObjectSchema extends Schema {

    static Builder builder() {
        return new Builder();
    }

    static Schema anyObject(String description) {
        return ObjectSchema.builder()
                .description(description)
                .additionalProperties(true)
                .schemaOfAdditionalProperties(ANY)
                .build();
    }

    @Value.Default
    default Map<String, Schema> properties() {
        return Collections.emptyMap();
    }

    default Schema property(String name) {
        Schema result = properties().get(name);
        if (result != null) {
            return result;
        }

        for (Map.Entry<Pattern, Schema> e : patternProperties().entrySet()) {
            if (e.getKey().matcher(name).matches()) {
                return e.getValue();
            }
        }

        if (additionalProperties()) {
            return schemaOfAdditionalProperties();
        }

        return null;
    }

    @Value.Default
    default Set<String> requiredProperties() {
        return Collections.emptySet();
    }

    @Value.Default
    default boolean additionalProperties() {
        return false;
    }

    @Nullable
    Schema schemaOfAdditionalProperties();

    @Value.Default
    default Map<Pattern, Schema> patternProperties() {
        return Collections.emptyMap();
    }

    @Override
    default void accept(Visitor visitor) {
        visitor.visitObjectSchema(this);
    }

    class PropertyBuilder {

        private final Builder builder;
        private final String propName;
        private Schema schema;
        private boolean required;

        public PropertyBuilder(Builder builder, String name) {
            this.builder = builder;
            this.propName = name;
        }

        public PropertyBuilder schema(Schema schema) {
            this.schema = schema;
            return this;
        }

        public PropertyBuilder required() {
            this.required = true;
            return this;
        }

        public Builder build() {
            builder.putProperties(propName, schema);
            if (required) {
                builder.addRequiredProperties(propName);
            }
            return builder;
        }
    }

    class Builder extends ImmutableObjectSchema.Builder {

        public PropertyBuilder property(String name) {
            return new PropertyBuilder(this, name);
        }

        public Builder property(String name, Schema schema) {
            return putProperties(name, schema);
        }

        public Builder property(String name, Schema schema, boolean required) {
            if (required) {
                addRequiredProperties(name);
            }
            return putProperties(name, schema);
        }

        public Builder anyObjectProperty(String name, String description) {
            return anyObjectProperty(name, description, false);
        }

        public Builder anyObjectProperty(String name, String description, boolean required) {
            if (required) {
                addRequiredProperties(name);
            }
            return putProperties(name, anyObject(description));
        }

        public Builder anyProperty(String name, String description, boolean required) {
            if (required) {
                addRequiredProperties(name);
            }
            return putProperties(name, Schema.any(description));
        }

        public Builder patternProperty(String name, Schema schema) {
            return putPatternProperties(Pattern.compile(name), schema);
        }
    }
}
