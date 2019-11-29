package com.netmego.gdpay;

import android.app.Application;

public class CmgameApplication extends Application {
	public void onCreate() {
		System.loadLibrary("megjb");
	}
}
