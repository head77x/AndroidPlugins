package billingSDK.billingDemo;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import billingSDK.billingDemo.SmsPayFactory.SmsPayListener;
import billingSDK.billingDemo.SmsPayFactory.SmsPurchaseListener;

import com.estore.lsms.tools.ApiParameter;

/*
 * Activity for Telecom_CTE
 */
public class Telecom_CTE_Activity extends Activity 
{
	
	public static String _smsPayItem;
	public static String _props;
	public static String _money;
	public static String _appid;
	public static SmsPayBase _smsPayer;
	public static SmsPayFactory _factory;
	public static SmsPurchaseListener _listener;
	public static SmsPayListener _paylistener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		_smsPayer.pay((Context)this, _smsPayItem, "", _props, _money, _appid, _paylistener, false);
		
	}

	/**
	 * Telecom Listener
	 */
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
        Bundle bdl = data.getExtras();
        int payResultCode = bdl.getInt(ApiParameter.RESULTCODE);

        if (ApiParameter.CTESTORE_SENDSUCCESS == payResultCode) {
        	// ��퍡��에�묌��먨뒣
        	Log.e("======�듕에鸚⑴옘令븅뿴 SDK=======", "��퍡��에�묌��먨뒣");
            _listener.onPurchaseSucceed();
            finish();
        } else if (ApiParameter.CTESTORE_SENDFAILED == payResultCode) {
        	// ��에�묌�鸚김뇰
        	if (_listener != null) {
				_listener.onPurchaseFailed("��에�묌�鸚김뇰");
			}
        	Log.e("======�듕에鸚⑴옘令븅뿴 SDK=======", "��에�묌�鸚김뇰");
        	finish();
        } else if (ApiParameter.CTESTORE_USERCANCEL == payResultCode) {
        	// �ⓩ댎訝삣뒯�뽪텋
        	if (_listener != null) {
				_listener.onPurchaseCanceld();
			}
        	Log.e("======�듕에鸚⑴옘令븅뿴 SDK=======", "�ⓩ댎訝삣뒯�뽪텋");
        	finish();
        } else {
        	// �앭쭓�뽩ㅁ兀�        	if (_listener != null) {
				_listener.onPurchaseFailed("�앭쭓�뽩ㅁ兀�");
			}
        	Log.e("======�듕에鸚⑴옘令븅뿴 SDK=======", "�앭쭓�뽩ㅁ兀�");
        	finish();
    }
}