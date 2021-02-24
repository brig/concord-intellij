package brig.concord.model;

public class DefaultFailVisitor extends Visitor {

    @Override
    protected void visitBooleanSchema(BooleanSchema schema) {
        throw new IllegalStateException("Unexpected boolean schema");
    }

    @Override
    protected void visitObjectSchema(ObjectSchema schema) {
        throw new IllegalStateException("Unexpected object schema");
    }

    @Override
    protected void visitStringSchema(StringSchema schema) {
        throw new IllegalStateException("Unexpected string schema");
    }

    @Override
    protected void visitArraySchema(ArraySchema schema) {
        throw new IllegalStateException("Unexpected array schema");
    }

    @Override
    protected void visitCombinedSchema(CombinedSchema schema) {
        throw new IllegalStateException("Unexpected combined schema");
    }

    @Override
    protected void visitConstSchema(ConstSchema schema) {
        throw new IllegalStateException("Unexpected const schema");
    }

    @Override
    public void visitAnyValueSchema(Schema schema) {
        throw new IllegalStateException("Unexpected any schema");
    }

    @Override
    public void visitIntSchema(IntSchema schema) {
        throw new IllegalStateException("Unexpected const schema");
    }
}
