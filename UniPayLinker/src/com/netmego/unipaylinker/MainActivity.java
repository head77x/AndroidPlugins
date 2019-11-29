package com.netmego.unipaylinker;

import com.unity3d.player.UnityPlayerActivity;

import android.os.Bundle;
import android.widget.*;

import com.unicom.dcLoader.Utils;
import com.unicom.dcLoader.Utils.UnipayPayResultListener;


public class MainActivity extends UnityPlayerActivity 
{
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_unicom_test);

		System.out.println("start");
	}
	
	
	public void onResume() {  
        super.onResume();  
        }
	
        public void onPause() {  
        super.onPause();  
        }  
        
	public void OnInit()
	{
		System.out.println("init");
		
		Utils.getInstances().init(getApplicationContext(),"90234616120120921431100",
				"902346161", "86000504","123123","400 600 999","456456","uid", new PayResultListener());
		
	}
	
	public void OnBuy()
	{
		System.out.println("on buy");
		
		Utils.getInstances().setBaseInfo(getApplicationContext(), true, true, "http://uniview.wostore.cn/log-app/test");
		Utils.getInstances().pay(getApplicationContext(),"130201102727",
				"90234616120120921431100001","bingxin40", "10", "2014",new PayResultListener());
	}
	
	public class PayResultListener implements UnipayPayResultListener
	{

		@Override
		public void PayResult(String paycode, int flag, String error) {
			Toast.makeText(getApplicationContext(), "flag="+flag+"code="+paycode+"error="+error, Toast.LENGTH_LONG).show();
		}
		
	}
	
}


