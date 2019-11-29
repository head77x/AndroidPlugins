package billingSDK.billingDemo;

import mm.sms.purchasesdk.SMSPurchase;
import mm.sms.purchasesdk.OnSMSPurchaseListener;
import android.app.Activity;
import android.content.Context;
import billingSDK.billingDemo.SmsPayFactory.SmsExitListener;
import billingSDK.billingDemo.SmsPayFactory.SmsPayListener;

public class SmsPayCMCC_MM extends SmsPayBase
{
	private static Activity _context;
	
	private String _appid;
	private String _appKey;
	
	private SmsPayCMCC_MM(OnSMSPurchaseListener listener, String MM_appid, String MM_appkey) {
		
		_appid = MM_appid;
		_appKey = MM_appkey;
		
		try {
			SMSPurchase.getInstance().setAppInfo(_appid, _appKey);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			SMSPurchase.getInstance().smsInit(_context, listener);
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}
	
	private static SmsPayCMCC_MM _singletonSmsPayCMCCMM;
	public static SmsPayCMCC_MM getInstance() {
		return _singletonSmsPayCMCCMM;
	}
	
	public static SmsPayCMCC_MM initSingleton(Activity context, String MM_appid, String MM_appkey, OnSMSPurchaseListener listener) {
		_context = context;
		if (_singletonSmsPayCMCCMM == null) {
			_singletonSmsPayCMCCMM = new SmsPayCMCC_MM(listener, MM_appid, MM_appkey);
		}
		return _singletonSmsPayCMCCMM;
	}
	
	@Override
	public void pay(Context context, String smsPayItem, String props, String paycode_3rd, String Money, String AppID, SmsPayListener listener, boolean isRepeated) {
		SMSPurchase.getInstance().smsOrder(context, smsPayItem, listener);
	}

	@Override
	public void exitGame(Context context, SmsExitListener listener) {
		listener.forceExitingGame();
	}

	@Override
	public void viewMoreGames(Context context) {
		// Do nothing...
	}

	@Override
	public boolean isMusicEnabled() {
		return true;
	}
}