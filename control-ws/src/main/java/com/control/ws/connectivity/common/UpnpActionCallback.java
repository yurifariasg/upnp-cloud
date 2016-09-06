package com.control.ws.connectivity.common;

import java.util.Map;

public abstract class UpnpActionCallback implements Runnable{
    protected Map<String,Object> mResponse;
    private boolean supportsFail = false;
    private boolean actionFailed = false;
    
    public UpnpActionCallback(){
        this(false);
    }
    
    public UpnpActionCallback(boolean supportsFail){
    	this.supportsFail = supportsFail;
    }
    public void setResponse(Map<String,Object> response){
        mResponse = response;
    }
    public Map<String,Object> getResponse(){
        return mResponse;
    }
	public boolean isSupportsFail() {
		return supportsFail;
	}
	public void setSupportsFail(boolean supportsFail) {
		this.supportsFail = supportsFail;
	}

	public void setFailed(boolean failed) {
		actionFailed = true;
	}
	
	public boolean didActionFailed(){
		return actionFailed;
	}
}
