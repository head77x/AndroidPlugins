package com.netmego.miguyouxinative;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;


import com.netmego.miguyouxinative.util.Inventory;
import com.netmego.miguyouxinative.util.Purchase;
import com.netmego.miguyouxinative.util.IabHelper;
import com.netmego.miguyouxinative.util.IabResult;
import com.netmego.miguyouxinative.MiguSDKFactory.BillingListener;
import com.netmego.miguyouxinative.MiguSDKFactory.ExitGameListener;
import com.netmego.miguyouxinative.MiguSDKFactory.LoginListener;
import com.snowfish.cn.ganga.helper.SFOnlineExitListener;
import com.snowfish.cn.ganga.helper.SFOnlineHelper;
import com.snowfish.cn.ganga.helper.SFOnlineLoginListener;
import com.snowfish.cn.ganga.helper.SFOnlineUser;

public class GoogleIAP_Manager extends SDKFactoryBase
{
	/*CP服务器地址，支付结果同步地址
	 * 如果客户端不设置，将以在易接后台创建游戏时设置的数据同步地址进行同步
	 * */
	static public final String CP_PAY_SYNC_URL = "http://testomsdk.xiaobalei.com:5555/cp/user/paylog/sync";
	
    static final int RC_REQUEST = 28532;

	private String _channelID;
	
	boolean _LogToMego = false;
	
	String TryItem;
	String TryMoney;
	String JustOrderNo;
	String PayLoadString;
		
	BillingListener MasterListener;
	
	private Handler mUIHandler = new Handler();
	
	Activity mycontext;
	
    // The helper object
    IabHelper mHelper;
	
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
    		System.out.println("Brandon : google pay purchase finished: " + result + ", purchase : " + purchase );
			LogToMegoServer("Brandon : google pay purchase finished: " + result + ", purchase : " + purchase);

//	        showDialog( mycontext, "결제 종료", "구글 플레이 결제 완료" + result );
    		
            String ret;
            
            // if we were disposed of in the meantime, quit.
            if (mHelper == null || result.isFailure() || purchase == null)
            {
                ret = TryItem + "|0|支付失败：错误代码：Error purchasing Google";
                
	    		System.out.println("Brandon : google pay failed :" + ret);
				LogToMegoServer("Brandon : google pay failed :" + ret);
    	    		
                MasterListener.onPurchaseFailed(TryItem, ret);
                
                JustOrderNo = null;
                return;
            }
            
    		System.out.println("Brandon : google pay success, try to mGotInventoryListener");
			LogToMegoServer("Brandon : google pay success, try to mGotInventoryListener");

