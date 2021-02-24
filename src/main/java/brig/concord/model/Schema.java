package brig.concord.model;

import javax.annotation.Nullable;

public interface Schema {

    Schema ANY = any(null);

    static Schema any(String description) {
        return new Schema() {

            @Nullable
            @Override
            public String id() {
                return null;
            }

            @Nullable
            @Override
            public String description() {
                return description;
            }

            @Override
            public void accept(Visitor visitor) {
                visitor.visitAnyValueSchema(this);
            }
        };
    }

    @Nullable
    String id();

    @Nullable
    String description();

    void accept(Visitor visitor);

    class Ref implements Schema {

        private Schema delegate;

        public void set(Schema delegate) {
            this.delegate = delegate;
        }

        @Nullable
        @Override
        public String id() {
            return delegate.id();
        }

        @Nullable
        @Override
        public String description() {
            return delegate.description();
        }

        @Override
        public void accept(Visitor visitor) {
            delegate.accept(visitor);
        }
    }
}
