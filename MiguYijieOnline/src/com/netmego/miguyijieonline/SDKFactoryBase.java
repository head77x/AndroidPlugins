package com.netmego.miguyijieonline;

import com.netmego.miguyijieonline.MiguSDKFactory.BillingListener;
import com.netmego.miguyijieonline.MiguSDKFactory.ExitGameListener;
import com.netmego.miguyijieonline.MiguSDKFactory.LoginListener;

import android.content.Context;
import android.net.Uri;

public abstract class SDKFactoryBase
{
	public abstract void trylogin(Context context, LoginListener listener);
	public abstract void pay(Context context, String smsPayItem, String paycode_3rd, String props, String Money, BillingListener listener, boolean isRepeated);
	public abstract void exitGame(Context context, ExitGameListener listener);
	public abstract void viewMoreGames(Context context);
	public abstract void doScreenShotShare(Context context, Uri uri);
}
