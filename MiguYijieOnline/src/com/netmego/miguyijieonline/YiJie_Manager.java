package com.netmego.miguyijieonline;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;

import com.netmego.miguyijieonline.SDKFactoryBase;
import com.netmego.miguyijieonline.YiJie_Manager;
import com.netmego.miguyijieonline.MiguSDKFactory.BillingListener;
import com.netmego.miguyijieonline.MiguSDKFactory.ExitGameListener;
import com.netmego.miguyijieonline.MiguSDKFactory.LoginListener;
import com.snowfish.cn.ganga.helper.SFOnlineExitListener;
import com.snowfish.cn.ganga.helper.SFOnlineHelper;
import com.snowfish.cn.ganga.helper.SFOnlineLoginListener;
import com.snowfish.cn.ganga.helper.SFOnlinePayResultListener;
import com.snowfish.cn.ganga.helper.SFOnlineUser;

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
		
		SFOnlineHelper.onCreate(context);    
			    
		System.out.println("Brandon : yijie xin Init :" + _channelID);
	}
	
	@Override public void trylogin(Context context, final LoginListener listener)
	{		
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
	        
			SFOnlineHelper.pay((Activity)context, Integer.parseInt(Money), props, 1,
					props, CP_PAY_SYNC_URL, new	SFOnlinePayResultListener() {
	    		
	    		String result;
	    		
	    		@Override
	    		public void onOderNo(String orderNo) {
//					showDialog(mycontext, "OrderNo", "Success\n" + orderNo );
    	    		System.out.println("Brandon : order no :" + orderNo);
	    			JustOrderNo = orderNo;
	    			
	    			
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
				SFOnlineHelper.exit((Activity)context, new SFOnlineExitListener() {
					/*  onSDKExit
					 *  @description��壤밪DK�쐣���눣�뼶力뺝룋�븣�씊竊뚦썮瘟껇�ε눦�빊
					 *  @param bool   �삸�맔���눣�젃恙쀤퐤  
					 */
					@Override
					public void onSDKExit(boolean bool) {
						if (bool){
							//apk���눣�눦�빊竊똡emo訝�阿잍쐣鵝욜뵪System.exit()�뼶力뺧폑鵝녻�룡낏�꼷360SDK�쉪���눣鵝욜뵪exit竊덌펹鴉싧�쇠눜歷멩닆���눣凉귛만
							listener.onExitGameConfirmExit();
						}
					}
					/*  onNoExiterProvide
					 *  @description��SDK亦→쐣���눣�뼶力뺝룋�븣�씊竊뚦썮瘟껇�ε눦�빊竊뚦룾�쑉閭ㅴ슴�뵪歷멩닆���눣�븣�씊
					 */
					@Override
					public void onNoExiterProvide() {
						AlertDialog.Builder builder = new Builder((Activity)context);
						builder.setTitle("歷멩닆�눎躍����눣�븣�씊");
						builder.setPositiveButton("���눣",
								new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								listener.onExitGameConfirmExit();
								//								System.exit(0);
							}
						});
						builder.show();
					}
				});
			
			
			
		}

		@Override
		public void viewMoreGames(Context context) 
		{
			// Do nothing...
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