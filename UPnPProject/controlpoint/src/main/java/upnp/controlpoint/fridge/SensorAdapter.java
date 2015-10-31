/* Copyright (c) 2013, TP Vision Holding B.V. 
 * All rights reserved.
 
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of TP Vision nor the  names of its contributors may
      be used to endorse or promote products derived from this software
      without specific prior written permission.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL TP VISION HOLDING B.V. BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package upnp.controlpoint.fridge;

import java.util.ArrayList;
import java.util.Iterator;

import com.tpvision.sensormgt.upnpcontrolpoint.model.DataItem;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import upnp.controlpoint.fridge.view.EnergySensorView;
import upnp.controlpoint.fridge.view.SensorDefaultView;
import upnp.controlpoint.fridge.view.SensorViewBase;
import upnp.controlpoint.fridge.view.TemperatureSensorView;

public class SensorAdapter extends BaseAdapter implements SensorViewBase.DataChangeListener
{        
    private static final String TAG = "SensorAdapter";
	private Context mContext;  
    public ArrayList<SensorViewBase> mSensors;
    
	public SensorAdapter(final Context context, ArrayList<SensorViewBase> sensors) 
    {
    	super();
        mContext = context;  
        mSensors = sensors;
    }         
	
	public boolean containsDataItem(DataItem dataItem) {
		
		boolean found = false;
		Iterator<SensorViewBase> it = mSensors.iterator();
		while (it.hasNext()&&!found) {
			SensorViewBase sensor = it.next();
			found = (sensor.getDataItem()!=null)&&(sensor.getDataItem().getPath()==dataItem.getPath());
		}
		
		return found;
	}
	
	@Override
	public final synchronized long getItemId(final int position) 
    {
        return position;
    }
	
	@Override
	public int getCount() 
    {
        return mSensors.size();
    }

	@Override
    public Object getItem(int position) 
    {
        return mSensors.get(position);
    }    
    
    @Override
    public int getViewTypeCount() {
        return 3;
    }
    
    @Override
    public int getItemViewType(int position) {
    	
    	SensorViewBase sv = mSensors.get(position);
    		
    	return sv.getSensorType();
    }
    
    @Override
    public synchronized View getView(final int position, View convertView, final ViewGroup parent) 
    {
    	SensorViewBase sv = mSensors.get(position);

    	View view;
    	switch (sv.getSensorType()) {
    	case SensorViewBase.TYPE_TEMPERATURE:
    		view = ((TemperatureSensorView) sv).getView(mContext, position, convertView, parent);
    		break;
    	case SensorViewBase.TYPE_ENERGY:
    		view = ((EnergySensorView) sv).getView(mContext, position, convertView, parent);
    		break;
    	default:
    		view = ((SensorDefaultView) sv).getView(mContext, position, convertView, parent);
    		break;
    	}
    	
    	return view;
    }

	@Override
	public void onDataChanged() {
		
		Log.w(TAG,"Adapter data changed, send notifyDataSetChanged");
		
		((Activity)mContext).runOnUiThread(new Runnable() {
			public void run() { 
				notifyDataSetChanged(); 
			}
		});
	}

//    @Override
//    public void onDataChanged(DataItem dataItem) {
//
//
//    	String descr = dataItem.getDescription();
//    	if (descr!=null) {
//    		if (descr.contains("itemname=\"DoorOpenAlarm\"")) {
//    			if (dataItem.getValue().contentEquals("1")) {
//    				
//    				((Activity)context).runOnUiThread(new Runnable() {
//    	    			public void run() { 
//    	    				Toast toast = Toast.makeText(context, "Don't leave the virtual door open\nYou'll get a huge virtual bill.", Toast.LENGTH_SHORT);
//    	    				toast.show();
//    	    			}
//    	    			
//    	    		});
//    				
//    			}
//    		} else {
//
//        		((Activity)context).runOnUiThread(new Runnable() {
//        			public void run() { 
//        				notifyDataSetChanged(); 
//        			}
//        		});
//        	}
//    	} 
//    }
	
	
}



