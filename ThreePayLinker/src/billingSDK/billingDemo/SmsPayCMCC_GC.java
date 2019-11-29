package billingSDK.billingDemo;

import cn.cmgame.billing.api.GameInterface;
import billingSDK.billingDemo.SmsPayFactory.SmsExitListener;
import billingSDK.billingDemo.SmsPayFactory.SmsPayListener;
import android.app.Activity;
import android.content.Context;

public class SmsPayCMCC_GC extends SmsPayBase
{
	private SmsPayCMCC_GC() {}

	
	private SmsPayCMCC_GC(Activity activity, SmsPayListener listener) {
		
		GameInterface.initializeApp(activity);
	}
	
	private static SmsPayCMCC_GC _singletonSmsPayCMCC;
	public static SmsPayCMCC_GC initSingleton(Activity context, SmsPayListener listener) {
		if (_singletonSmsPayCMCC == null) {
			_singletonSmsPayCMCC = new SmsPayCMCC_GC(context, listener);
		}
		return _singletonSmsPayCMCC;
	}
	
	public static SmsPayCMCC_GC getInstance() {
		return _singletonSmsPayCMCC;
	}

	@Override
	public void pay(Context context, String smsPayItem, String props, String paycode_3rd, String Money, String AppID, SmsPayListener listener, boolean isRepeated) {
		GameInterface.doBilling(context, true, isRepeated, smsPayItem, null, listener);
	}


	@Override
	public void exitGame(Context context, SmsExitListener listener) {
		GameInterface.exit(context, listener);
	}


	@Override
	public void viewMoreGames(Context context) {
		GameInterface.viewMoreGames(context);
	}


	@Override
	public boolean isMusicEnabled() {
		return GameInterface.isMusicEnabled();
	}
}
