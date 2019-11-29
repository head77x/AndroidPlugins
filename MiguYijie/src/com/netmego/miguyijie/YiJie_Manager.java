package com.netmego.miguyijie;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;


import com.netmego.miguyijie.MiguSDKFactory.BillingListener;
import com.netmego.miguyijie.MiguSDKFactory.ExitGameListener;
import com.netmego.miguyijie.MiguSDKFactory.LoginListener;

import com.snowfish.cn.ganga.offline.helper.SFCommonSDKInterface;
import com.snowfish.cn.ganga.offline.helper.SFGameExitListener;
import com.snowfish.cn.ganga.offline.helper.SFIPayResultListener;


public class YiJie_Manager extends SDKFactoryBase
{
	/*CP服务器地址，支付结果同步地址
	 * 如果客户端不设置，将以在易接后台创建游戏时设置的数据同步地址进行同步
	 * */
	static public final String CP_PAY_SYNC_URL = "http://testomsdk.xiaobalei.com:5555/cp/user/paylog/sync";
	

	private String _channelID;
	
	String TryItem;
	String TryMoney;
	String JustOrderNo;
		
	BillingListener MasterListener;
	
	private Handler mUIHandler = new Handler();
	
	Activity mycontext;
	
	private YiJie_Manager(Activity context, BillingListener listener) 
	{
		MasterListener = listener;
		
		mycontext = context;
		
		SFCommonSDKInterface.onInit(context);    
			    
		System.out.println("Brandon : yijie xin Init :" + _channelID);
	}
	
	@Override public void trylogin(Context context, final LoginListener listener)
	{
/*		
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
*/	    
	}
		
	private static YiJie_Manager _singletonYiJie;
	public static YiJie_Manager getInstance() 
	{
		return _singletonYiJie;
	}

	
	
	public static YiJie_Manager initSingleton(Activity context, BillingListener listener ) 
	{
			if (_singletonYiJie == null )
			{
				_singletonYiJie = new YiJie_Manager(context, listener);
			}
			return _singletonYiJie;
		}
		
		@Override
		public void pay(Context context, String smsPayItem, String ext_code, String props, String Money, BillingListener listener, boolean isRepeated) 
		{
			TryItem = smsPayItem;
			TryMoney = Money;
	
//			showDialog(mycontext, "smsPay", "try" );
	        
			SFCommonSDKInterface.pay((Activity)context, smsPayItem, new SFIPayResultListener()
			{
	    		String result;

	    		@Override
	    		public void onCanceled(String remain) {
	                result = TryItem + "|0|支付取消：错误代码："+remain;
	                
	                System.out.println("Brandon : yijie pay canceled :" + result);
	    	    		
	                MasterListener.onPurchaseFailed(TryItem, result);
	                
	                JustOrderNo = null;
	    		}

	    		@Override         
	    		public void onFailed(String remain) {   
//					showDialog(mycontext, "Failed", remain );
	    			
	                result = TryItem + "|0|支付失败：错误代码："+remain;
	                
	                System.out.println("Brandon : yijie pay failed :" + result);
	    	    		
	                MasterListener.onPurchaseFailed(TryItem, result);
	                
	                JustOrderNo = null;
	    		}

	    		@Override
	    		public void onSuccess(String remain) {
//					showDialog(mycontext, "Success", remain + "\norder :" + JustOrderNo );
	    				        		
	              result = TryItem + "|1|付款成功";
	              
	              System.out.println("Brandon : yijie pay success :" + result);
	    		
	              MasterListener.onPurchaseSucceed(TryItem, JustOrderNo);
	              
	              JustOrderNo = null;
	    		}
			});
		}

		
		
		@Override
		public void exitGame(final Context context, final ExitGameListener listener) 
		{
			// exit方法用于系统全局退出
			/*public static void exit(Activity context, SFOnlineExitListener listener)
			 *  @param context   上下文Activity 
			 *  @param listener  退出回调函数
			 */
			SFCommonSDKInterface.onExit((Activity)context, new SFGameExitListener() {
				@Override
				public void onGameExit(boolean flag) {
		              System.out.println("Brandon : yijie exit :" + flag);
					if (flag) {
						listener.onExitGameConfirmExit();
					}
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
			return SFCommonSDKInterface.isMusicEnabled(mycontext);
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

}
