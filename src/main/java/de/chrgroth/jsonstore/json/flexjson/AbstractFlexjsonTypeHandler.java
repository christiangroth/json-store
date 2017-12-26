package de.chrgroth.jsonstore.json.flexjson;

import java.lang.reflect.Type;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import flexjson.transformer.AbstractTransformer;

/**
 * Abstract base class for all custom type handler controlling serialization and deserialization of ustom types.
 *
 * @author Christian Groth
 */
public abstract class AbstractFlexjsonTypeHandler extends AbstractTransformer implements ObjectFactory {

    @Override
    public abstract void transform(Object object);

    @Override
    public abstract Object instantiate(ObjectBinder context, Object value, Type targetType, @SuppressWarnings("rawtypes") Class targetClass);
}
