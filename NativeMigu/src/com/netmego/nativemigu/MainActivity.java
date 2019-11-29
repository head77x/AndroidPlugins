package com.netmego.nativemigu;

import java.io.File;
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

import com.netmego.miguyouxinative.MiguSDKFactory;
import com.netmego.miguyouxinative.MiguSDKFactory.BillingListener;
import com.netmego.miguyouxinative.MiguSDKFactory.ExitGameListener;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	Activity thisActivity;
	Button payButton1, payButton2, payButton3, payButton4, payButton5;
	String payAlias = "";
		
	String MM_appID = "7a8f4006ffb69553a282c7dc7aae13";		// MiSDK AppID - 대쉬보드에서 확인
	String MM_appkey = "3c7827fa71860661d4ec7344bb86c7";	// MiSDK AppKey - 대쉬보드에서 확인
	String CompanyName = "杭州指老虎网络科技有限公司";
	String QAPhone = "57182877709"; 
	String AppName = "NativeSDKTest";
		
	boolean PromptExitInGame;
	
	private Handler postHandler = new Handler(); 
	
	BillingListener MainListener;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		thisActivity = this;
		payButton1 = (Button) findViewById(R.id.button1);
		payButton1.setOnClickListener(button1_OnClickListener);
		payButton2 = (Button) findViewById(R.id.button2);
		payButton2.setOnClickListener(button2_OnClickListener);
		payButton3 = (Button) findViewById(R.id.button3);
		payButton3.setOnClickListener(button3_OnClickListener);
		payButton4 = (Button) findViewById(R.id.button4);
		payButton4.setOnClickListener(button4_OnClickListener);
		payButton5 = (Button) findViewById(R.id.button5);
		payButton5.setOnClickListener(button5_OnClickListener);
		
        OnInit();
		
	}
	
	private OnClickListener button1_OnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) 
		{
			OnBuy( "001", "001", "Buy 1000 Gold", "1" ); // 금액 1 은 0.01 위안
		}
	};

	private OnClickListener button2_OnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			OnBuy( "002", "002", "Buy 2000 Gold", "2" ); // 금액 2는 0.02위안
		}
	};

	private OnClickListener button3_OnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			AboutUs(thisActivity);
			}
	};

	private OnClickListener button4_OnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			viewMoreGames();
		}
	};

	private OnClickListener button5_OnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			exitGame();
		}
	};
		 
	@Override
	protected void onStop() 
	{
		super.onStop();
		if ( MiguSDKFactory.getInstance() != null)
			MiguSDKFactory.getInstance().Stop(this);
	}
	 
	@Override
	protected void onRestart() 
	{
		// TODO Auto-generated method stub
		super.onRestart();
		if ( MiguSDKFactory.getInstance() != null)
			MiguSDKFactory.getInstance().Restart(this);
	}
	 
	@Override
	protected void onResume() 
	{
		super.onResume();

		if ( MiguSDKFactory.getInstance() != null)
			MiguSDKFactory.getInstance().Resume(this);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		if ( MiguSDKFactory.getInstance() != null)
			MiguSDKFactory.getInstance().Pause(this);
	}

	 @Override
	 protected void onDestroy() {
		 super.onDestroy();
		 if ( MiguSDKFactory.getInstance() != null)
			 MiguSDKFactory.getInstance().Destroy(this);
	 };

	 // 4.3 추가 내용
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ( MiguSDKFactory.getInstance() != null)
			MiguSDKFactory.getInstance().ActivityResult(requestCode, resultCode, data);
		else
            super.onActivityResult(requestCode, resultCode, data);
    }
	 
	
	// #2. 초기화 함수 
	public void OnInit()
	{
		System.out.println("Migu SDK init :" + AppName + ":"  + CompanyName);

		MainListener = new BillingListener()
		{
			@Override
			public void onPurchaseSucceed(String item, String code3rd) 
			{
				System.out.println("Migu Buy Success :" + item);
				
				// 3.4 버전에서 추가된 내용 - 영수 검증 단계 - 결제 성공이 오면, 반드시 아래의 CheckBillWithMigu 함수를 이용해서 결제 검증 후, 그 결과에 따라 아이템을 지급해야 함
				if ( code3rd != null )
				{
					try
					{
						CheckBillWithMigu(code3rd);
					}
					catch(Exception c)
					{
						showDialog(MainActivity.this, "showDialog", "error checkbillwithmigu");
					}
				}
				else
					showDialog(MainActivity.this, "Payment", "ITEM:" + item + "\nBuy Success" );
					
			}
			
			@Override
			public void onPurchaseInfo(String item, String msg) 
			{
				System.out.println("Migu Buy Info :" + item + ":" + msg);
			}
			
			@Override
			public void onPurchaseFailed(String item, String msg) 
			{
				System.out.println("Migu Buy Failed :" + item + ":" + msg);
				showDialog(MainActivity.this, "Payment", "ITEM:" + item + "\nBuy Failed!!!" );
			}
			
			@Override
			public void onPurchaseCanceld(String item, String msg) 
			{
				System.out.println("Migu Buy Canceled :" + item + ":" + msg);
				showDialog(MainActivity.this, "Payment", "ITEM:" + item + "\nBuy Canceled!!!" );
			}
		};
		
		postHandler.post( new Runnable()
		{
			public void run()
			{
				MiguSDKFactory.init(
						MainActivity.this, MainListener,
						MM_appID, MM_appkey,
						CompanyName, QAPhone, AppName );
			}
		});
	}
	
	// #3. 결제 신청
	public void OnBuy( final String smsPayItem, final String vaccode, final String props, final String money )
	{
		// 3.42 버전 이후 추가된 내용. 로그인이 되었는지 확인해서 로그인 진행
		if ( MiguSDKFactory.getInstance().isLogined() == false )
		{
			MiguSDKFactory.getInstance().login();
			return;
		}		
		
		System.out.println("try to buy :" + smsPayItem);
		
		postHandler.post( new Runnable()
		{
			public void run()
			{
				MiguSDKFactory.getInstance().pay(MainActivity.this, smsPayItem, vaccode, props, money, MainListener, true);
			}
		});
	}


	 	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK && MiguSDKFactory.getInstance() != null) 
	    {
	    	PromptExitInGame = false;
	    	MiguSDKFactory.getInstance().exitGame(MainActivity.this, new ExitGameListener() 
	    	{
	    		@Override
	    		public void onExitGameConfirmExit() 
	    		{
	    			System.exit(0);
	    		}
	
	    		@Override
	    		public void onExitGameCancelExit() 
	    		{
	    		}
	          
	    		@Override
	    		public void onExitGameInGame()
	    		{
	    			PromptExitInGame = true; 
	    		}
	    	});
    	
	    	if ( PromptExitInGame == true )
	    		return super.onKeyDown(keyCode, event);
    	
	    	return true;
	    }
	    
	    return super.onKeyDown(keyCode, event);
	}    
	       
  	public void exitGame()
  	{
  		if ( MiguSDKFactory.getInstance() != null )
		postHandler.post( new Runnable()
		{
			public void run()
			{
		    	MiguSDKFactory.getInstance().exitGame(MainActivity.this, new ExitGameListener() 
		    	{
		    		@Override
		    		public void onExitGameConfirmExit() 
		    		{
		    			System.exit(0);
		    		}
		
		    		@Override
		    		public void onExitGameCancelExit() 
		    		{
		    		}
		          
		    		@Override
		    		public void onExitGameInGame()
		    		{
		    		}
		    	});
			}
		});
  	}
	  	    
    public void viewMoreGames()
    {
    	if ( MiguSDKFactory.getInstance() == null )
    		return;
    	
    	MiguSDKFactory.getInstance().viewMoreGames(MainActivity.this);
    }
	
    public boolean isMusicEnabled()
    {
    	if ( MiguSDKFactory.getInstance() == null )
    		return true;
    	
    	return MiguSDKFactory.getInstance().isMusicEnabled();
    }

	public void screenShotShare(final String filepath)
	{
    	if ( MiguSDKFactory.getInstance() == null )
    		return;
    	
		System.out.println("Brandon : scr path" + filepath);
    	MiguSDKFactory.getInstance().doScreenShotShare(MainActivity.this, Uri.fromFile(new File(filepath)));
	}
    
	public void AboutUs(Context context)
	{
    	if ( MiguSDKFactory.getInstance() == null )
    		return;
    	
    	MiguSDKFactory.getInstance().AboutUs(context);
	}
	
	private Handler mUIHandler = new Handler();
	
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

	
	public void CheckBillWithMigu(final String orderID) throws ClientProtocolException, IOException
	{
		Thread thread1 = new Thread(new Runnable()
	    {
		    public void run()
		    { 
	    	try {		
				HttpPost request = makeHttpPost( "http://open.miguyouxi.com/index.php?m=open&c=yisdk&a=queryPay&", 
						MiguSDKFactory.getInstance().getAppID(), MiguSDKFactory.getInstance().getAppKey(), MiguSDKFactory.getInstance().getLoginID(), orderID );
				HttpClient client = new DefaultHttpClient() ;  
				ResponseHandler<String> reshandler = new BasicResponseHandler() ;
				String result = client.execute( request, reshandler ) ;  
				
				System.out.println("Brandon : Bill Confirm result : " + result);
				
				showDialog( MainActivity.this, "Bill Confirm result", result );
				
				// 여기서 실제 result의 JSON 리턴 내용 중, Status 가 Success 인 경우, 실제 아이템을 지급하면 됨.
				
	    		}
	           catch (Exception e)
	           {
	           }

		    }
	    });
		
	    thread1.start();
	}
	
	private HttpPost makeHttpPost(String url, String appid, String appkey, String userid, String orderID) 
	{  
		String packageName = getApplicationContext().getPackageName();
		// TODO Auto-generated method stub  
		HttpPost request = new HttpPost( url ) ;  
		Vector<NameValuePair> nameValue = new Vector<NameValuePair>() ;
		nameValue.add( new BasicNameValuePair( "packageName", packageName ) ) ; 
		nameValue.add( new BasicNameValuePair( "mi_appid", appid ) ) ; 
		nameValue.add( new BasicNameValuePair( "mi_appkey", appkey ) ) ;  
		nameValue.add( new BasicNameValuePair( "orderID", orderID ) ) ;  
		nameValue.add( new BasicNameValuePair( "userID", userid ) ) ;  
		
		String randstr = getMD5Hash(orderID + System.currentTimeMillis() );
		randstr = randstr.substring(0, 10);

		nameValue.add( new BasicNameValuePair( "Rand", randstr ) ) ;  

		String original = "packageName=" + packageName + "&mi_appid=" + appid + "&mi_appkey=" + appkey + "&orderID=" + orderID + "&userID=" + userid + "&rand=" + randstr;
		
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
