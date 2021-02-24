package brig.concord.model;

public abstract class AbstractVisitor extends Visitor {

    public void visit(Schema schema) {
        schema.accept(this);
    }

    @Override
    protected void visitBooleanSchema(BooleanSchema schema) {
    }

    @Override
    protected void visitStringSchema(StringSchema schema) {
    }

    @Override
    protected void visitIntSchema(IntSchema schema) {
    }

    @Override
    protected void visitConstSchema(ConstSchema schema) {
    }

    @Override
    protected void visitObjectSchema(ObjectSchema schema) {
    }

    @Override
    protected void visitArraySchema(ArraySchema schema) {
    }

    @Override
    protected void visitCombinedSchema(CombinedSchema schema) {
    }

    @Override
    protected void visitAnyValueSchema(Schema schema) {
    }
}
