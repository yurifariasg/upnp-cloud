package com.control.ws.model;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;

import org.json.simple.JSONObject;

import com.control.ws.model.interfaces.ISourcedDeviceUpnp;

public abstract class SourcedDeviceUpnp extends DeviceUpnp implements ISourcedDeviceUpnp {

    private EnumSet<Source> sources;

    public SourcedDeviceUpnp(SourcedDeviceUpnp device){
        super(device);
        sources = device.sources;
    }
    public SourcedDeviceUpnp(String uuid, String type, String name) {
        super(uuid, type, name);
        sources = EnumSet.noneOf(Source.class);
    }

    public EnumSet<Source> getSources() {
        return sources;
    }

    public void setSources(EnumSet<Source> sources) {
        this.sources = sources;
    }

    public boolean isAvailable() {
        return !getSources().isEmpty();
    }
    
    @Override
    public String toString() {
        return "SourcedDeviceUpnp [sources=" + sources + ", uuid=" + getUuid()  + ", name=" + getName() + "]";
    }

	
	@Override
	public JSONObject toJSON() {
		JSONObject object = new JSONObject();
		object.put("type", getType());
		object.put("id", encrypt(getDescription().getJid()));
		return object;
	}
	
	public String encrypt(String str) {
		try {
	        final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
	        messageDigest.update(str.getBytes(), 0, str.length());
	        return new BigInteger(1, messageDigest.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

}
