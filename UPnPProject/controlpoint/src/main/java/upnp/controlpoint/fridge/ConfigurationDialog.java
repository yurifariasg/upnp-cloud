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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.tpvision.sensormgt.controlpoint.fridge.R;
import com.tpvision.sensormgt.upnpcontrolpoint.model.DataItem;

import java.util.ArrayList;



public class ConfigurationDialog extends DialogFragment
{
	private ArrayList<DataItem> mAssociatedDataItems= new ArrayList<DataItem>();
	private ListView mListView;

	private FridgeApplication mApplication;

	private DataItemsConfigurationAdapter mDataItemsConfigurationAdapter;
	
	static ConfigurationDialog newInstance() {
		return new ConfigurationDialog();
    }
	
	public ConfigurationDialog() 
	{
		super();
		
//		this.setCanceledOnTouchOutside(true);		
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//notifyDataSetChanged();
	}
	
	public void setAssociatedDataItems(ArrayList<DataItem> dataItems) {
		mAssociatedDataItems = dataItems;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	
		//Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.configuration_dialog, null);
		mListView = (ListView) layout.findViewById(R.id.listView1);
		mApplication = (FridgeApplication) (getActivity().getApplication());
		                
		mDataItemsConfigurationAdapter = new DataItemsConfigurationAdapter(getActivity(), mAssociatedDataItems);
		mListView.setAdapter(mDataItemsConfigurationAdapter);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.configuration_dialog_title)
		.setView(layout);
		
		Dialog dialog = builder.create();

		return dialog;
	}
	
	
}
