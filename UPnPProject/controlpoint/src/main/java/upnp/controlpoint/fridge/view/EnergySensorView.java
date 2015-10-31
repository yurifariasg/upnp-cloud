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

import com.tpvision.sensormgt.controlpoint.fridge.R;
import com.tpvision.sensormgt.upnpcontrolpoint.model.DataItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import upnp.controlpoint.uicomponent.BargraphView;

public class EnergySensorView extends SensorViewBase {

	private String mName;
	private int mLevel=0;
	
	static class ViewHolder {
		protected BargraphView bargraph;
		protected TextView friendlyName;
	}

	public EnergySensorView(DataItem dataItem) {
		super(dataItem);
		
		if (dataItem !=null) { 
			setLevel(dataItem.getValue());
			mName = dataItem.getName();
		}
		
		mSensorType = SensorViewBase.TYPE_ENERGY;
		
		
	}

	private void setLevel(String level) {
		
		try {
			mLevel = Integer.parseInt(level)/10;
		} catch (NumberFormatException e) {
			mLevel = 0;
		}
	}
	
	
	@Override
	public View getView(Context context, int position, View convertView, final ViewGroup parent) {
		View rowView = null;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.energy_sensor_view, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.bargraph = (BargraphView) rowView.findViewById(R.id.bargraphView1);
			viewHolder.friendlyName = (TextView) rowView.findViewById(R.id.friendlyNameTextView);

			rowView.setTag(viewHolder);
			//		  viewHolder.checkedText.setTag(mDevices.get(position));
		} else {
			rowView = convertView;
			//	      ((ViewHolder) rowView.getTag()).checkedText.setTag(mDevices.get(position));
		}
		ViewHolder holder = (ViewHolder) rowView.getTag();
		holder.friendlyName.setText(mName+": "+mLevel+"0 KW/h");
		holder.bargraph.setLevel(mLevel);
		
		return rowView;
	}
	
	@Override
	protected void refreshData() {
		if (mDataItem !=null) { 
			setLevel(mDataItem.getValue());
		}
	}

} 