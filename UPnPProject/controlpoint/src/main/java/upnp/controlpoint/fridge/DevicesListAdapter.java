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

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tpvision.sensormgt.controlpoint.fridge.R;


public class DevicesListAdapter extends ArrayAdapter<UPnPDeviceInfo> {
  private final Activity context;
  List<UPnPDeviceInfo> mDevices;
  int currentSelection=-1;

  static class ViewHolder {
	protected ImageView image;
	protected CheckedTextView  checkedText;
	protected TextView smallText;
  }

  public DevicesListAdapter(Activity context, List<UPnPDeviceInfo> devices) {
  //  super(context, android.R.layout.simple_list_item_single_choice, devices);
	  super(context, R.layout.devices_list_item, devices);
    this.context = context;
    this.mDevices = devices;
  }

  @Override
  public View getView(int position, View convertView, final ViewGroup parent) {
	  View rowView = null;
	  if (convertView == null) {
		  LayoutInflater inflater = context.getLayoutInflater();
		  rowView = inflater.inflate(R.layout.devices_list_item, null);
		  final ViewHolder viewHolder = new ViewHolder();
		  viewHolder.checkedText = (CheckedTextView) rowView.findViewById(R.id.text1);
//		  viewHolder.checkedText.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View view) {
//				Log.d("adapter","clicked");
//			}
//		        
//		  });
		  viewHolder.smallText = (TextView) rowView.findViewById(R.id.smalltext);
		  
		  
		  rowView.setTag(viewHolder);
		  viewHolder.checkedText.setTag(mDevices.get(position));
	  } else {
		  rowView = convertView;
	      ((ViewHolder) rowView.getTag()).checkedText.setTag(mDevices.get(position));
	  }
	  ViewHolder holder = (ViewHolder) rowView.getTag();
	  holder.checkedText.setText(mDevices.get(position).getFriendlyName());
	  //holder.checkedText.setChecked(mDevices.get(position).isUsed());
	  holder.checkedText.setEnabled(mDevices.get(position).isFound());
	  holder.smallText.setText(mDevices.get(position).getUDN());
	  holder.smallText.setEnabled(mDevices.get(position).isFound());
	  
	  return rowView;
  }
  
  @Override
  public boolean areAllItemsEnabled() {
      return true; //no separators
  }
  
} 