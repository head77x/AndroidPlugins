package com.netmego.miguyouxinative;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.snowfish.cn.ganga.helper.SFOnlineHelper;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MiguSDKFactory 
{
	private static String sdk_version = "4.32";  
	
	private Handler mUIHandler = new Handler();
	
	private static String _appName;
	private static String _Company;
	private static String _Telephone;
	
	// SDK 선택자
	public enum SDKSelector {
		BILL_UNKNOWN,
		BILL_SP,
		BILL_YIJIE,
		BILL_GOOGLE,
		BILL_IAM
	};
	
	// 빌링 결과 처리 공통 리스너
	public interface BillingListener {
		public void onPurchaseSucceed(String item, String code3rd);
		public void onPurchaseCanceld(String item, String msg);
		public void onPurchaseFailed(String item, String msg);
		public void onPurchaseInfo(String item, String msg);
	};
	
	// 로그인 처리자
	public interface LoginListener {
		public void onLoginSuccess(String id, String name, String token);
		public void onLoginFailed(String msg);
		public void onLogOut(Object customParam);
	};
	
	// 게임 종료 처리 리스너 - CMGD 용
	public interface ExitGameListener {
		public void onExitGameCancelExit();
		public void onExitGameConfirmExit();
		public void onExitGameInGame();
	};
	
	private static boolean _bIniting = false;
	private static boolean _bInited = false;
	
	private static Activity _context;
	
	private SDKFactoryBase _smsPayer;
	private LoginListener MainLoginListener;
	
	private static MiguSDKFactory _singleton;

	public final static int opMISMATCHES	= 0x00000000;
	public final static int opIAM = 0x00000008;
	public final static int opGOOGLE = 0x00000010;
	public final static int opSP			= 0x00000020;
	public final static int opYIJIE		= 0x00000040;
	
	private String migu_appid;
	private String migu_appkey;
	private String migu_agentid;
	
	private String LoginID;
	private String UserName;
	private boolean bLogined;
	private String LoginToken;
	private String LoginMsg;
	
	// 넷미고 앱 ID 등 얻기
	public void getAppIDKey(Context con)
	{
        Bundle metaData = null;
		try 
		{
			metaData = con.getPackageManager().getApplicationInfo(con.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		migu_appid = metaData.getString("MIGU_CHANNEL_ID");
		migu_appkey = metaData.getString("MIGU_APP_KEY");
		migu_agentid = metaData.getString("MIGU_SUB_CHANNEL_ID");
	}
	
	// 넷미고 앱 아이디 얻기
	public String getAppID()
	{
		return migu_appid;
	}

	// 넷미고 앱 키 얻기
	public String getAppKey()
	{
		return migu_appkey;
	}
	
	// 로그인 토큰 얻기
	public String getLoginToken()
	{
		return LoginToken;
	}
	
	// 에이전트 아이디 얻기
	public String getAgentID()
	{
		return migu_agentid;
	}
	
	// 빌드 스타일 - BTypeValue 얻기
	public static SDKSelector getBuildType(Context con)
	{
        Bundle metaData = null;
		try 
		{
			metaData = con.getPackageManager().getApplicationInfo(con.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}

		// 강제로 셋팅된 SDK 선택자가 있는지 여부
		int BillType = opYIJIE;
		
		if ( metaData.getString("BTypeValue") == null )
		{
			System.out.println("BType Value : null");
			
			BillType = opYIJIE;
		}
		else
		try
		{
			BillType = Integer.valueOf(metaData.getString("BTypeValue"));
		}
		catch(Exception ect)
		{
			System.out.println("BType Value : exception" + ect);
			BillType = opYIJIE;
		}

		System.out.println("BType Value : " + BillType);
	
		Log.e("Application BTypeValue: ", BillType + "");

		if (BillType == opSP) {
			return SDKSelector.BILL_SP;
		}
		else
		if (BillType == opGOOGLE )
			return SDKSelector.BILL_GOOGLE;
		else
		if (BillType == opIAM )
			return SDKSelector.BILL_IAM;
			
		return SDKSelector.BILL_YIJIE;
		
	}
	
	// 구글 플레이용 GPK 얻기
	public static String getGooglePublicKey(Context con)
	{
        Bundle metaData = null;
		try 
		{
			metaData = con.getPackageManager().getApplicationInfo(con.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}

		String publickey = null;
		
		if ( metaData.getString("GPK") == null )
			return null;
		else
		try
		{
			publickey = metaData.getString("GPK");
		}
		catch(Exception ect)
		{
			publickey = null;
		}
		
		return publickey;
	}

	// 구글 플레이용 GPK 얻기
	public static boolean getLogToMego(Context con)
	{
        Bundle metaData = null;
		try 
		{
			metaData = con.getPackageManager().getApplicationInfo(con.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		if ( metaData.getString("LTM") == null )
			return false;
		
		return true;
	}
	
	// Paymentwall용 Key 얻기
	public static String getPaymentWallPublicKey(Context con)
	{
        Bundle metaData = null;
		try 
		{
			metaData = con.getPackageManager().getApplicationInfo(con.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}

		String publickey = null;
		
		if ( metaData.getString("PWK") == null )
			return null;
		else
		try
		{
			publickey = metaData.getString("PWK");
		}
		catch(Exception ect)
		{
			publickey = null;
		}
		
		return publickey;
	}

	// Paymentwall용 Key 얻기
	public static String getPaymentWallSecret(Context con)
	{
        Bundle metaData = null;
		try 
		{
			metaData = con.getPackageManager().getApplicationInfo(con.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}

		String publickey = null;
		
		if ( metaData.getString("PWSEC") == null )
			return null;
		else
		try
		{
			publickey = metaData.getString("PWSEC");
		}
		catch(Exception ect)
		{
			publickey = null;
		}
		
		return publickey;
	}
	
	private static SDKSelector getMobileOperatorApp(Context con)
	{
        TelephonyManager telManager = (TelephonyManager)con.getSystemService(Context.TELEPHONY_SERVICE); 
        String operator = telManager.getSimOperator();
        
        Bundle metaData = null;
		try 
		{
			metaData = con.getPackageManager().getApplicationInfo(con.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		// 강제로 셋팅된 SDK 선택자가 있는지 여부
		int BillType = opYIJIE;
		
		if ( metaData.getString("BTypeValue") == null )
			BillType = opYIJIE;
		else
		try
		{
			BillType = Integer.valueOf(metaData.getString("BTypeValue"));
		}
		catch(Exception ect)
		{
			BillType = opYIJIE;
		}
		
		Log.e("Application BTypeValue: ", BillType + "");

		if (BillType == opSP) {
			return SDKSelector.BILL_SP;
		} else if (BillType == opYIJIE) {
			return SDKSelector.BILL_YIJIE;
		}
		else
		if (BillType == opGOOGLE )
			return SDKSelector.BILL_GOOGLE;
		else
		if (BillType == opIAM )
			return SDKSelector.BILL_IAM;
		
		return SDKSelector.BILL_YIJIE;
	}
	
	
	private static SDKSelector getMobileOperatorMe(Activity __context)
	{
        TelephonyManager telManager = (TelephonyManager)__context.getSystemService(Context.TELEPHONY_SERVICE); 
        String operator = telManager.getSimOperator();
        
        Bundle metaData = null;
		try 
		{
			metaData = __context.getPackageManager().getApplicationInfo(__context.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		// 강제로 셋팅된 SDK 선택자가 있는지 여부
		int BillType = opYIJIE;
		if ( metaData.getString("BTypeValue") == null )
			BillType = opYIJIE;
		else
		try
		{
			BillType = Integer.valueOf(metaData.getString("BTypeValue"));
		}
		catch(Exception ect)
		{
			BillType = opYIJIE;
		}
		
		
		Log.e("BTypeValue: ", BillType + "");
		if (BillType == opSP) {
			return SDKSelector.BILL_SP;
		} else if (BillType == opYIJIE) {
			return SDKSelector.BILL_YIJIE;
		}
		else
			if (BillType == opGOOGLE )
				return SDKSelector.BILL_GOOGLE;
			else
			if (BillType == opIAM )
				return SDKSelector.BILL_IAM;
		
		return SDKSelector.BILL_YIJIE;
	}
	
	private static SDKSelector getMobileOperator() 
	{
        TelephonyManager telManager = (TelephonyManager)_context.getSystemService(Context.TELEPHONY_SERVICE); 
        String operator = telManager.getSimOperator();
        
        Bundle metaData = null;
		try 
		{
			metaData = _context.getPackageManager().getApplicationInfo(_context.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		// 강제로 셋팅된 SDK 선택자가 있는지 여부
		int BillType = opYIJIE;
		if ( metaData.getString("BTypeValue") == null )
			BillType = opYIJIE;
		else
		try
		{
			BillType = Integer.valueOf(metaData.getString("BTypeValue"));
		}
		catch(Exception ect)
		{
			BillType = opYIJIE;
		}
		
		Log.e("BTypeValue: ", BillType + "");

		if (BillType == opSP) {
			return SDKSelector.BILL_SP;
		} else if (BillType == opYIJIE) {
			return SDKSelector.BILL_YIJIE;
		}
		else
			if (BillType == opGOOGLE )
				return SDKSelector.BILL_GOOGLE;
			else
			if (BillType == opIAM )
				return SDKSelector.BILL_IAM;

		return SDKSelector.BILL_YIJIE;
    }
	
	// #2-1. 결제 모듈 팩토리 초기화
	public static void init(
			Activity context, BillingListener MainListener,
			String MM_appID, String MM_appkey, 
			String Company, String Telephone, String appName
			)
	{
		if (!_bIniting && !_bInited) 
		{
			_bIniting = true;
			_context = context;
			if (_singleton == null) 
			{
				System.out.println("MiguSDK Version : " + sdk_version);
				
				_Company = Company;
				_Telephone = Telephone;
				_appName = appName;
				
				_singleton = new MiguSDKFactory( MainListener,
						MM_appID, MM_appkey,
						Company, Telephone, appName);
			}
			_bIniting = false;
		}
	}
		
	// #2-2. 팩토리 생성자
	private MiguSDKFactory( BillingListener MainListener,
			String MM_appid, String MM_appkey,
			String Company, String Telephone, String appName
			) 
	{
		
		MainLoginListener = new LoginListener()
		{
			@Override
			public void onLoginSuccess(String id, String username, String token) 
			{
				LoginID = id;
				UserName = username;
				LoginToken = token;
				bLogined = true;
			}
			
			@Override
			public void onLoginFailed(String msg) 
			{
				bLogined = false;
				LoginMsg = msg;
			}
			
			@Override
			public void onLogOut(Object customParam)
			{
				
			}
		};
		
		switch (getBuildType(_context)) 
		{
			case BILL_SP:
				migu_appid = MM_appid;
				migu_appkey = MM_appkey;
				SP_Manager.initSingleton(_context, MainListener);
			break;
			case BILL_GOOGLE:
				getAppIDKey(_context);
				GoogleIAP_Manager.initSingleton(_context, MainListener, getGooglePublicKey(_context), getLogToMego(_context));
				_bInited = true;
				login();
			break;
			case BILL_IAM:
				getAppIDKey(_context);
				IAM_Manager.initSingleton(_context, MainListener, getPaymentWallPublicKey(_context), getPaymentWallSecret(_context));
				_bInited = true;
				login();
			break;
			default:
				getAppIDKey(_context);
				YiJie_Manager.initSingleton(_context, MainListener);
				break;
		}
		
		_bInited = true;
	}
	
	
	public Activity getContext() {
		return _context;
	}
	
	public synchronized static MiguSDKFactory getInstance() 
	{
		return _singleton;
	}
	
	protected void initFinished() {
		_bInited = true;
	}
	
	public void login()
	{
		if (!_bInited) 
		{
			System.out.println("Brandon : not yet init !!!!");
		}
		else  
		{
			switch (getBuildType(_context)) 
			{
				case BILL_YIJIE:		// YIJIE
					_smsPayer = YiJie_Manager.getInstance();
					_smsPayer.trylogin(_context, MainLoginListener);
					break;
				case BILL_SP:	// ZHEXIN
					bLogined = true;
					LoginID = "ZhexinSP";
					UserName = "ZhexinLocal";
				break;
				case BILL_GOOGLE:
					bLogined = true;
					LoginID = "GoogleSP";
					UserName = "GoogleLocal";
//					_smsPayer = GoogleIAP_Manager.getInstance();
//					_smsPayer.trylogin(_context, MainLoginListener);
					break;
				case BILL_IAM:
					_smsPayer = IAM_Manager.getInstance();
					_smsPayer.trylogin(_context, MainLoginListener);
				break;				
				default:
					bLogined = true;
					LoginID = "Default";
					UserName = "ZhexinLocal";
				break;
			}
		}
	}
		
	public void pay(Context context, String smsPayItem, String paycode_3rd, String props, String Money, BillingListener listener, boolean isRepeated) 
	{
		if (!_bInited) 
		{
			System.out.println("Brandon : not yet init !!!!");
		}
		else  
		{
			switch (getBuildType(_context)) 
			{
				case BILL_YIJIE:		// YIJIE
					_smsPayer = YiJie_Manager.getInstance();
					_smsPayer.pay(context, smsPayItem, paycode_3rd, props, Money, listener, isRepeated);
					break;
				case BILL_SP:	// ZHEXIN
					_smsPayer = SP_Manager.getInstance();
					_smsPayer.pay(context, smsPayItem, paycode_3rd, props, Money, listener, isRepeated);
				break;
				case BILL_GOOGLE:
					_smsPayer = GoogleIAP_Manager.getInstance();
					_smsPayer.pay(context, smsPayItem, paycode_3rd, props, Money, listener, isRepeated);
					break;
				case BILL_IAM:
					_smsPayer = IAM_Manager.getInstance();
					_smsPayer.pay(context, smsPayItem, paycode_3rd, props, Money, listener, isRepeated);
					break;
				default:
					listener.onPurchaseFailed(smsPayItem, "no singleton!!!!");
				break;
			}
			
		}
	}
	
	public void exitGame(Context context, ExitGameListener listener) 
	{
		switch (getBuildType(_context)) 
		{
			case BILL_YIJIE:		// 여기에 ANYSDK 처리할것
				_smsPayer = YiJie_Manager.getInstance();
			break;
			case BILL_SP:
				_smsPayer = SP_Manager.getInstance();
			break;
			case BILL_GOOGLE:
				_smsPayer = GoogleIAP_Manager.getInstance();
			break;
			case BILL_IAM:
				_smsPayer = IAM_Manager.getInstance();
			break;
			default:
				listener.onExitGameConfirmExit();
			return;
		}
		
		System.out.println("Brandon : ask exit!!!! :");
		
		_smsPayer.exitGame(context, listener);
	}
	
	public void viewMoreGames(Context context) 
	{
		switch (getBuildType(_context)) 
		{
		case BILL_YIJIE:		// 여기에 ANYSDK 처리할것
			_smsPayer = YiJie_Manager.getInstance();
			break;
			case BILL_SP:
				_smsPayer = SP_Manager.getInstance();
			break;
			case BILL_GOOGLE:
				_smsPayer = GoogleIAP_Manager.getInstance();
			break;
			case BILL_IAM:
				_smsPayer = IAM_Manager.getInstance();
			break;
			default:
			return;
		}
		
		_smsPayer.viewMoreGames(context);
	}
	
	public boolean isMusicEnabled() 
	{
		switch (getBuildType(_context)) 
		{
			case BILL_YIJIE:		// 여기에 ANYSDK 처리할것
				_smsPayer = YiJie_Manager.getInstance();
			break;
			case BILL_SP:
				_smsPayer = SP_Manager.getInstance();
			break;
			case BILL_GOOGLE:
				_smsPayer = GoogleIAP_Manager.getInstance();
			break;
			case BILL_IAM:
				_smsPayer = IAM_Manager.getInstance();
			break;
			default:
			return true;
		}
		
		return _smsPayer.isMusicEnabled();
	}	
	
	// 로그인 상태 얻기
	public boolean isLogined()
	{
		return bLogined;
	}
	
	// 로그인 아이디 얻기
	public String getLoginID()
	{
		return LoginID;
	}
	
	// 로그인 유저명 얻기
	public String getLoginUserName()
	{
		return UserName;
	}
	
	public void doScreenShotShare(Context context, final Uri uri)
	{
		switch (getBuildType(_context)) 
		{
			case BILL_YIJIE:		// 여기에 ANYSDK 처리할것
				_smsPayer = YiJie_Manager.getInstance();
			break;
			case BILL_SP:
				_smsPayer = SP_Manager.getInstance();
			break;
			case BILL_GOOGLE:
				_smsPayer = GoogleIAP_Manager.getInstance();
			break;
			case BILL_IAM:
				_smsPayer = IAM_Manager.getInstance();
			break;
			default:
			return;
		}
		
		_smsPayer.doScreenShotShare(context, uri);
	}
	
	public void Pause(Context context)
	{
		System.out.println("onPause");
		
		switch (getBuildType(_context)) 
		{
			case BILL_YIJIE:		// 여기에 ANYSDK 처리할것
				SFOnlineHelper.onPause((Activity)context);
			break;
			case BILL_GOOGLE:
				SFOnlineHelper.onPause((Activity)context);
			break;
			case BILL_IAM:
				SFOnlineHelper.onPause((Activity)context);
			break;
		}
	}
	
	public void Resume(Context context)
	{
		System.out.println("onResume");

		switch (getBuildType(_context)) 
		{
			case BILL_YIJIE:		// 여기에 ANYSDK 처리할것
				SFOnlineHelper.onResume((Activity)context);
			break;
			case BILL_GOOGLE:
				SFOnlineHelper.onResume((Activity)context);
			break;
			case BILL_IAM:
				SFOnlineHelper.onResume((Activity)context);
			break;
			default:
			return;
		}
	}
	
	public void Stop(Context context)
	{
		System.out.println("onStop");

		switch (getBuildType(_context)) 
		{
			case BILL_YIJIE:		// 여기에 ANYSDK 처리할것
				SFOnlineHelper.onStop((Activity)context);
			break;
			case BILL_GOOGLE:
				SFOnlineHelper.onStop((Activity)context);
			break;
			case BILL_IAM:
				SFOnlineHelper.onStop((Activity)context);
			break;
			
			default:
			return;
		}
		
	}

	public void Restart(Context context)
	{
		System.out.println("onRestart");

		switch (getBuildType(_context)) 
		{
			case BILL_YIJIE:		// 여기에 ANYSDK 처리할것
				SFOnlineHelper.onRestart((Activity)context);
			break;
			case BILL_GOOGLE:
				SFOnlineHelper.onRestart((Activity)context);
			break;
			case BILL_IAM:
				SFOnlineHelper.onRestart((Activity)context);
			break;
			
			default:
			return;
		}
		
	}
	
	public void Destroy(Context context)
	{
		System.out.println("onDestroy");
		 
		switch (getBuildType(_context)) 
		{
			case BILL_YIJIE:		// 여기에 ANYSDK 처리할것
				SFOnlineHelper.onDestroy((Activity)context);
			break;
			case BILL_GOOGLE:
				SFOnlineHelper.onDestroy((Activity)context);
			if ( GoogleIAP_Manager.getInstance() != null )
				GoogleIAP_Manager.getInstance().IAPDestroy();
			break;
			case BILL_IAM:
				SFOnlineHelper.onDestroy((Activity)context);
			break;
			default:
			return;
		}
	}
	
	// 구글 플레이용 처리를 위함
    public void ActivityResult(int requestCode, int resultCode, Intent data) 
    {
		System.out.println("onActivityResult");
		 
		switch (getBuildType(_context)) 
		{
			case BILL_YIJIE:		// 여기에 ANYSDK 처리할것
			break;
			case BILL_GOOGLE:
			if ( GoogleIAP_Manager.getInstance() != null )
				GoogleIAP_Manager.getInstance().ActivityResult(requestCode, resultCode, data);
			break;
			case BILL_IAM:
				if ( IAM_Manager.getInstance() != null )
					IAM_Manager.getInstance().ActivityResult(requestCode, resultCode, data);
			break;
			default:
			return;
		}
    }
	
	
	public void AboutUs(Context context)
	{
		showDialog(context, "关于", "应用名称:" + _appName + "\n应用类型:手游\n公司名称:浙江弥谷网络科技有限公司\n客服电话:57182877709\n" );
	}
	
	public void showDialog(Context context, String title, String msg) {
        final String curMsg = msg;
        final String curTitle = title;
        final Context __context = context;

        mUIHandler.post( new Runnable()
		{
            @Override
            public void run() {
                new AlertDialog.Builder(__context)
                .setTitle(curTitle)
                .setMessage(curMsg)
                .setPositiveButton("Ok", 
                        new DialogInterface.OnClickListener() {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                
                            }
                        }).create().show();
            }
        });
    }
	
	

	public void NoticeToMiguServer(final String payidx, final String money, final String channel) throws ClientProtocolException, IOException
	{
		Thread thread1 = new Thread(new Runnable()
	    {
		    public void run()
		    { 
	    	try {		
				HttpPost request = makeHttpPost( "http://open.miguyouxi.com/index.php", migu_appid, migu_appkey, payidx, money, channel);
				HttpClient client = new DefaultHttpClient() ;  
				ResponseHandler<String> reshandler = new BasicResponseHandler() ;
				String result = client.execute( request, reshandler ) ;  
				System.out.println("Brandon : log result : " + result);
	    		}
	           catch (Exception e)
	           {
	           }

		    }
	    });
		
	    thread1.start();
	}
	
	private HttpPost makeHttpPost(String url, String appid, String appkey, String payidx, String money, String channel) 
	{  
		// TODO Auto-generated method stub  
		HttpPost request = new HttpPost( url ) ;  
		Vector<NameValuePair> nameValue = new Vector<NameValuePair>() ;  
		nameValue.add( new BasicNameValuePair( "appid", appid ) ) ; 
		
        String key = getMD5Hash( appkey + "com.miguyouxi");
                
        String orderkey = getMD5Hash( appid + Secure.ANDROID_ID + System.currentTimeMillis() );
        
		nameValue.add( new BasicNameValuePair( "key", key ) ) ;  
		nameValue.add( new BasicNameValuePair( "action", "setLog" ) ) ;  
		nameValue.add( new BasicNameValuePair( "order", orderkey ) ) ;  
		nameValue.add( new BasicNameValuePair( "player", Secure.ANDROID_ID ) ) ;  
		nameValue.add( new BasicNameValuePair( "item", payidx ) ) ;  
		nameValue.add( new BasicNameValuePair( "money", money ) ) ;  
		nameValue.add( new BasicNameValuePair( "channel", channel ) ) ;  
		request.setEntity( makeEntity(nameValue) ) ;  
		return request ;  
	}  
	
	private HttpEntity makeEntity( Vector<NameValuePair> $nameValue ) 
	{  
		HttpEntity result = null ;  
		try {  
			result = new UrlEncodedFormEntity( $nameValue ) ;  
		} catch (UnsupportedEncodingException e) 
		{  
			// TODO Auto-generated catch block  
			e.printStackTrace();  
		}  

		return result ;  
	}  
	
	public static String getMD5Hash(String s) {
		  MessageDigest m = null;
		  String hash = null;

		  try {
		    m = MessageDigest.getInstance("MD5");
		    m.update(s.getBytes(),0,s.length());
		    hash = new BigInteger(1, m.digest()).toString(16);
		  } catch (NoSuchAlgorithmException e) {
		    e.printStackTrace();
		  }
		  return hash;
		}  	
}
