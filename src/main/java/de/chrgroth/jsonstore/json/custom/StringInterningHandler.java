package de.chrgroth.jsonstore.json.custom;

import java.lang.reflect.Type;

import de.chrgroth.jsonstore.json.AbstractFlexjsonTypeHandler;
import flexjson.ObjectBinder;

/**
 * Handler to intern all deserialized {@link String} instances. This may save a lot of memory if strings are reused quire often.
 *
 * @author Christian Groth
 */
public class StringInterningHandler extends AbstractFlexjsonTypeHandler {

    @Override
    public void transform(Object object) {

        // nothing to do
    }

    @Override
    public Object instantiate(ObjectBinder context, Object value, Type targetType, @SuppressWarnings("rawtypes") Class targetClass) {

        // null guard
        if (value == null) {
            return value;
        }

        // intern the string
        return ((String) value).intern();
    }
}
