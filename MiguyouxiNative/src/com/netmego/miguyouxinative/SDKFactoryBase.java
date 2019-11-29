package com.netmego.miguyouxinative;

import com.netmego.miguyouxinative.MiguSDKFactory.BillingListener;
import com.netmego.miguyouxinative.MiguSDKFactory.ExitGameListener;
import com.netmego.miguyouxinative.MiguSDKFactory.LoginListener;

import android.content.Context;
import android.net.Uri;

public abstract class SDKFactoryBase
{
	public abstract void trylogin(Context context, LoginListener listener);
	public abstract void pay(Context context, String smsPayItem, String paycode_3rd, String props, String Money, BillingListener listener, boolean isRepeated);
	public abstract void exitGame(Context context, ExitGameListener listener);
	public abstract void viewMoreGames(Context context);
	public abstract boolean isMusicEnabled();
	public abstract void doScreenShotShare(Context context, Uri uri);
}