			mHelper.queryInventoryAsync(mGotInventoryListener);

//            mHelper.consumeAsync(purchase, mConsumeFinishedListener);
        }
    };
    
	// Listener that's called when we finish querying the items we own
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {	
			Purchase purchase = null;
			String ret = "";
			if(inventory != null) {
					if ( TryItem.compareTo("000") == 0 )	// 최초 단계 처리중이라면..
					{
						LogToMegoServer("Brandon : google pay first checked inventory");
						for ( int i = 1; i < 30; i++ )	// 최초에 이전에 아이템 지급한 것이 처리 않되었다면, 일단 모두 찾아서 초기화해버림
						{
							String temp = String.format("%03d", i);
							if (inventory.hasPurchase(temp))
							{
								purchase = inventory.getPurchase(temp);
								mHelper.consumeAsync(purchase, mConsumeFinishedListener);
							}
						}
						LogToMegoServer("Brandon : google pay first checked clened");
						return;
					}
					else
					if (inventory.hasPurchase(TryItem)) {	// 정상 결제 처리중이라면...
					//	mSkuDetails = inventory.getSkuDetails(PRODUCT_ID[i]);
						purchase = inventory.getPurchase(TryItem);		
						
			    		System.out.println("Brandon : google pay success, checked inventory");
						LogToMegoServer("Brandon : google pay success, checked inventory : " + purchase);
					}
					else
					{
						LogToMegoServer("Brandon : google pay failed, inventory has no purchase item :" + TryItem);						
					}
				}
			else
			{
				LogToMegoServer("Brandon : google pay failed, inventory is null");						
			
			}
						
			// 구매 아이템 확인 성공
			if(result.isSuccess()) {
				// 소비 메시지 보내기
				if(purchase != null) 
					{
		    			System.out.println("Brandon : google pay success, try to consume");
						LogToMegoServer("Brandon : google pay result success, try to consume : " + purchase);

						mHelper.consumeAsync(purchase, mConsumeFinishedListener);
					}
				else 
				{
					LogToMegoServer("Brandon : google pay result success, but purchase is null");						
					
				}
			} else {
				// 구매 실패/취소
                ret = TryItem + "|0|支付失败：错误代码：Error purchasing Google in mGotInventoryListener";
                
	    		System.out.println("Brandon : google pay mGotInventoryListener failed :" + ret);
				LogToMegoServer("Brandon : google pay mGotInventoryListener failed : " + ret);
  	    		
                MasterListener.onPurchaseFailed(TryItem, ret);
                
                JustOrderNo = null;
                return;
			}			
		}
	};
    

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
        	String ret;
        	
    		System.out.println("Brandon : Consumption finished. Purchase: " + purchase + ", result: " + result);
			LogToMegoServer("Brandon : Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null||result.isFailure()) 
            {
                ret = TryItem + "|0|支付失败：错误代码：Error consume Google";
                
	    		System.out.println("Brandon : google pay consume failed :" + ret);
				LogToMegoServer("Brandon : mHelper, or failed: " + mHelper );
    	    		
                MasterListener.onPurchaseFailed(TryItem, ret);
                
                JustOrderNo = null;
            	return;
            }
            
            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) 
            {
              ret = TryItem + "|1|付款成功";
              
              System.out.println("Brandon : google pay success :" + ret);
				LogToMegoServer("Brandon : google pay success :" + ret);
    		
              MasterListener.onPurchaseSucceed(TryItem, JustOrderNo);
              
              JustOrderNo = null;
            }
            else
            {
                ret = TryItem + "|0|支付失败：错误代码：Error consume Google";
                
  	    		System.out.println("Brandon : google pay consume result failed :" + ret);
				LogToMegoServer("Brandon : google pay consume result failed :" + ret);
      	    		
                  MasterListener.onPurchaseFailed(TryItem, ret);
                  
                  JustOrderNo = null;
                  
              	return;    
            }
        }
    };
    
       
	private GoogleIAP_Manager(Activity context, BillingListener listener, String googlepublickey, boolean weblogging) 
	{
		_LogToMego = weblogging;
		
		MasterListener = listener;
		
		mycontext = context;
		
	    SFOnlineHelper.onCreate(context);	    
	    
	    if ( googlepublickey == null )
			System.out.println("Brandon : google Init failed");
	    else
	    {
	        mHelper = new IabHelper((Context)context, googlepublickey);

	        if ( mHelper == null )
	        {
                // Oh noes, there was a problem.
    			System.out.println("Brandon : google Setup failed : mhelper cannot make");
                return;
	        }
	        
	        // enable debug logging (for a production application, you should set this to false).
	        mHelper.enableDebugLogging(false);

	        // Start setup. This is asynchronous and the specified listener
	        // will be called once setup completes.
	        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
	            public void onIabSetupFinished(IabResult result) {

	                if (!result.isSuccess()) {
	                    // Oh noes, there was a problem.
	        			System.out.println("Brandon : google Setup failed : " + result);
	        			LogToMegoServer("Brandon : google Setup failed : " + result);
	                    return;
	                }

	                // Have we been disposed of in the meantime? If so, quit.
	                if (mHelper == null) return;

	                // IAB is fully set up. Now, let's get an inventory of stuff we own.
        			System.out.println("Brandon : google Setup success");
        			LogToMegoServer("Brandon : google Setup success");
    
        			TryItem = "000";
        			// 인벤토리 지원 않함
	                mHelper.queryInventoryAsync(mGotInventoryListener);
	            }
	        });
    	
	    	
	    	System.out.println("Brandon : google iap Init :" + googlepublickey);
			LogToMegoServer("Brandon : google iap Init :" + googlepublickey);
	    }
	}
	
	@Override public void trylogin(Context context, final LoginListener listener)
	{
		// Do nothing
		SFOnlineHelper.setLoginListener((Activity)context, new SFOnlineLoginListener() 
		{    
			@Override    public void onLoginSuccess(SFOnlineUser user, Object customParams) 
			{     
				listener.onLoginSuccess(user.getChannelUserId(),user.getUserName(),user.getToken());
			}   
			
			@Override    public void onLoginFailed(String reason, Object customParams) 
			{     
				listener.onLoginFailed(reason + "|" + customParams);
			}
			
			@Override    public void onLogout(Object customParams) 
			{                //登出回调    
				listener.onLogOut(customParams);
			}
		});
	    
	    SFOnlineHelper.login((Activity)context, "Login"); 
		
	}
		
	private static GoogleIAP_Manager _singletonYiJie;
	public static GoogleIAP_Manager getInstance() 
	{
		return _singletonYiJie;
	}

	public void IAPDestroy()
	{
	   if (mHelper != null) mHelper.dispose();
	   mHelper = null;		
	}
	
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{
        if (mHelper == null) 
        	{
			LogToMegoServer("Brandon : ActivityResult mhelper null");
			return;
        	}

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
//            super.onActivityResult(requestCode, resultCode, data);
        }
	}
	
	public static GoogleIAP_Manager initSingleton(Activity context, BillingListener listener, String googlepublickey, boolean logweb) 
	{
			if (_singletonYiJie == null )
			{
				_singletonYiJie = new GoogleIAP_Manager(context, listener, googlepublickey, logweb);
			}
			return _singletonYiJie;
		}
		
		@Override
		public void pay(Context context, String smsPayItem, String ext_code, String props, String Money, BillingListener listener, boolean isRepeated) 
		{
			TryItem = smsPayItem;
			TryMoney = Money;
			
			if ( !MiguSDKFactory.getInstance().isLogined() )
			{
				String ret = TryItem + "|0|支付失败：错误代码：need to login";
				
	    		System.out.println("Brandon : google pay failed :" + ret);
				LogToMegoServer("Brandon : google pay failed :" + ret);
   	    		
                MasterListener.onPurchaseFailed(TryItem, ret);
                
                JustOrderNo = null;
                
                return;
			}
	
			try
			{
				GetOrderCodeWithMigu(smsPayItem, Money);
			}
			catch(Exception c)
			{
				String ret = TryItem + "|0|支付失败：错误代码：Cannot get order code";
				
	    		System.out.println("Brandon : google pay failed :" + ret);
				LogToMegoServer("Brandon : google pay failed :" + ret);
   	    		
                MasterListener.onPurchaseFailed(TryItem, ret);
                
                JustOrderNo = null;
                
                return;
			}
		}
		
		public void RealPayment(String ordercode)
		{
			
	        /* TODO: for security, generate your payload here for verification. See the comments on
	         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
	         *        an empty string, but on a production app you should carefully generate this. */
			PayLoadString = ordercode + System.currentTimeMillis();
	        String payload = getMD5Hash( PayLoadString );
	        
	        JustOrderNo = ordercode;
	        
//	        showDialog( mycontext, "결제 시작", "구글 플레이 결제를 시작합니다." );
			LogToMegoServer("Brandon : Start launchpurchaseflow" );

	        mHelper.launchPurchaseFlow(mycontext, TryItem, RC_REQUEST,
	                mPurchaseFinishedListener);
		}			
		
		/** Verifies the developer payload of a purchase. */
	    boolean verifyDeveloperPayload(Purchase p) {
	        String payload = p.getDeveloperPayload();

	        if ( payload.compareTo( getMD5Hash(PayLoadString) ) == 0 )
	        	return true;
	        
	        return false;
	    }
		
		
		@Override
		public void exitGame(final Context context, final ExitGameListener listener) 
		{
			// exit方法用于系统全局退出
			/*public static void exit(Activity context, SFOnlineExitListener listener)
			 *  @param context   上下文Activity 
			 *  @param listener  退出回调函数
			 */
			SFOnlineHelper.exit((Activity)context, new SFOnlineExitListener() {
				/*  onSDKExit
				 *  @description　当SDK有退出方法及界面，回调该函数
				 *  @param bool   是否退出标志位  
				 */
				@Override
				public void onSDKExit(boolean bool) {
		              System.out.println("Brandon : yijie exit :" + bool);
					
					
					if (bool){
						//apk退出函数，demo中也有使用System.exit()方法；但请注意360SDK的退出使用exit（）会导致游戏退出异常
						listener.onExitGameConfirmExit();
					}
				}
				/*  onNoExiterProvide
				 *  @description　SDK没有退出方法及界面，回调该函数，可在此使用游戏退出界面
				 */
				@Override
				public void onNoExiterProvide() 
				{
			        mUIHandler.post(new Runnable() {
			            @Override
			            public void run() {
			                new AlertDialog.Builder(context)
			                .setTitle("游戏退出")
			                .setMessage("现在游戏退出吗？")
			                .setPositiveButton("不", 
			                        new DialogInterface.OnClickListener() {
			                            
			                            @Override
			                            public void onClick(DialogInterface dialog, int which) 
			                            {
			                        		listener.onExitGameCancelExit();
			                            }
			                        })
			                .setNegativeButton("是", 
			                        new DialogInterface.OnClickListener() {
			                            
			                            @Override
			                            public void onClick(DialogInterface dialog, int which) 
			                            {
			                        		listener.onExitGameConfirmExit();
			                            }
			                        }).create().show();
			            }
			        });
				}
			});
		}

		@Override
		public void viewMoreGames(Context context) 
		{
			// Do nothing...
		}

		@Override
		public boolean isMusicEnabled() {
			return SFOnlineHelper.isMusicEnabled(mycontext);
		}
		
		@Override
		public void doScreenShotShare (Context context, Uri uri) {
			// Do nothing...
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
		
		public void LogToMegoServer(final String logs)
		{
			if ( !_LogToMego )
				return;
			
			Thread thread1 = new Thread(new Runnable()
		    {
			    public void run()
			    { 
		    	try {		
					HttpPost request = makeLogPost( "http://www.netmego.com/loging/index.php?", logs );
					HttpClient client = new DefaultHttpClient() ;  
					ResponseHandler<String> reshandler = new BasicResponseHandler() ;
					String result = client.execute( request, reshandler ) ;  
		    		}
		           catch (Exception e)
		           {
		           }

			    }
		    });
			
		    thread1.start();
			
		}
		
		private HttpPost makeLogPost(String url, String log) 
		{  
			// TODO Auto-generated method stub  
			HttpPost request = new HttpPost( url ) ;  
			Vector<NameValuePair> nameValue = new Vector<NameValuePair>() ;
			nameValue.add( new BasicNameValuePair( "log", log ) ) ; 

			String original = "log=" + log;
			
	        String key = getMD5Hash( original + "D8936149A201D1B0");
	        
			nameValue.add( new BasicNameValuePair( "sign", key ) ) ;  
			request.setEntity( makeEntity(nameValue) ) ;  
			return request ;  
		}  		

		public void GetOrderCodeWithMigu(final String ItemIdx, final String Money) throws ClientProtocolException, IOException
		{
			Thread thread1 = new Thread(new Runnable()
		    {
			    public void run()
			    { 
		    	try {		
					System.out.println("Brandon : try to get order code");
					HttpPost request = makeHttpPost( "http://open.miguyouxi.com/index.php?m=open&c=yisdk&a=googlePay", 
							MiguSDKFactory.getInstance().getAppID(), MiguSDKFactory.getInstance().getAppKey(), 
							MiguSDKFactory.getInstance().getLoginID(), 
							MiguSDKFactory.getInstance().getAgentID(), TryItem, TryMoney);
					HttpClient client = new DefaultHttpClient() ;  
					ResponseHandler<String> reshandler = new BasicResponseHandler() ;
					String result = client.execute( request, reshandler ) ;  
					
					System.out.println("Brandon : result get order code : " + result);
					LogToMegoServer("Brandon : result get order code : " + result);
								
					JSONObject obj=new JSONObject(result);
					
					String flags = obj.getString("status");
					
					if ( flags.compareTo( "success") == 0 )
					{
						String ordercode = obj.getString("orderID");
					
						// 여기서 실제 result의 JSON 리턴 내용 중, Status 가 Success 인 경우, 실제 아이템을 지급하면 됨.
						RealPayment(ordercode);
						
			    		System.out.println("Brandon : google pay orderid :" + ordercode);
						LogToMegoServer("Brandon : google pay orderid :" + ordercode);

					}
					else	
					{
						String ret = TryItem + "|0|支付失败：错误代码：Cannot get order code";
						
			    		System.out.println("Brandon : google pay failed :" + ret);
						LogToMegoServer("Brandon : google pay failed :" + ret);
		    	    		
		                MasterListener.onPurchaseFailed(TryItem, ret);
		                
		                JustOrderNo = null;
					}
		    		}
		           catch (Exception e)
		           {
		           }

			    }
		    });
			
		    thread1.start();
		}
		
		private HttpPost makeHttpPost(String url, String appid, String appkey, String userid, String agentID, String itemidx, String money) 
		{  
			userid = "0";
			
			String packageName = mycontext.getPackageName();
			// TODO Auto-generated method stub  
			HttpPost request = new HttpPost( url ) ;  
			Vector<NameValuePair> nameValue = new Vector<NameValuePair>() ;
			nameValue.add( new BasicNameValuePair( "packageName", packageName ) ) ; 
			nameValue.add( new BasicNameValuePair( "mi_appid", appid ) ) ; 
			nameValue.add( new BasicNameValuePair( "mi_appkey", appkey ) ) ;  
			nameValue.add( new BasicNameValuePair( "agentID", agentID ) ) ;  
			nameValue.add( new BasicNameValuePair( "userID", userid ) ) ;  
			nameValue.add( new BasicNameValuePair( "money", money ) ) ;  
			nameValue.add( new BasicNameValuePair( "pid", itemidx ) ) ;  
			
			String randstr = getMD5Hash(userid + System.currentTimeMillis() );
			randstr = randstr.substring(0, 10);

			nameValue.add( new BasicNameValuePair( "Rand", randstr ) ) ;  

			String original = "packageName=" + packageName + "&mi_appid=" + appid + "&mi_appkey=" + appkey + "&userID=" + userid + "&money=" + money + "&pid=" + itemidx + "&agentID=" + agentID + "&Rand="  + randstr;
			
	        String key = getMD5Hash( original + "D8936149A201D1B0");
	        
			nameValue.add( new BasicNameValuePair( "sign", key ) ) ;  
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
