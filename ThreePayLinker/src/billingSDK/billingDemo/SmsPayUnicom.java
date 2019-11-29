package billingSDK.billingDemo;

import com.unicom.dcLoader.Utils;
import com.unicom.dcLoader.Utils.UnipayPayResultListener;

import android.app.Activity;
import android.content.Context;
import billingSDK.billingDemo.SmsPayFactory.SmsExitListener;
import billingSDK.billingDemo.SmsPayFactory.SmsPayListener;

public class SmsPayUnicom extends SmsPayBase
{
	private static Activity _context;
	
	private String _appid;
	private String _cpId;
	private String _cpCode;
	private String _companyName;
	private String _telephone;
	private String _appName;
	private boolean _otherpay;
	
	private SmsPayUnicom(UnipayPayResultListener listener,String appName, String appId, String cpCode, 
			String cpId, String Company, String Telephone, boolean otherpay) {
		
		_appid = appId;
		_cpCode = cpCode;
		_cpId = cpId;
		_companyName = Company;
		_telephone = Telephone;
		_appName = appName;
		
		_otherpay = otherpay;
		
		try {
			Utils.getInstances().init(_context, _appid, _cpCode, _cpId, _companyName, _telephone, _appName, "uid", listener);
		} catch (Exception e) {
		}
	}
	
	private static SmsPayUnicom _singletonSmsPayUnicom;
	public static SmsPayUnicom getInstance() {
		return _singletonSmsPayUnicom;
	}
	
	public static SmsPayUnicom initSingleton(Activity context, String appName, String appId, String cpCode, 
			String cpId, String Company, String Telephone, boolean otherpay, UnipayPayResultListener listener) 
	{
		_context = context;
		if (_singletonSmsPayUnicom == null) {
			_singletonSmsPayUnicom = new SmsPayUnicom(listener,appName, appId, cpCode, cpId, Company, Telephone, otherpay);
		}
		return _singletonSmsPayUnicom;
	}
	
	@Override
	public void pay(Context context, String smsPayItem, String paycode_3rd, String props, String Money, String AppID, SmsPayListener listener, boolean isRepeated) {
		Utils.getInstances().setBaseInfo(context, _otherpay, true, "http://uniview.wostore.cn/log-app/test");
		Utils.getInstances().pay(context, paycode_3rd, smsPayItem, props, Money, AppID, listener);
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