package de.chrgroth.jsonstore.json.flexjson.custom;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

import flexjson.JSONException;
import flexjson.ObjectBinder;

/**
 * Custom transformer and object factory to handle instances {@link LocalDateTime} during JSON serialization and deserialization.
 *
 * @author Christian Groth
 */
public class DateTimeTypeHandler extends AbstractFlexjsonTypeHandler {

    private String dateTimePattern;
    private ThreadLocal<DateTimeFormatter> formatter = new ThreadLocal<DateTimeFormatter>();

    /**
     * Creates a new instance using the given pattern. Take a look at {@link DateTimeFormatter} for concrete syntax.
     *
     * @param dateTimePattern
     *            date time pattern
     */
    public DateTimeTypeHandler(String dateTimePattern) {
        this.dateTimePattern = dateTimePattern;
    }

    @Override
    public void transform(Object object) {
        if (object == null) {
            getContext().write("null");
        } else if (object instanceof TemporalAccessor) {
            getContext().writeQuoted(getFormatter().format((TemporalAccessor) object));
        }
    }

    @Override
    public Object instantiate(ObjectBinder context, Object value, Type targetType, @SuppressWarnings("rawtypes") Class targetClass) {
        try {
            return LocalDateTime.from(getFormatter().parse(value.toString()));
        } catch (DateTimeParseException e) {
            throw new JSONException(getClass().getSimpleName() + " failed to parse " + value + " at " + context.getCurrentPath() + " with pattern: " + dateTimePattern + "!!");
        }
    }

    private DateTimeFormatter getFormatter() {
        if (formatter.get() == null) {
            formatter.set(DateTimeFormatter.ofPattern(dateTimePattern));
        }
        return formatter.get();
    }
}
