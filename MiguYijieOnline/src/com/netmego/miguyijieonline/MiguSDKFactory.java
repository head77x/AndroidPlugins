package com.netmego.miguyijieonline;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
import android.telephony.TelephonyManager;
import android.util.Log;

import com.netmego.miguyijieonline.MiguSDKFactory;
import com.netmego.miguyijieonline.SDKFactoryBase;
import com.netmego.miguyijieonline.YiJie_Manager;
import com.netmego.miguyijieonline.MiguSDKFactory.BillingListener;
import com.netmego.miguyijieonline.MiguSDKFactory.ExitGameListener;
import com.netmego.miguyijieonline.MiguSDKFactory.LoginListener;
import com.netmego.miguyijieonline.MiguSDKFactory.SDKSelector;
import com.snowfish.cn.ganga.helper.SFOnlineHelper;

public class MiguSDKFactory 
{
	private static String sdk_version = "1.01";  
	
	private Handler mUIHandler = new Handler();
	
	private static String _appName;
	private static String _Company;
	private static String _Telephone;
	
	// SDK 선택자
	public enum SDKSelector {
		BILL_UNKNOWN,
		BILL_YIJIE,
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
		if (BillType == opYIJIE) {
			return SDKSelector.BILL_YIJIE;
		}

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
		if (BillType == opYIJIE) {
			return SDKSelector.BILL_YIJIE;
		}
		
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

		if (BillType == opYIJIE) {
			return SDKSelector.BILL_YIJIE;
		}

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
			default:
			return;
		}
		
		_smsPayer.viewMoreGames(context);
	}
	
	public boolean isMusicEnabled() 
	{
		return true;
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

			default:
			return;
		}
	}
	
	// 구글 플레이용 처리를 위함
    protected void ActivityResult(int requestCode, int resultCode, Intent data) 
    {
		System.out.println("onActivityResult");
		 
		switch (getBuildType(_context)) 
		{
			case BILL_YIJIE:		// 여기에 ANYSDK 처리할것
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
