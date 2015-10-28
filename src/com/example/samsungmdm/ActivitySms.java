package com.example.samsungmdm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.admin.DevicePolicyManager;
import android.app.enterprise.DeviceInventory;
import android.app.enterprise.EnterpriseDeviceManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class ActivitySms extends ActivityCall {
	public static final String TAG = "activitySMS";
	private TextView textView;

	public void onClick(View v) throws IOException {
		switch (v.getId()) {
					
		case R.id.writeALLSMSs:
			writeAllSMSs(null, null);
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms);

		// Ensure application has device admin privileges
		ComponentName deviceAdmin = new ComponentName(this,
				MyDeviceAdminReceiver.class);
		DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

		if (!dpm.isAdminActive(deviceAdmin)) {
			Log.d(TAG, "enabling application as device admin");

			try {
				Intent intent = new Intent(
						DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
				intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
						deviceAdmin);
				intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "");
				startActivity(intent);
			} catch (Exception e) {
				Log.d(TAG, "Exception: " + e);
			}
		} else {
			Log.d(TAG, "Application already has device admin privileges");
		}
	}

	public void enableSMSCapture(boolean status) {
		EnterpriseDeviceManager edm = (EnterpriseDeviceManager) getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
		DeviceInventory deviceInventoryPolicy = edm.getDeviceInventory();

		try {
			boolean result = deviceInventoryPolicy.enableSMSCapture(true);
			if (true == result) {
				// SMS logging enabled.
			}
		} catch (SecurityException e) {
			Log.w(TAG, "SecurityException: " + e);
		}
	}
	
	public static String LastElement(String log) {

        String last = log.substring(log.length() - 1); 
		
		return last;
	}

	public List<String> getInboundSMSCaptured() {
		EnterpriseDeviceManager edm = (EnterpriseDeviceManager) getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
		DeviceInventory deviceInventoryPolicy = edm.getDeviceInventory();

		List<String> list = new ArrayList<String>();
		List<String> outlist = new ArrayList<String>();

		try {
			deviceInventoryPolicy.enableSMSCapture(true);
			// The device has likely logged some SMS messages at some point
			// after
			// enabling the policy.
			list = deviceInventoryPolicy.getInboundSMSCaptured();

			String separator1 = ";";

			TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String imei = telemamanger.getDeviceId(); // imei
			
			for (String log : list) {
				String character = "$";
				String newlog = log + character;
				outlist.add(newData(getPieceOfStr("TimeStamp:", " - ", log)) + separator1
						+ "In" + separator1
						+ getPieceOfStr("From:", " - ", log) + separator1 
						+  "\ufeff" + getPieceOfStr("Body:", LastElement(newlog), newlog) + separator1 
						);
			}

			System.out.print(outlist);

		} catch (SecurityException e) {
			Log.w(TAG, "SecurityException: " + e);
		}
		return outlist;
	}

	public List<String> getOutboundSMSCaptured() {
		EnterpriseDeviceManager edm = (EnterpriseDeviceManager) getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
		DeviceInventory deviceInventoryPolicy = edm.getDeviceInventory();

		List<String> list = new ArrayList<String>();
		List<String> outlist = new ArrayList<String>();

		String separator1 = ";";
		
		TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		try {
			deviceInventoryPolicy.enableSMSCapture(true);
			// The device has likely logged some SMS messages at some point
			// after
			// enabling the policy.
			list = deviceInventoryPolicy.getOutboundSMSCaptured();

			for (String log : list) {
				String character = "$";
				String newlog = log + character;
				outlist.add(newData(getPieceOfStr("TimeStamp:", " - ", log)) + separator1
						+ "Out" + separator1
						+ getPieceOfStr("To:", " - ", log) + separator1 
						+ "\ufeff" + getPieceOfStr("Body:", LastElement(newlog), newlog) + separator1);
			}

		} catch (SecurityException e) {
			Log.w(TAG, "SecurityException: " + e);
		}
		return outlist;
	}

	public void enableSMSCapture(View sms) {
		boolean checked = ((CheckBox) sms).isChecked();

		if (checked) {
			enableSMSCapture(true);
		} else {
			enableSMSCapture(false);
		}
	}

	// create sorted List<String> of all calls
	public void writeAllSMSs(List<String> InSMSs, List<String> OutSMSs) {

		List<String> AllSMSs_list = new ArrayList<String>();

		InSMSs = getInboundSMSCaptured();
		OutSMSs = getOutboundSMSCaptured();

		// separator for excel
		String separator = ";";

		AllSMSs_list = InSMSs;

		AllSMSs_list.addAll(OutSMSs);

		// sort from old to new calls
		Collections.sort(AllSMSs_list);

		// add info on top of file
		AllSMSs_list.add(0, "Time" + separator + "Body" + separator
				+ "SMS Number" + separator + "Operator");

		try {
			FileOutputStream fileout = openFileOutput("SMSsLog.csv",
					MODE_MULTI_PROCESS);
			OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);

			for (int i = 0; i < AllSMSs_list.size(); i++) {
				outputWriter.write(AllSMSs_list.get(i) + "\n");
			}

			Collections.reverse(AllSMSs_list);

			outputWriter.flush();
			outputWriter.close();

			// display file saved message
			Toast.makeText(getBaseContext(), "File saved successfully!",
					Toast.LENGTH_SHORT).show();

			copyFile("/data/data/com.example.samsungmdm/files/SMSsLog.csv",
					"/storage/emulated/0/KNOX_Logs/SMSslog.csv");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}