package brig.concord.model;

import org.immutables.value.Value;

import javax.annotation.Nullable;

public interface ValueTypeHandler<V> {

    @Value.Immutable
    interface Result {

        @Value.Parameter
        boolean ok();

        @Nullable
        @Value.Parameter
        String error();

        static Result of(boolean ok) {
            return ImmutableResult.of(ok, null);
        }

        static Result success() {
            return ImmutableResult.of(true, null);
        }

        static Result fail() {
            return ImmutableResult.of(false, null);
        }

        static Result fail(String error) {
            return ImmutableResult.of(false, error);
        }
    }

    Result canHandle(V value);
}
