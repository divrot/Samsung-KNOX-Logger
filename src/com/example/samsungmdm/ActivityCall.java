package com.example.samsungmdm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
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

public class ActivityCall extends Activity {
	private TextView textView;
	private static final String TAG = "activityCall";

	public void onclick(View v) throws IOException {
		switch (v.getId()) {
//		case R.id.WriteInCALL:
//			writeInCALL(null);
//			break;
//
//		case R.id.WriteOutCall:
//			writeOutCALL(null);
//			break;

		case R.id.writeAllCalls:
			writeAllCALLs(null, null);
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call);

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
	
	public void enableCallingCapture(boolean enable) {
		EnterpriseDeviceManager edm = (EnterpriseDeviceManager) getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
		DeviceInventory deviceInventoryPolicy = edm.getDeviceInventory();

		try {
			boolean result = deviceInventoryPolicy.enableCallingCapture(true);
			if (true == result) {
				// call logging enabled
			}
		} catch (SecurityException e) {
			Log.w(TAG, "SecurityException: " + e);
		}
	}

	public static boolean copyFile(String from, String to) {
		try {
			@SuppressWarnings("unused")
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(from);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(from);
				FileOutputStream fs = new FileOutputStream(to);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// get piece of string
	public static String getPieceOfStr(String start, String end, String log) {

		int start_pos = log.indexOf(start) + start.length();
		String result = log.substring(start_pos, log.indexOf(end, start_pos));

		return result;
	}

	public static String newDuration(String Duration) {

		int IntDuration = Integer.parseInt(Duration);

		long second = (IntDuration / 1000) % 60;
		long minute = (IntDuration / (1000 * 60)) % 60;
		long hour = (IntDuration / (1000 * 60 * 60)) % 24;
		int IntRoundDuration = (int) Math.ceil(IntDuration % 100);

		String time = String.format("%02d:%02d:%02d:%03d", hour, minute,
				second, IntRoundDuration);

		return time;
	}

	public static String newData(String Data) {

		DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

		long milliSeconds= Long.parseLong(Data);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		
		return formatter.format(calendar.getTime());
	  }
 

	public List<String> getIncomingCallingCaptured() {
		EnterpriseDeviceManager edm = (EnterpriseDeviceManager) getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
		DeviceInventory deviceInventoryPolicy = edm.getDeviceInventory();

		List<String> list = new ArrayList<String>();
		List<String> outlist = new ArrayList<String>();

		TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String getSimSerialNumber = telemamanger.getSimSerialNumber(); // serial
		String imei = telemamanger.getDeviceId(); // imei
		String OperatorName = telemamanger.getSimOperatorName(); // operator
																	// name
		String separator = ";";

		try {
			deviceInventoryPolicy.enableCallingCapture(true);
			// The device has likely logged some calls at some point after
			// enabling the
			// policy.
			list = deviceInventoryPolicy.getIncomingCallingCaptured();

			if (0 < list.size()) {
				for (String log : list) {
					
					outlist.add(newData(getPieceOfStr("TimeStamp:", " - ", log)) + separator
							+ "In" + separator
							+ getPieceOfStr("From:", " - ", log) + separator
							+ imei + separator
							+ newDuration(getPieceOfStr("Duration:", " - ", log)) + separator
							+ getSimSerialNumber + separator
							+ OperatorName);

				}
			}
		} catch (SecurityException e) {
			Log.w(TAG, "SecurityException: " + e);
		}

		return outlist;
	}

	public List<String> getOutgoingCallingCaptured() {
		EnterpriseDeviceManager edm = (EnterpriseDeviceManager) getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
		DeviceInventory deviceInventoryPolicy = edm.getDeviceInventory();

		List<String> list = new ArrayList<String>();
		List<String> outlist = new ArrayList<String>();

		TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String getSimSerialNumber = telemamanger.getSimSerialNumber(); // serial
		String imei = telemamanger.getDeviceId(); // imei
		String OperatorName = telemamanger.getSimOperatorName();
		
		String separator = ";";

		try {
			deviceInventoryPolicy.enableCallingCapture(true);
			// The device has likely logged some calls at some point after
			// enabling the
			// policy.
			list = deviceInventoryPolicy.getOutgoingCallingCaptured();

			for (String log : list) {
				outlist.add(newData(getPieceOfStr("TimeStamp:", " - ", log)) + separator
						+ "Out" + separator + getPieceOfStr("To:", " - ", log)
						+ separator + imei + separator
						+ newDuration(getPieceOfStr("Duration:", " - ", log))
						+ separator + getSimSerialNumber + separator
						+ OperatorName);

			}
		} catch (SecurityException e) {
			Log.w(TAG, "SecurityException: " + e);
		}
		return outlist;
	}

	public void enableCallCapture(View call) {
		boolean checked = ((CheckBox) call).isChecked();
		if (checked) {
			enableCallingCapture(true);
			Toast.makeText(this, "enableCallingCapture(true)",
					Toast.LENGTH_SHORT).show();
		} else {
			enableCallingCapture(false);
			Toast.makeText(this, "enableCallingCapture(false)",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void showInCallLog(View v) {
		ArrayList<String> callTextListIn = (ArrayList<String>) getIncomingCallingCaptured();

		textView.setMovementMethod(new ScrollingMovementMethod());
		textView.setText("");

		for (int i = 0; i < callTextListIn.size(); i++) {
			textView.append("\n" + callTextListIn.get(i));
		}
	}

	public void showOutCallLog(View v) {
		ArrayList<String> callTextListOut = (ArrayList<String>) getOutgoingCallingCaptured();

		textView.setMovementMethod(new ScrollingMovementMethod());
		textView.setText("");

		for (int i = 0; i < callTextListOut.size(); i++) {
			textView.append("\n" + callTextListOut.get(i));
		}
	}
	
	//create sorted List<String> of all calls
	public void writeAllCALLs(List<String> InCalls, List<String> OutCalls) {

		List<String> AllCalls_list = new ArrayList<String>();

		InCalls = getIncomingCallingCaptured();
		OutCalls = getOutgoingCallingCaptured();
		
		//separator for excel
		String separator = ";";

		AllCalls_list = InCalls;
		
		AllCalls_list.addAll(OutCalls);
		
		//sort from old to new calls
		Collections.sort(AllCalls_list);
		
		//add info on top of file
		AllCalls_list.add(0, "Time" + separator + "Status" + separator
				+ "Call Number" + separator + "IMEI" + separator + "Duration"
				+ separator + "SIM(ICCiD)" + separator + "Operator");

		try {
			FileOutputStream fileout = openFileOutput("callsLog.csv",
					MODE_MULTI_PROCESS);
			OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
			// outputWriter.write(textView.getText().toString());

			for (int i = 0; i < AllCalls_list.size(); i++) {
				outputWriter.write(AllCalls_list.get(i) + "\n");
			}

			Collections.reverse(AllCalls_list);

			outputWriter.flush();

			outputWriter.close();

			// display file saved message
			Toast.makeText(getBaseContext(), "File saved successfully!",
					Toast.LENGTH_SHORT).show();

			copyFile("/data/data/com.example.samsungmdm/files/callsLog.csv",
					"/storage/emulated/0/KNOX_Logs/callslog.csv");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// ////////////////////////////////////////////////////////////////////////
}
