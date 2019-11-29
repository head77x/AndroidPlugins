package com.netmego.migulogin;

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
import android.content.Context;
import android.telephony.TelephonyManager;


public class simplelogin 
{
	// 로그인 처리자
	public interface LoginListener {
		public void onLoginSuccess(String userid);
		public void onLoginFailed(String msg);
		public void onLogOut();
	};
	
	Activity mycontext;
	
	private static simplelogin _singletonsimplelogin;
	
	private boolean bLogined = false;
	private String LoginID;

	
	public static simplelogin getInstance() 
	{
		return _singletonsimplelogin;
	}
	
	public static simplelogin initSingleton(Activity context) 
	{
			if (_singletonsimplelogin == null )
			{
				_singletonsimplelogin = new simplelogin(context);
			}
			return _singletonsimplelogin;
	}
	
	private simplelogin(Activity context)
	{
		_singletonsimplelogin = this;
		
		mycontext = context;
	}
	
	private String getMyPhoneNumber(Activity act)
	{
		TelephonyManager mgr;
		
		mgr = (TelephonyManager)act.getSystemService(Context.TELEPHONY_SERVICE);
		return mgr.getLine1Number();
	}
	
	public void login(final String mi_appid, final String mi_appkey, final String agentID, final LoginListener who) throws ClientProtocolException, IOException
	{
		if ( bLogined == true )
		{
			who.onLoginSuccess(LoginID);
			return;
		}
		
		if ( who == null )
		{
			System.out.println("Brandon : LoginListener cannot be NULL");
			return;
		}
		
		final String myphone = getMyPhoneNumber(mycontext);
//		System.out.println("Brandon : myphonenumber" + myphone);
		
		Thread thread1 = new Thread(new Runnable()
	    {
		    public void run()
		    { 
	    	try {		
				HttpPost request = makeHttpPost( "http://open.miguyouxi.com/index.php?m=open&c=yisdk&a=qLogin", 
						mi_appid, mi_appkey, 
						agentID, 
						myphone);
				HttpClient client = new DefaultHttpClient() ;  
				ResponseHandler<String> reshandler = new BasicResponseHandler() ;
				String result = client.execute( request, reshandler ) ;  
				
//				System.out.println("Brandon : result get order code : " + result);
							
				JSONObject obj=new JSONObject(result);
				
				String flags = obj.getString("status");
				String code = obj.getString("code");
					
				
				if ( flags.compareTo("1") == 0 && code.compareTo("100") == 0 )
				{
					String userid = obj.getString("msg");
				
					// 여기서 실제 result의 JSON 리턴 내용 중, Status 가 Success 인 경우, 실제 아이템을 지급하면 됨.
					who.onLoginSuccess(userid);
					
					bLogined = true;
					LoginID = userid;
				}
				else	
				{
					String msg = obj.getString("msg");
					who.onLoginFailed(msg);
					bLogined = false;
					LoginID = null;
				}
	    		}
	           catch (Exception e)
	           {
					who.onLoginFailed("Cannot login because of system error :" + e);
					bLogined = false;
					LoginID = null;
	           }
		    }
	    });
		
	    thread1.start();
	}
	
	private HttpPost makeHttpPost(String url, String appid, String appkey, String agentID, String userphone) 
	{  
		String packageName = mycontext.getPackageName();
		// TODO Auto-generated method stub  
		HttpPost request = new HttpPost( url ) ;  
		Vector<NameValuePair> nameValue = new Vector<NameValuePair>() ;
		nameValue.add( new BasicNameValuePair( "packageName", packageName ) ) ; 
		nameValue.add( new BasicNameValuePair( "mi_appid", appid ) ) ; 
		nameValue.add( new BasicNameValuePair( "mi_appkey", appkey ) ) ;  
		nameValue.add( new BasicNameValuePair( "agentID", agentID ) ) ;  
		nameValue.add( new BasicNameValuePair( "mobile", userphone ) ) ;  
		
		String randstr = getMD5Hash(userphone + System.currentTimeMillis() );
		randstr = randstr.substring(0, 10);

		nameValue.add( new BasicNameValuePair( "Rand", randstr ) ) ;  

		String original = "packageName=" + packageName + "&mi_appid=" + appid + "&mi_appkey=" + appkey + "&agentID=" + agentID + "&mobile=" + userphone + "&Rand="  + randstr;
		
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
