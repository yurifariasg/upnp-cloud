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

package upnp.controlpoint.fridge.view;

import java.util.ArrayList;
import java.util.Iterator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.tpvision.sensormgt.upnpcontrolpoint.model.DataItem;
import com.tpvision.sensormgt.upnpcontrolpoint.model.DataItem.SensorDataItemReadListener;
import com.tpvision.sensormgt.upnpcontrolpoint.model.Sensor;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorCollection;
import com.tpvision.sensormgt.upnpcontrolpoint.model.SensorURN;


public abstract class SensorViewBase implements SensorDataItemReadListener {
	
	private static final String TAG = null;
	
	public static final int TYPE_OTHER = 0;
	public static final int TYPE_ENERGY = 1;
	public static final int TYPE_TEMPERATURE = 2;
	
	protected int mSensorType = TYPE_OTHER;
	
	public String mFriendlyname;
	
	public interface DataChangeListener {
		public void onDataChanged();
	}
	
	private SensorCollection mSensorCollection;
	private String mSensorID;
	private String mSensorURN;

	private DataChangeListener mListener;

	protected DataItem mDataItem;
	
	public SensorViewBase(DataItem dataItem) {
		mDataItem = dataItem;
		dataItem.readSensorData();
		dataItem.addSensorDataItemReadListener(this);
		//supportsOnOff = true;	
	}
	
	public void setOnDataChangeListener(DataChangeListener listener) {
		mListener = listener;
	}
	
	public void removeOnDataChangeListener() {
		mListener = null;
	}
	
	public int getSensorType() {
		return mSensorType;
	}
	
	public DataItem getDataItem() {
		return mDataItem;
	}
	
	public abstract View getView(Context context, int position, View convertView, final ViewGroup parent);
	
	public SensorCollection getSensorCollection() {
		return mSensorCollection;
	}
	
	//TODO: move ??
	public DataItem findDataItem(String itemName) {
		
		ArrayList<Sensor> sensors = mSensorCollection.getSensors();
		Iterator<Sensor> si = sensors.iterator();
		boolean found = false;
		DataItem dataItem=null;
		while (si.hasNext()&&!found) {
			Sensor sensor = si.next();
			ArrayList<SensorURN> sensorURNs = sensor.getSensorURNs();
			Iterator<SensorURN> su = sensorURNs.iterator();
			while (su.hasNext()&&!found) {
				SensorURN sensorURN = su.next();
				ArrayList<DataItem> dataItems = sensorURN.getDataItems();
				Iterator<DataItem> dii = dataItems.iterator();
				while (dii.hasNext()&&!found) {
					dataItem = dii.next();
					found = dataItem.getName().contentEquals(itemName);
				}
			}
		}
		
		return dataItem;
	}
	
	public Boolean changeFriendlyName(String newName)
	{		
//		if (this.controlpoint!=null)
//		{			
//			if (this.controlpoint.sendMessage(this.currentUPnPDeviceUdn, CN_Networks_Enum.RF4CENetwork.name(), 
//					this.nodeId, HA_ActionTypeEnum.CHANGE_NAME.name(), newName))
//			{
//				this.friendlyname = newName;
//				return true;
//			}
//		}
		return false;
	}
	
	public Boolean supportsOnOff()
	{	
		return false;
	}
	
	//do nothing for this sensor type
	public boolean activate() {
		return true;
	}

//	public static int getItemPosByNodeId(List<HomeControlItem> homeControlItems, String nodeId)
//	{	
//		try
//		{
//			for (int i=0;i<homeControlItems.size();i++)
//			{
//				if (homeControlItems.get(i).nodeId.equalsIgnoreCase(nodeId))
//				{
//					return i;
//				}
//			}
//		}catch(Exception e)
//		{}
//		return -1;
//	}
		
	//Override this to trigger re-initialising the Views data
	protected void refreshData() {
		
	}

	@Override
	public void onSensorDataItemRead(DataItem dataItem) {
		Log.w(TAG,"DataItem event recieved from "+dataItem.getName());
		refreshData();
		if (mListener!= null) mListener.onDataChanged();
	}

	
}
