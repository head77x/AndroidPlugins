package com.netmego.unityprintermanager;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;

import com.newland.mtype.ModuleType;
import com.newland.mtype.module.common.printer.FontSettingScope;
import com.newland.mtype.module.common.printer.FontType;
import com.newland.mtype.module.common.printer.LiteralType;
import com.newland.mtype.module.common.printer.PrintContext;
import com.newland.mtype.module.common.printer.Printer;
import com.newland.mtype.module.common.printer.PrinterResult;
import com.newland.mtype.module.common.printer.PrinterStatus;
import com.newland.mtype.module.common.printer.ThrowType;
import com.newland.mtype.module.common.printer.WordStockType;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class PrinterActivity extends UnityPlayerActivity 
{
	private N900Device n900Device;
	
	public static Activity getUnityActivity_() {
        return UnityPlayer.currentActivity;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		Log.d("Brandon", "Brandon : success create printeractivity");
		
		super.onCreate(savedInstanceState);
		n900Device=N900Device.getInstance(this);
		
	}
		
    //It is a black magic in onPause and onResume
    //to make the unity view not disappear when return from background
    //Something about inactive activity which might be not reload when resume from bg.
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    
	// 4.3 추가 내용
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	@Override
	protected void onStop() 
	{
		super.onStop();
	}
	 
	@Override
	protected void onRestart() 
	{
		// TODO Auto-generated method stub
		super.onRestart();
	}
	 
	 @Override
	 protected void onDestroy() {
		 super.onDestroy();
	 };
	
		// Connect Device
		public void connectDevice() {
						try {
							if (!n900Device.isDeviceAlive()) {
								n900Device.connectDevice();
							} else {
								// tvOperationMessage.append("Device is connected" + "\r\n");
							}
						} catch (Exception e) {
							// tvOperationMessage.append("Device connect exception" + e + "\r\n");
						}
				}
	 
	
	public void OnPrint(
			final String printstring, final String callbackGameObject, final String callbackFunc )
	{
		Printer printer = (Printer) n900Device.getPrinter(); // Step 1： Printer initialization this method must be called once before printing. 
		printer.init(); // Step 2： start printing, and the parameters include printing information, timeout and timeout unit 
		printer.print(printstring, 30, TimeUnit.SECONDS);
	}
	
	// Displays the information returned by the operation.
	public void showMessage(final String mess, final int messageType) {
				switch (messageType) {
				case 1:
//					message = "<font color='black'>" + mess + "</font>";
					break;
				case 2:
//					message = "<font color='red'>" + mess + "</font>";
					break;
				case 0:
//					message = "<font color='green'>" + mess + "</font>";
					break;
				case 3:
//					message = "<font color='blue'>" + mess + "</font>";
					break;
				default:
					break;
				}
//				newMessage = message + "<br>" + newMessage;
//				tvOperationMessage.setText(Html.fromHtml(newMessage,getImageGetter(),null));
	}

	
	
}
