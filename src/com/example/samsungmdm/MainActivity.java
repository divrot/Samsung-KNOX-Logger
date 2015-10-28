package com.example.samsungmdm;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.app.enterprise.EnterpriseDeviceManager;
import android.app.enterprise.RestrictionPolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity implements View.OnClickListener {

	public static final String TAG = "DEBUG";

	public int REQUEST_ENABLE;
	private Button mActivateLicenseButton;
	private Button toActivitySms;
	private Button toActivityCall;
	private Button toUploadToServer;

	private EnterpriseDeviceManager mEnterpriseDeviceManager;
	private RestrictionPolicy mRestrictionPolicy;
	private DevicePolicyManager mDPM;
	private ComponentName mAdminName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mEnterpriseDeviceManager = (EnterpriseDeviceManager) getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
		mRestrictionPolicy = mEnterpriseDeviceManager.getRestrictionPolicy();

		mActivateLicenseButton = (Button) findViewById(R.id.activateLicenseButton);
		mActivateLicenseButton.setOnClickListener((OnClickListener) this);

		toActivitySms = (Button) findViewById(R.id.toActivitySms);
		toActivitySms.setOnClickListener((OnClickListener) this);

		toActivityCall = (Button) findViewById(R.id.toActivityCall);
		toActivityCall.setOnClickListener((OnClickListener) this);
		
		toUploadToServer = (Button)findViewById(R.id.toUploadToServer);
		toUploadToServer.setOnClickListener((OnClickListener) this);;
		
		File directory = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "KNOX_Logs"); // creating folder on device
		directory.mkdirs();

		findViewsInActivity();
		grantAdminPrivileges();
	}

	private void grantAdminPrivileges() {
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mAdminName = new ComponentName(this, MyDeviceAdminReceiver.class);

		if (!mDPM.isAdminActive(mAdminName)) {
			// Not yet device admin
			Intent intent = new Intent(
					DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					"This needs to be added");
			startActivityForResult(intent, REQUEST_ENABLE);
		}
	}

	private void findViewsInActivity() {

		mActivateLicenseButton = (Button) findViewById(R.id.activateLicenseButton);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.toActivitySms:
			Intent mySmsIntent = new Intent(MainActivity.this,
					ActivitySms.class);
			MainActivity.this.startActivity(mySmsIntent);
			break;
		case R.id.toActivityCall:
			Intent myCallIntent = new Intent(MainActivity.this,
					ActivityCall.class);
			MainActivity.this.startActivity(myCallIntent);
			break;
		case R.id.activateLicenseButton:
			ActivateLicense activateLicense = new ActivateLicense();
			activateLicense.applyInitialLicenses(MainActivity.this);
			break;
			
		case R.id.toUploadToServer:
			Intent myUploadIntent = new Intent(MainActivity.this,
					UploadToServer.class);
			MainActivity.this.startActivity(myUploadIntent);
			break;
		}
	}

	public void toActivityCall(View v) {
		Intent intent = new Intent(this, ActivityCall.class);
		startActivity(intent);
	}

	public void toActivitySms(View v) {
		Intent intent = new Intent(this, ActivityCall.class);
		startActivity(intent);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (REQUEST_ENABLE == requestCode) {
			if (resultCode == Activity.RESULT_OK) {
				// Has become the admin
				Toast.makeText(getBaseContext(), "Admin Rights Granted",
						Toast.LENGTH_SHORT).show();
			} else {
				// failed to become the admin
				Toast.makeText(getBaseContext(), "Admin Rights Denied",
						Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Request code is: " + requestCode
						+ ", Result OK is: " + Activity.RESULT_OK);
			}
		}
	}
}
