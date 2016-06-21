package de.chrgroth.jsonstore.store;

import java.util.Date;

import flexjson.JSON;

/**
 * Represents metadata information about any JSON store instance.
 *
 * @author Christian Groth
 * @param <T>
 *            type of objects being stored
 * @param
 *            <P>
 *            type of object being serialized and deserialized in order to store and load objects of type T
 */
public class JsonStoreMetadata<T, P> {

    @JSON
    private P payload;
    private String payloadType;
    private Integer payloadTypeVersion;
    private boolean singleton;
    private Date created;
    private Date modified;

    public P getPayload() {
        return payload;
    }

    public void setPayload(P payload) {
        this.payload = payload;
    }

    public String getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    public Integer getPayloadTypeVersion() {
        return payloadTypeVersion;
    }

    public void setPayloadTypeVersion(Integer payloadTypeVersion) {
        this.payloadTypeVersion = payloadTypeVersion;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public Date getCreated() {
        return created != null ? new Date(created.getTime()) : null;
    }

    public void setCreated(Date created) {
        this.created = created != null ? new Date(created.getTime()) : null;
    }

    public Date getModified() {
        return modified != null ? new Date(modified.getTime()) : null;
    }

    public void setModified(Date modified) {
        this.modified = modified != null ? new Date(modified.getTime()) : null;
    }
}
