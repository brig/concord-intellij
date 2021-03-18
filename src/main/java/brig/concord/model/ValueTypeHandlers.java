package brig.concord.model;

import java.util.Map;

public class ValueTypeHandlers<V> {

    private final Map<String, ValueTypeHandler<V>> handlers;
    private final ValueTypeHandler<Object> defaultHandler = new SuccessValueTypeHandler();

    public ValueTypeHandlers(Map<String, ValueTypeHandler<V>> handlers) {
        this.handlers = handlers;
    }

    @SuppressWarnings("unchecked")
    public ValueTypeHandler<V> get(String valueType) {
        ValueTypeHandler<V> handler = handlers.get(valueType);
        if (handler != null) {
            return handler;
        }
        return (ValueTypeHandler<V>) defaultHandler;
    }

    private static class SuccessValueTypeHandler implements ValueTypeHandler<Object> {

        @Override
        public Result canHandle(Object value) {
            return Result.success();
        }
    }
}
