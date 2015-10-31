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

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tpvision.sensormgt.controlpoint.fridge.R;
import com.tpvision.sensormgt.upnpcontrolpoint.model.DataItem;


public class TemperatureSensorView extends SensorViewBase {

	static class ViewHolder {
		protected ImageView sign;
		protected ImageView tenDigit;
		protected ImageView digit;
		protected ImageView deciDigit;
		protected TextView name;
	}

	private static final String TAG = "TemperatureSensorView";

	private int mSignLevel;
	private int mTenLevel;
	private int mDigitLevel;
	private int mDeciLevel;
	private String mName;

	public TemperatureSensorView(DataItem dataItem) {
		super(dataItem);
		
		if (dataItem !=null) { 
			valueToView(dataItem.getValue());
			mName = dataItem.getName();
		}
		mSensorType = SensorViewBase.TYPE_TEMPERATURE;
	}

	private void valueToView(String value) {
		
		mSignLevel = 0;
		mTenLevel = 0;
		mDigitLevel = 0;
		mDeciLevel = 0;
		
		if (value!=null) {
			try {
				Float fval= Float.parseFloat(value);
				if (fval<0) { 
					mSignLevel = 1; 
					fval = Math.abs(fval);
				}
				
				fval = fval * 10;
				int intval = (int) Math.floor(fval);
				String stringval = Integer.toString(intval);
				
				if (stringval.length()==1) {
					mTenLevel = 10; //off
					mDigitLevel = 10; //off
					mDeciLevel = Integer.parseInt(stringval.substring(0,1));
				}
				
				if (stringval.length()==2) {
					mTenLevel = 10; //off
					mDigitLevel = Integer.parseInt(stringval.substring(0,1));
					mDeciLevel = Integer.parseInt(stringval.substring(1,2));
				}
				
				if (stringval.length()==3) {
					mTenLevel = Integer.parseInt(stringval.substring(0,1));
					mDigitLevel = Integer.parseInt(stringval.substring(1,2));
					mDeciLevel = Integer.parseInt(stringval.substring(2,3));
				}
				
				
				
			} catch (NumberFormatException e) {
				Log.e(TAG,"Error reading temperature value: "+e);
			}
		}
		
	}
	
	@Override
	public View getView(Context context, int position, View convertView, final ViewGroup parent) {
		View rowView = null;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.seven_seg_view, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.sign = (ImageView) rowView.findViewById(R.id.sign);
			viewHolder.tenDigit = (ImageView) rowView.findViewById(R.id.ten_digit);
			viewHolder.digit = (ImageView) rowView.findViewById(R.id.digit);
			viewHolder.deciDigit = (ImageView) rowView.findViewById(R.id.deci_digit);
			viewHolder.name = (TextView) rowView.findViewById(R.id.nameTextView);
			
		//	viewHolder.friendlyName = (TextView) rowView.findViewById(R.id.friendlyNameTextView);

			rowView.setTag(viewHolder);
			//		  viewHolder.checkedText.setTag(mDevices.get(position));
		} else {
			rowView = convertView;
			//	      ((ViewHolder) rowView.getTag()).checkedText.setTag(mDevices.get(position));
		}
		ViewHolder holder = (ViewHolder) rowView.getTag();

		holder.sign.setImageLevel(mSignLevel);
		holder.tenDigit.setImageLevel(mTenLevel);
		holder.digit.setImageLevel(mDigitLevel);
		holder.deciDigit.setImageLevel(mDeciLevel);
		
		holder.name.setText(mName);
		
	//	holder.friendlyName.setText("Friendly"); //mDevices.get(position).getFriendlyName()

		return rowView;
	}

	@Override
	protected void refreshData() {
		if (mDataItem !=null) { 
			valueToView(mDataItem.getValue());
		}
	}
	
} 