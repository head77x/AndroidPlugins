package billingSDK.billingDemo;

import billingSDK.billingDemo.SmsPayFactory.SmsExitListener;
import billingSDK.billingDemo.SmsPayFactory.SmsPayListener;
import android.content.Context;

public abstract class SmsPayBase
{
	
	/**
	 * @param context
	 * @param smsPayItem
	 * @param listener
	 * @param isRepeated (For 燁삣뒯歷멩닆�뷴쑑)
	 * 倻귝옖瑥ε뢿�겻폖�뭪rue,烏①ㅊ�фА溫↑뉩�꾥�兀밭궧��룾�띶쨳溫↑뉩�꾥�兀밭궧竊똕DK訝띹눎�ⓧ퓷耶섋�兀방젃恙쀤퐤竊쎽폖�뭚alse�쇾맏訝�А�㎬�兀밭쉪溫↑뉩�뱄펽SDK弱녻눎�ⓧ퓷耶섋�兀방젃恙쀤퐤竊뚨뵪�룝툔訝�А�⒴댆瑥θ�兀밭궧�띰펽訝띴폏�띶쨳瓮녑눣溫↑뉩�뚪씊
	 * 
	 */
	public abstract void pay(Context context, String smsPayItem, String paycode_3rd, String props, String Money, String AppID, SmsPayListener listener, boolean isRepeated);
	
	/**
	 * @param listener
	 * 		��눣歷멩닆竊뉶or 燁삣뒯歷멩닆�뷴쑑竊�	 */
	public abstract void exitGame(Context context, SmsExitListener listener);
	
	/**
	 * @param context
	 * 		�η쐦�닷쩀歷멩닆竊뉶or 燁삣뒯歷멩닆�뷴쑑竊�	 */
	public abstract void viewMoreGames(Context context);
	
	/**
	 * @return ��맔凉�맦�녔븞
	 */
	public abstract boolean isMusicEnabled();
}
