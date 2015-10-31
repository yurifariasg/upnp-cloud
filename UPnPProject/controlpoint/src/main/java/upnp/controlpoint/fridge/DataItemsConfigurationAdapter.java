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

import android.app.Activity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.tpvision.sensormgt.controlpoint.fridge.R;
import com.tpvision.sensormgt.upnpcontrolpoint.model.DataItem;

import java.util.List;


public class DataItemsConfigurationAdapter extends ArrayAdapter<DataItem> {

	private Activity mContext;
	List<DataItem> mDataItems;
	int currentSelection=-1;


	static class ViewHolder {
		protected TextView itemName;
		protected EditText itemValue;
		protected String currentItemValue;
	}

	public DataItemsConfigurationAdapter(Activity context, List<DataItem> dataItems) {
		//  super(context, android.R.layout.simple_list_item_single_choice, devices);
		super(context, R.layout.dataitem_configuration_list_item, dataItems);
		mContext = context;
		mDataItems = dataItems;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		View rowView = null;
		if (convertView == null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			rowView = inflater.inflate(R.layout.dataitem_configuration_list_item, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.itemName = (TextView) rowView.findViewById(android.R.id.text1);
			viewHolder.itemValue = (EditText) rowView.findViewById(android.R.id.text2);
			viewHolder.itemValue.setInputType(InputType.TYPE_CLASS_NUMBER); 
			
			
			viewHolder.itemValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		        @Override
		        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        	
		            if (actionId == EditorInfo.IME_ACTION_DONE) {

		                //yourcalc();
		            	Log.e("TEMP","EDIT DONE");
		            	
		            	
		                return true;
		            }
		            return false;
		        }
		    });
			
			viewHolder.itemValue.setOnKeyListener(new OnKeyListener() {
			    public boolean onKey(View v, int keyCode, KeyEvent event) {
			        // If the event is a key-down event on the "enter" button
			        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
			            (keyCode == KeyEvent.KEYCODE_ENTER)) {
			          
			        	viewHolder.itemValue.clearFocus();
			        	DataItem dataItem = mDataItems.get(position);
			        	useValue(viewHolder, dataItem);
			        	
			          return true;
			        }
			        return false;
			    }
			});

			rowView.setTag(viewHolder);
			// viewHolder.checkedText.setTag(mDevices.get(position));
		} else {
			rowView = convertView;
			//((ViewHolder) rowView.getTag()).checkedText.setTag(mDevices.get(position));
		}
		ViewHolder holder = (ViewHolder) rowView.getTag();
		holder.itemName.setText(mDataItems.get(position).getName());
		holder.itemValue.setText(mDataItems.get(position).getValue());
		holder.currentItemValue = mDataItems.get(position).getValue();

		return rowView;
	}
	
	/**
	 * Copy value from text entry field to DataItem.writeSensorData which triggers the writeSensor SOAP action
	 * @param holder
	 * @param dataItem
	 */
	private void useValue(ViewHolder holder, DataItem dataItem) {
		
    	String itemValue = holder.itemValue.getText().toString();
    	
    	try {
    		int val = Integer.parseInt(itemValue);
    		//TODO: could check for limit in description
    		if (val>3600) {
    			//too large, restore the original value
    			holder.itemValue.setText(holder.currentItemValue);
    		} else {
    			if (dataItem!=null) {
	        		dataItem.writeSensorData(itemValue);
	        		holder.currentItemValue = itemValue;
	        	} else {
	        		holder.itemValue.setText(holder.currentItemValue);
	        	}
	        		
    		}
    	} catch (NumberFormatException e) {
    		//set back the previous value
    		holder.itemValue.setText(holder.currentItemValue);
    	}
	}
} 