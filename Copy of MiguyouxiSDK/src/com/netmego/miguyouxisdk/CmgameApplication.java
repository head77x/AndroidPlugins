package com.netmego.miguyouxisdk;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.netmego.miguyouxisdk.MiguSDKFactory.SDKSelector;

public class CmgameApplication extends Application 
{
	@Override
	public void onCreate() 
	{
		super.onCreate();
		System.loadLibrary("megjb");
	}
	
}
