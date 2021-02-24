package brig.concord.model;

public abstract class Visitor {

    public void visit(Schema schema) {
        schema.accept(this);
    }

    protected abstract void visitBooleanSchema(BooleanSchema schema);

    protected abstract void visitIntSchema(IntSchema schema);

    protected abstract void visitStringSchema(StringSchema schema);

    protected abstract void visitConstSchema(ConstSchema schema);

    protected abstract void visitObjectSchema(ObjectSchema schema);

    protected abstract void visitArraySchema(ArraySchema schema);

    protected abstract void visitCombinedSchema(CombinedSchema schema);

    protected abstract void visitAnyValueSchema(Schema schema);
}
