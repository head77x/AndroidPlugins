package com.netmego.testmigulogin;

import com.netmego.migulogin.simplelogin;
import com.netmego.migulogin.simplelogin.LoginListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {
	Button LoginButton;
	LoginListener MyLoginListener;
	private Handler postHandler = new Handler(); 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        LoginButton = (Button) findViewById(R.id.button1);
        LoginButton.setOnClickListener(button_OnClickListener);

		simplelogin.initSingleton(this);
        
		MyLoginListener = new LoginListener() 
		{
			public void onLoginSuccess(String userid)
			{
				System.out.println("Migu Login Success :" + userid);
				
			}
			
			public void onLoginFailed(String msg)
			{
				System.out.println("Migu Login failed :" + msg);
				
			}
			
			public void onLogOut()
			{
				System.out.println("Migu Logout");
	
			}
		};
		
		
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	private OnClickListener button_OnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) 
		{
			postHandler.post( new Runnable()
			{
				public void run()
				{
					try
					{
						simplelogin.getInstance().login("7a8f4006ffb69553a282c7dc7aae13", "3c7827fa71860661d4ec7344bb86c7", "0", MyLoginListener);
					}
					catch( Exception e)
					{
						Toast temp = Toast.makeText(MainActivity.this, "Exception : " + e, 10);
						temp.show();
					}
				}
			});
		}
	};
}
