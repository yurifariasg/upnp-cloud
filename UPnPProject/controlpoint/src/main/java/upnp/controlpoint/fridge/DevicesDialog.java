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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.tpvision.sensormgt.controlpoint.fridge.R;
import com.tpvision.sensormgt.upnpcontrolpoint.upnpinterface.UPnPDeviceList.UPnPDeviceListListener;

import java.util.ArrayList;
import java.util.List;

public class DevicesDialog extends DialogFragment implements UPnPDeviceListListener {

	// Debugging
	public final String TAG = this.getClass().getSimpleName(); //get the class name for debugging
	private static final boolean DEBUG = true;

	private List<UPnPDeviceInfo> mDevices;
	private ArrayList<UPnPDeviceInfo> mDevicesOriginal;
	private DevicesListAdapter mDeviceListAdapter;
	private FridgeApplication mApplication;
	private ListView mListView;

	private int mPreviousPos=-1;
	
	static DevicesDialog newInstance() {
		return new DevicesDialog();
    }

	public void setDiscoveredDevices(List<UPnPDeviceInfo> discoveredDevices) {
		mDevices = discoveredDevices;
		mDevicesOriginal = new ArrayList<UPnPDeviceInfo>(discoveredDevices);
		//notifyDataSetChanged();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		//Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_devices_list, null);
		mListView = (ListView) layout.findViewById(R.id.listView1);
		mApplication = (FridgeApplication) (getActivity().getApplication());
		
		//get current list of discovered devices, and register for receiving updates
		mDevices = mApplication.getDiscoveredDevices();
		((FridgeApplication) (getActivity().getApplication())).getUPnP().setDeviceListListener(this);
		
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mDeviceListAdapter = new DevicesListAdapter(getActivity(), mDevices);
		mListView.setAdapter(mDeviceListAdapter);
		
		//due to custom layout, android doesn't find the checkbox. 
		//find the previous checkbox and uncheck it
		//find the current checkbox and check it
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if ((mPreviousPos!=-1) && (mPreviousPos != position)) {
					View item = (View) mListView.getChildAt(mPreviousPos);
					if (item!=null) {
						CheckedTextView previousCheckBox = (CheckedTextView) item.findViewById(R.id.text1);
						if (previousCheckBox!=null) previousCheckBox.setChecked(false);
					}
				}
				
				mListView.setItemChecked(position, true);
				mPreviousPos = position; //store for next click
				CheckedTextView checkBox = (CheckedTextView) view.findViewById(R.id.text1);
				if (checkBox!=null) checkBox.setChecked(true);
			}
		});
		
		for (int i = 0; i < mListView.getCount(); i++) { 
			mListView.setItemChecked(i, false); 
		}
		
		

		builder.setTitle(R.string.dialog_title_devices)
		.setView(layout);

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				int position = mListView.getCheckedItemPosition();
				Log.e(TAG,"Position :"+position);
				
				
				if (position!=ListView.INVALID_POSITION) {
					mApplication.setCurrentDevice(mDevices.get(position));
				} else {
					mApplication.setCurrentDevice(null);
				}
				
				//notify the host application of this dialog of the change
				mListener.onSelectionUpdated(DevicesDialog.this);
			}
		});

		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog, restore the original settings
				mDevices = mDevicesOriginal;
				mDevicesOriginal = null;
				
				notifyDataSetChanged();
				
				//notify the host application of this dialog of the change
				mListener.onSelectionUpdated(DevicesDialog.this);
			}
		});

		Dialog dialog = builder.create();

		return dialog;
	}

	public void notifyDataSetChanged() {
		
		for (int i = 0; i < mListView.getCount(); i++) { 
			mListView.setItemChecked(i, false); 
		}
		
		if (mDeviceListAdapter!=null) mDeviceListAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
//FIXME:		
//		mApplication.getUPnP().removeDeviceListListener();
	}

	private Runnable mDeviceListChangedRunnable = new Runnable() {

		@Override
		public void run() { 
			notifyDataSetChanged();
		}
	};
	
	@Override
	public void onDeviceListChanged() {
		
		Log.d(TAG,"Discovered Devices list changed");
		
		mDevices = ((FridgeApplication) (getActivity().getApplication())).getDiscoveredDevices();
		
		//update the list in the fragment
		getActivity().runOnUiThread(mDeviceListChangedRunnable);
	}
	
	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface DialogNotificationListener {
        public void onSelectionUpdated(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    DialogNotificationListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the DialogNotificationListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DialogNotificationListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
	

}