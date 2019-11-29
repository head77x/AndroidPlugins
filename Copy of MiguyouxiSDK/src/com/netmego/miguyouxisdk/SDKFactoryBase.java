package com.netmego.miguyouxisdk;

import com.netmego.miguyouxisdk.MiguSDKFactory.BillingListener;
import com.netmego.miguyouxisdk.MiguSDKFactory.ExitGameListener;

import android.content.Context;
import android.net.Uri;

public abstract class SDKFactoryBase 
{
	public abstract void pay(Context context, String smsPayItem, String paycode_3rd, String props, String Money, BillingListener listener, boolean isRepeated);
	public abstract void exitGame(Context context, ExitGameListener listener);
	public abstract void viewMoreGames(Context context);
	public abstract boolean isMusicEnabled();
	public abstract void doScreenShotShare(Context context, Uri uri);
}
