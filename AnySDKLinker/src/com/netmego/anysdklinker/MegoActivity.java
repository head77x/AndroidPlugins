package com.netmego.anysdklinker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.anysdk.framework.AdsWrapper;
import com.anysdk.framework.IAPWrapper;
import com.anysdk.framework.PluginWrapper;
import com.anysdk.framework.PushWrapper;
import com.anysdk.framework.ShareWrapper;
import com.anysdk.framework.SocialWrapper;
import com.anysdk.framework.UserWrapper;
import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKAds;
import com.anysdk.framework.java.AnySDKAnalytics;
import com.anysdk.framework.java.AnySDKIAP;
import com.anysdk.framework.java.AnySDKListener;
import com.anysdk.framework.java.AnySDKParam;
import com.anysdk.framework.java.AnySDKPush;
import com.anysdk.framework.java.AnySDKShare;
import com.anysdk.framework.java.AnySDKSocial;
import com.anysdk.framework.java.AnySDKUser;
import com.anysdk.framework.java.ToolBarPlaceEnum;

import com.unity3d.player.UnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

import android.os.Bundle;
import android.os.Handler;

import android.R.bool;
import android.R.string;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MegoActivity extends UnityPlayerActivity
{
	private static final String TAG_STRING = "ANYSDK";
	private static Activity mAct = null;
	private static Handler mUIHandler = null;
	private static boolean mFirst = false;
	private static Dialog myDialog = null;
	private final static String nd91Channle = "000007";
	private Map<String, String> mProductionInfo = null;
	private Map<String, String> mShareInfo = null;
	private ArrayList<String> mTagInfo = null;
	private Map<String, String> mArchInfo = null;
	
	private Handler postHandler = new Handler(); 
	
    String _callbackGameObject;
    String _callbackFunc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}
	
	
	public void onResume() 
	{  
        super.onResume();  
    }
	
    public void onPause() 
    {  
    	super.onPause();  
    }  
    
	void initAnySDK(final String appKey, final String appSecret, final String privateKey, final String oauthLoginServer)
	{
		AnySDK.getInstance().initPluginSystem(this, appKey, appSecret, privateKey, oauthLoginServer);
		
		/**
		 * 野밭뵪?루내瀯잆곫뵱餓섊내瀯잆곩뮈?딁내瀯잆곭퍨溫←내瀯잆곭ㅎ雅ㅷ내瀯잆곫렓?곭내瀯잆곩늽雅ョ내瀯잒?營췥ebug與▼폀
		 * 力ⓩ꼷竊쉊ebug與▼폀凉??펽?녑???틙SDK?꾣탩瑥뺞Æ凉륅펽?餓δ툓瀛욕뎺?▼퓚?둪ebug與▼폀溫양쉰訝튴alse
		 */
		AnySDKUser.getInstance().setDebugMode(true);
		AnySDKPush.getInstance().setDebugMode(true);
		AnySDKAnalytics.getInstance().setDebugMode(true);
		AnySDKAds.getInstance().setDebugMode(true);
		AnySDKShare.getInstance().setDebugMode(true);
		AnySDKSocial.getInstance().setDebugMode(true);
		AnySDKIAP.getInstance().setDebugMode(true);
		/**
		 * 訝븀내瀯잒?營?썞??		 */
		setListener();
		
	}
        
	public void setListener() {
		/**
		 * 訝븀뵪?루내瀯잒?營?썞??		 */
		AnySDKUser.getInstance().setListener(new AnySDKListener() {
			
			@Override
			public void onCallBack(int arg0, String arg1) {
				Log.d(String.valueOf(arg0), arg1);
				switch(arg0)
				{
				case UserWrapper.ACTION_RET_INIT_SUCCESS://?앭쭓?뻊DK?먨뒣?욆컘
					break;
				case UserWrapper.ACTION_RET_INIT_FAIL://?앭쭓?뻊DK鸚김뇰?욆컘
					Exit();
					break;
				case UserWrapper.ACTION_RET_LOGIN_SUCCESS://?삯솁?먨뒣?욆컘
					showDialog(arg1, "User is  online");
			        break;
				case UserWrapper.ACTION_RET_LOGIN_NO_NEED://?삯솁鸚김뇰?욆컘
				case UserWrapper.ACTION_RET_LOGIN_TIMEOUT://?삯솁鸚김뇰?욆컘
			    case UserWrapper.ACTION_RET_LOGIN_CANCEL://?삯솁?뽪텋?욆컘
				case UserWrapper.ACTION_RET_LOGIN_FAIL://?삯솁鸚김뇰?욆컘
					showDialog(arg1, "fail");
					AnySDKAnalytics.getInstance().logError("login", "fail");
			    	break;
				case UserWrapper.ACTION_RET_LOGOUT_SUCCESS://?삣눣?먨뒣?욆컘
					break;
				case UserWrapper.ACTION_RET_LOGOUT_FAIL://?삣눣鸚김뇰?욆컘
					showDialog(arg1  , "?삣눣鸚김뇰");
					break;
				case UserWrapper.ACTION_RET_PLATFORM_ENTER://亮녑룿訝?퓘瓦쎾뀯?욆컘
					break;
				case UserWrapper.ACTION_RET_PLATFORM_BACK://亮녑룿訝?퓘??뷴썮瘟?					break;
				case UserWrapper.ACTION_RET_PAUSE_PAGE://?귛걶?뚪씊?욆컘
					break;
				case UserWrapper.ACTION_RET_EXIT_PAGE://??뷸만?뤷썮瘟?			         Exit();
					break;
				case UserWrapper.ACTION_RET_ANTIADDICTIONQUERY://?꿩쾳瓦룡윥瑥℡썮瘟?					showDialog(arg1  , "?꿩쾳瓦룡윥瑥℡썮瘟?);
					break;
				case UserWrapper.ACTION_RET_REALNAMEREGISTER://若욃릫力ⓨ냼?욆컘
					showDialog(arg1  , "若욃릫力ⓨ냼?욆컘");
					break;
				case UserWrapper.ACTION_RET_ACCOUNTSWITCH_SUCCESS://?뉑뜟兀?뤇?먨뒣?욆컘
					break;
				case UserWrapper.ACTION_RET_ACCOUNTSWITCH_FAIL://?뉑뜟兀?뤇鸚김뇰?욆컘
					break;
				default:
					break;
				}
			}
		});
		
		/**
		 * 訝뷸뵱餓섊내瀯잒?營?썞??		 */
		AnySDKIAP.getInstance().setListener(new AnySDKListener() {
			
			@Override
			public void onCallBack(int arg0, String arg1) {
				Log.d(String.valueOf(arg0), arg1);
				String temp = "fail";
				switch(arg0)
				{
				case IAPWrapper.PAYRESULT_INIT_FAIL://??퍡?앭쭓?뽩ㅁ兀ε썮瘟?					
					break;
				case IAPWrapper.PAYRESULT_INIT_SUCCESS://??퍡?앭쭓?뽪닇?잌썮瘟?					
					break;
				case IAPWrapper.PAYRESULT_SUCCESS://??퍡?먨뒣?욆컘
					ArrayList<String> idArrayList =  AnySDKIAP.getInstance().getPluginId();
					String orderid = AnySDKIAP.getInstance().getOrderId(idArrayList.get(0));
					PurchaseResultSuccess(orderid);
					temp = "Success";
					showDialog(temp, temp);
					break;
				case IAPWrapper.PAYRESULT_FAIL://??퍡鸚김뇰?욆컘
					PurchaseResultFailed();
					showDialog(temp, temp);
					break;
				case IAPWrapper.PAYRESULT_CANCEL://??퍡?뽪텋?욆컘
					PurchaseResultFailed();
					showDialog(temp, "Cancel" );
					break;
				case IAPWrapper.PAYRESULT_NETWORK_ERROR://??퍡擁끾뿶?욆컘
					showDialog(temp, "NetworkError");
					break;
				case IAPWrapper.PAYRESULT_PRODUCTIONINFOR_INCOMPLETE://??퍡擁끾뿶?욆컘
					showDialog(temp, "ProductionInforIncomplete");
					break;
				case IAPWrapper.PAYRESULT_NOW_PAYING:
//					showTipDialog();
					break;
				default:
					break;
				}
			}
		});
		
		/**
		 * 訝뷴뮈?딁내瀯잒?營?썞??		 */
		AnySDKAds.getInstance().setListener(new AnySDKListener() {
			
			@Override
			public void onCallBack(int arg0, String arg1) {
				Log.d(String.valueOf(arg0), arg1);
				switch (arg0) {
				case AdsWrapper.RESULT_CODE_AdsDismissed://亮욕몜易덂ㅁ?욆컘
					break;
				case AdsWrapper.RESULT_CODE_AdsReceived://?ε룛?곁퐨瀯쒎썮瘟?					break;
				case AdsWrapper.RESULT_CODE_AdsShown://?양ㅊ營묊퍥?욆컘
					break;
				case AdsWrapper.RESULT_CODE_PointsSpendFailed://燁?늽罌숁텋兀밧ㅁ兀?					break;
				case AdsWrapper.RESULT_CODE_PointsSpendSucceed://燁?늽罌숁텋兀방닇??					break;
				case AdsWrapper.RESULT_CODE_OfferWallOnPointsChanged://燁?늽罌숂㎝?녷뵻??					break;
				case AdsWrapper.RESULT_CODE_NetworkError://營묊퍥?븅뵗
					break;

				default:
					break;
				}
				
			}
		});
		/**
		 * 訝뷴늽雅ョ내瀯잒?營?썞??		 */
		AnySDKShare.getInstance().setListener(new AnySDKListener() {
			
			@Override
			public void onCallBack(int arg0, String arg1) {
				Log.d(String.valueOf(arg0), arg1);
				switch (arg0) {
				case ShareWrapper.SHARERESULT_CANCEL://?뽪텋?녵벴	
					break;
				case ShareWrapper.SHARERESULT_FAIL://?녵벴鸚김뇰
					break;
				case ShareWrapper.SHARERESULT_NETWORK_ERROR://?녵벴營묊퍥?븅뵗
					break;
				case ShareWrapper.SHARERESULT_SUCCESS://?녵벴瀯볠옖?먨뒣
					break;

				default:
					break;
				}
				
			}
		});
		/**
		 * 訝븀ㅎ雅ㅷ내瀯잒?營?썞??		 */
		AnySDKSocial.getInstance().setListener(new AnySDKListener() {
			
			@Override
			public void onCallBack(int arg0, String arg1) {
				Log.d(String.valueOf(arg0), arg1);
				switch (arg0) {
				case SocialWrapper.SOCIAL_SIGNIN_FAIL://鹽얌벡?삯솁鸚김뇰
					break;
				case SocialWrapper.SOCIAL_SIGNIN_SUCCEED://鹽얌벡?삯솁?먨뒣
					break;
				case SocialWrapper.SOCIAL_SIGNOUT_FAIL://鹽얌벡?삣눣鸚김뇰
					break;
				case SocialWrapper.SOCIAL_SIGNOUT_SUCCEED://鹽얌벡?삣눣?먨뒣
					break;
				case SocialWrapper.SOCIAL_SUBMITSCORE_FAIL://?먧벡?녷빊鸚김뇰
					break;
				case SocialWrapper.SOCIAL_SUBMITSCORE_SUCCEED://?먧벡?녷빊?먨뒣
					break;
				default:
					break;
				}
				
			}
		});

		/**
		 * 訝뷸렓?곭내瀯잒?營?썞??		 */
		AnySDKPush.getInstance().setListener(new AnySDKListener() {
			
			@Override
			public void onCallBack(int arg0, String arg1) {
				Log.d(String.valueOf(arg0), arg1);
				switch (arg0) {
				case PushWrapper.ACTION_RET_RECEIVEMESSAGE://?ε룛?경렓?곫텋??					
					break;

				default:
					break;
				}
			}
		});
	}
	
	
	public void OnInit(final String appKey, final String appSecret, final String privateKey, final String oauthLoginServer,
			final String callbackGameObject, final String callbackFunc )
	{
		initAnySDK(appKey, appSecret, privateKey, oauthLoginServer);
		
		postHandler.post( new Runnable()
		{
			public void run()
			{
				try 
				{
					AnySDKPush.getInstance().startPush();
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
		
        initData();
	}
	
	public void initData()
	{
        mShareInfo = new HashMap<String, String>();
        mShareInfo.put("title","ShareSDK???訝ょ쪥也뉒쉪SDK");
        mShareInfo.put("titleUrl","http://sharesdk.cn");
        mShareInfo.put("site","ShareSDK");
        mShareInfo.put("siteUrl", "http://sharesdk.cn");
        mShareInfo.put("text", "ShareSDK?녷닇雅녺??뺛곫뵱?곩쫩孃?에?곫뼭役ゅ쒜?싥곮끍溫?쒜?싩춬鹽얌벡亮녑룿");
        mShareInfo.put("comment", "??");
        
        mArchInfo = new HashMap<String, String>();
        mArchInfo.put("rank", "friends");
        
        mTagInfo = new ArrayList<String>();
        mTagInfo.add("easy");
        mTagInfo.add("fast");
	}
	
	
	public void OnBuy(final String Product_Price, final String Product_Id, final String Product_Name, final String Server_Id, final String Product_Count, 
			final String Role_Id, final String Role_Name, final String Role_Grade, final String Role_Balance, final String Ext) 
	{
        mProductionInfo = new HashMap<String, String>();
        mProductionInfo.put("Product_Price", Product_Price);
        if(AnySDK.getInstance().getChannelId().equals("000016") || AnySDK.getInstance().getChannelId().equals("000009")|| AnySDK.getInstance().getChannelId().equals("000349")){
        	mProductionInfo.put("Product_Id", Product_Id);
		}else{
			mProductionInfo.put("Product_Id", "monthly");
		}
        mProductionInfo.put("Product_Name",Product_Name);
        mProductionInfo.put("Server_Id", Server_Id);
        mProductionInfo.put("Product_Count", Product_Count);
        mProductionInfo.put("Role_Id",Role_Id);
        mProductionInfo.put("Role_Name", Role_Name);
        mProductionInfo.put("Role_Grade", Role_Grade);
        mProductionInfo.put("Role_Balance", Role_Balance);
        mProductionInfo.put("Role_Balance", Role_Balance);
        mProductionInfo.put("EXT", Ext);
         		
		System.out.println("on buy");

		postHandler.post( new Runnable()
		{
			public void run()
			{
				try 
				{
					ArrayList<String> idArrayList =  AnySDKIAP.getInstance().getPluginId();
		    		if (idArrayList.size() == 1) {
		    			AnySDKIAP.getInstance().payForProduct(idArrayList.get(0), mProductionInfo);
					}
		    		else {
						ChoosePayMode(idArrayList);
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}	
	
	public void PurchaseResultSuccess(String orderid)
	{
		String result = "DESC:Success";
		
		result = result + "|";
				
		if (orderid != null && orderid.trim().length() != 0) 
		{
			result = result + orderid;
		}
		
		UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, result );
		System.out.println(result);
		
	}
	
	public void PurchaseResultFailed()
	{
		String result = "DESC:Failed";
		
		result = result + "|0";

		UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, result );
		System.out.println(result);
	}
	

	public static void showDialog(String title, String msg) {
        final String curMsg = msg;
        final String curTitle = title;
        
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(mAct)
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
	
 public static void Exit() {
	 mAct.finish();
	 System.exit(0);
	 
    }

	public void  ChoosePayMode(ArrayList<String> payMode) 
	{
/*		
		myLayout = new LinearLayout(mAct);
		OnClickListener onclick = new OnClickListener() { 

			@Override
			public void onClick(View v) {
				AnySDKIAP.getInstance().payForProduct((String) v.getTag(), mProductionInfo);
			}
	    };
		for (int i = 0; i < payMode.size(); i++) {
			Button button = new Button(mAct);
			String res = "Channel" + payMode.get(i);
			button.setText(getResourceId(res,"string"));
			button.setOnClickListener(onclick);
			button.setTag(payMode.get(i));
			myLayout.addView(button);
		}
			
		AlertDialog.Builder dialog02 = new AlertDialog.Builder(mAct);    
	   	dialog02.setView(myLayout); 
	   	dialog02.setTitle("UI PAY");
	   	
	    	
	   	dialog02.show();
*/	   	
	}
 
	private static int  getResourceId(String name, String type) {
	    return mAct.getResources().getIdentifier(name, type, mAct.getPackageName());
	}
	
}
