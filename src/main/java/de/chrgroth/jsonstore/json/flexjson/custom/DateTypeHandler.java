package de.chrgroth.jsonstore.json.flexjson.custom;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import flexjson.ObjectBinder;

/**
 * Custom type handler processing {@link Date} instances with configured pattern.
 *
 * @author Christian Groth
 */
public class DateTypeHandler extends AbstractFlexjsonTypeHandler {

    private flexjson.transformer.DateTransformer delegate;

    /**
     * Creates a new instance using the given pattern. Take a look at {@link DateTimeFormatter} for concrete syntax.
     *
     * @param dateTimePattern
     *            date time pattern
     */
    public DateTypeHandler(String dateTimePattern) {
        delegate = new flexjson.transformer.DateTransformer(dateTimePattern);
    }

    @Override
    public void transform(Object object) {
        delegate.transform(object);
    }

    @Override
    public Object instantiate(ObjectBinder context, Object value, Type targetType, @SuppressWarnings("rawtypes") Class targetClass) {
        return delegate.instantiate(context, value, targetType, targetClass);
    }
}
