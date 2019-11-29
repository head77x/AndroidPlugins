package billingSDK.billingDemo;

import com.estore.lsms.tools.ApiParameter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import billingSDK.billingDemo.SmsPayFactory.SmsExitListener;
import billingSDK.billingDemo.SmsPayFactory.SmsPayListener;

public class SmsPayTelecom_CTE extends SmsPayBase
{
	private String _channelID;
	
	private SmsPayTelecom_CTE(String channelID, String seccode, String appName, String Company) 
	{
		_channelID = channelID;
		
		System.out.println("Brandon : dian xin Init :" + _channelID);
	}
	
	private static SmsPayTelecom_CTE _singletonSmsPayTelecom;
	public static SmsPayTelecom_CTE getInstance() {
		return _singletonSmsPayTelecom;
	}
	
	public static SmsPayTelecom_CTE initSingleton(Activity context, String channelID, String seccode, String appName, String Company ) {
		if (_singletonSmsPayTelecom == null) {
			_singletonSmsPayTelecom = new SmsPayTelecom_CTE(channelID, seccode, appName, Company);
		}
		return _singletonSmsPayTelecom;
	}
	
	@Override
	public void pay(Context context, String smsPayItem, String paycode_3rd, String props, String Money, String AppID, SmsPayListener listener, boolean isRepeated) 
	{
		Intent intent = new Intent();
		intent.setClass(context, com.estore.ui.CTEStoreSDKActivity.class);
		
		Bundle bundle = new Bundle();

/*		
		bundle.putString(ApiParameter.APNAME, _Company);
		bundle.putString(ApiParameter.APPNAME, _appName);
		bundle.putString(ApiParameter.APSECRET, _seccode);
*/		
		bundle.putString(ApiParameter.APPCHARGEID, smsPayItem);
		bundle.putString(ApiParameter.CHANNELID, _channelID);
		bundle.putBoolean(ApiParameter.SCREENHORIZONTAL, true);
		bundle.putString(ApiParameter.CHARGENAME, props);
		bundle.putInt(ApiParameter.PRICETYPE, 0); // 0 : monthly, 1 : time
		bundle.putString(ApiParameter.PRICE, Money);
		bundle.putString(ApiParameter.REQUESTID, AppID);
		intent.putExtras(bundle);
		((Activity) context).startActivityForResult(intent, 0);
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
