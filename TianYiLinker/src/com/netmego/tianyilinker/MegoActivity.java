package com.netmego.tianyilinker;

import com.unity3d.player.UnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;

import com.estore.lsms.tools.ApiParameter;

public class MegoActivity extends UnityPlayerActivity 
{
	String _callbackGameObject;
	String _callbackFunc;
	
	private Handler postHandler = new Handler(); 
  	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}
	
	public void OnBuy(final String ItemCode, final String ChannelID, final String ItemName, final String Money, final String orderIdx, final boolean isRepeated, final String callbackGameObject, final String callbackFunc )
	{
		_callbackGameObject = callbackGameObject;
		_callbackFunc = callbackFunc;
		
		postHandler.post( new Runnable()
		{
			public void run()
			{
				Intent intent = new Intent();
                intent.setClass(MegoActivity.this, com.estore.ui.CTEStoreSDKActivity.class);

                Bundle bundle = new Bundle();
                //ʾ������ֵ	            
                bundle.putString(ApiParameter.APPCHARGEID, ItemCode);
                bundle.putString(ApiParameter.CHANNELID, ChannelID);
                bundle.putBoolean(ApiParameter.SCREENHORIZONTAL, false);
                
                bundle.putString(ApiParameter.CHARGENAME, ItemName);//�Ʒѵ�����
                bundle.putInt(ApiParameter.PRICETYPE, (isRepeated ? 0 : 1));//�Ʒ����ͣ�0���μƷѣ�1���¼Ʒ�
                bundle.putString(ApiParameter.PRICE, Money);//�۸�             
                bundle.putString(ApiParameter.REQUESTID, orderIdx);

                intent.putExtras(bundle);
                ((Activity) MegoActivity.this).startActivityForResult(intent, 0);				
			}
		});
	}
    
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
        Bundle bdl = data.getExtras();
        int payResultCode = bdl.getInt(ApiParameter.RESULTCODE);

        if (ApiParameter.CTESTORE_SENDSUCCESS == payResultCode){
            		            //֧�����ŷ��ͳɹ�       	
        	
        }else if (ApiParameter.CTESTORE_USERCANCEL == payResultCode){
        	//�û�����ȡ��
        }else{
        	//ʧ��
        }
        
		UnityPlayer.UnitySendMessage(_callbackGameObject, _callbackFunc, requestCode + "|" + resultCode + "|" + payResultCode );
    }	
    
}


