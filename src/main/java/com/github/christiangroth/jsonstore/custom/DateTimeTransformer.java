package com.github.christiangroth.jsonstore.custom;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

import flexjson.JSONException;
import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import flexjson.transformer.AbstractTransformer;

// TODO unit test
// TODO comment
public class DateTimeTransformer extends AbstractTransformer implements ObjectFactory {
	
	private String dateTimePattern;
	private ThreadLocal<DateTimeFormatter> formatter = new ThreadLocal<DateTimeFormatter>();
	
	public DateTimeTransformer(String dateTimePattern) {
		this.dateTimePattern = dateTimePattern;
	}
	
	@Override
	public void transform(Object object) {
		if (object == null) {
			getContext().write("null");
		} else if (object instanceof TemporalAccessor) {
			getContext().writeQuoted(getFormatter().format((TemporalAccessor) object));
		} else {
			throw new JSONException(getClass().getSimpleName() + " is unable to transform non temporal accessor: " + object.getClass().getName() + "!!");
		}
	}
	
	@Override
	public Object instantiate(ObjectBinder context, Object value, Type targetType, @SuppressWarnings("rawtypes") Class targetClass) {
		try {
			return getFormatter().parse(value.toString());
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
