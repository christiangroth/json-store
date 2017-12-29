package de.chrgroth.jsonstore;

import java.util.Date;

import flexjson.JSON;

/**
 * Represents metadata information about any JSON store instance.
 *
 * @author Christian Groth
 * @param <P>
 *            type of object being serialized and deserialized in order to store and load objects of type T
 */
public class JsonStoreMetadata<P> {

    // TODO get rid of this annotation!!
    @JSON
    private P payload;
    private String uid;
    private int payloadTypeVersion;
    private boolean singleton;
    private Date created;
    private Date modified;

    public P getPayload() {
        return payload;
    }

    public void setPayload(P payload) {
        this.payload = payload;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getPayloadTypeVersion() {
        return payloadTypeVersion;
    }

    public void setPayloadTypeVersion(int payloadTypeVersion) {
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
