package com.control.ws;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.control.ws.database.DBManager;
import com.control.ws.model.DeviceUpnp;
import com.control.ws.model.Sensor;
import com.control.ws.model.SourcedDeviceUpnp;
import com.control.ws.model.db.SensorValue;
import com.control.ws.xmpp.data.DeviceDescription;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("devices")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("all")
    public Response getAll() {
    	
    	Collection<DeviceUpnp> deviceList = DBManager.getInstance().getDevices();

    	JSONObject json = new JSONObject();
    	try {
	    	JSONArray jsonArray = new JSONArray();
	    	for (DeviceUpnp device : deviceList) {
	    		jsonArray.add(device.toJSON());
	    	}
	    	json.put("devices", jsonArray);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
        return Response.ok(json.toJSONString(), MediaType.APPLICATION_JSON_TYPE).build();
    }
    
    @GET
    public Response getDevice(@QueryParam("id") String id) {
    	if (id == null || id.isEmpty()) {
    		return getAll();
    	}
    	
    	Set<DeviceUpnp> deviceList = DBManager.getInstance().getDevices();
    	
    	for (DeviceUpnp device : deviceList) {
    		JSONObject deviceJson = device.toJSON();
    		String deviceId = String.valueOf(deviceJson.get("id"));
    		if (deviceId.equals(id)) {
    			JSONObject deviceJSON = device.toJSON();
    			List<SensorValue> sensorValues = DBManager.getInstance().getValuesFor(device);
    			JSONObject sensorValuesJSON = new JSONObject();
    			if (sensorValues != null) {
	    			for (SensorValue value : sensorValues) {
	    				sensorValuesJSON.put(value.dataItem, value.value);
	    			}
    			}
    			
    			deviceJSON.put("body", sensorValuesJSON);
    			
    			return Response.ok(deviceJSON.toJSONString(), MediaType.APPLICATION_JSON_TYPE).build();
    		}
    	}
    	
        return Response.ok("{'error': 'not found'}", MediaType.APPLICATION_JSON_TYPE).build();
    }
}
