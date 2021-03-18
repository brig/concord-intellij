package brig.concord.model;

import java.util.HashSet;
import java.util.Set;

public class ValueTypeVisitor extends Visitor {

    private final Set<String> types = new HashSet<>();

    public void reset() {
        this.types.clear();
    }

    public Set<String> types() {
        return types;
    }

    @Override
    protected void visitBooleanSchema(BooleanSchema schema) {
        this.types.add(ValueTypes.BOOLEAN);
    }

    @Override
    protected void visitObjectSchema(ObjectSchema schema) {
        this.types.add(ValueTypes.OBJECT);
    }

    @Override
    protected void visitStringSchema(StringSchema schema) {
        this.types.add(schema.customType() != null ? schema.customType() : ValueTypes.STRING);
    }

    @Override
    protected void visitArraySchema(ArraySchema schema) {
        this.types.add(ValueTypes.ARRAY);
    }

    @Override
    public void visitIntSchema(IntSchema schema) {
        this.types.add(ValueTypes.INT);
    }

    @Override
    protected void visitCombinedSchema(CombinedSchema schema) {
        ValueTypeVisitor visitor = new ValueTypeVisitor();
        for (Schema s : schema.subSchemas()) {
            visitor.reset();
            visitor.visit(s);
            this.types.addAll(visitor.types());
        }
    }

    @Override
    protected void visitConstSchema(ConstSchema schema) {
        switch (schema.valueType()) {
            case STRING:
                this.types.add(ValueTypes.STRING);
                break;
            case INT:
                this.types.add(ValueTypes.INT);
                break;
            default:
                throw new RuntimeException("Unknown type: " + schema.valueType());
        }
    }

    @Override
    public void visitAnyValueSchema(Schema schema) {
    }
}
