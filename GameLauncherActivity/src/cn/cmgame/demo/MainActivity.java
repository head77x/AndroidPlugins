package cn.cmgame.demo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.chinagame.billing.GameInfoBean;
import com.chinagame.billing.GameLauncherActivity;
import com.chinagame.billing.TouchPay;
import com.chinagame.billing.TouchPay.ExitCallBack;
import com.chinagame.billing.TouchPay.PayCallBack;

/**
 * This is just a demo activity for game developer to integrate CMGC's SDK.
 *
 * @author AFWang
 */
public class MainActivity extends ListActivity {
  static final String[] BUTTONS = new String[]{
    "00.购买计费点001",
    "01.购买计费点002",
    "02.购买计费点003",
    "03.购买计费点004",
    "04.购买计费点005",
    "05.animation",
    "06.Exit",
    "07.30元计费点,允许第三方支付",
    "08.30元计费点,不允许第三方支付",
    "09.101元计费点，允许第三方支付",
    "10.101元计费点，不允许第三方支付",
  };

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final GameInfoBean infoBean = new GameInfoBean("86000692", "902346161", "90234616120130608134128735800", "测试DEMO", "联通", "400-800-900", "UID");
    TouchPay.initSdk(MainActivity.this, infoBean, true, false, new PayCallBack() {
      @Override
      public void onPayResult(int resultCode, int operator, String code, String desc) {

      }
    });

    setListAdapter(new ArrayAdapter<String>(this, R.layout.game_item, BUTTONS));
    ListView lv = getListView();
    lv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position<5) {
          String billingIndex = getBillingIndex(position);
          TouchPay.pay(MainActivity.this, "Daoju", "1", billingIndex, "130608005756", "90234616120130608134128735800001", "1234567890", new PayCallBack() {
            @Override
            public void onPayResult(int resultCode, int operator, String code, String desc) {
              Toast.makeText(MainActivity.this, "result code=" + resultCode + ", and desc=" + desc, Toast.LENGTH_SHORT).show();
            }
          });
        } else if (1 == position) {
          Intent intent = new Intent();
          intent.setClass(MainActivity.this, GameLauncherActivity.class);
          startActivity(intent);
        } else if (2 == position) {
          exitGame();
        } else if (7 == position) {
//					GameInfoBean infoBean = new GameInfoBean("86000692", "902346161","90234616120130608134128735800", "测试DEMO", "联通", "400-800-900", "UID");
			   TouchPay.initSdk(MainActivity.this, infoBean, true,false, new PayCallBack() {
						@Override
						public void onPayResult(int resultCode, int operator, String code,String desc) {

						}
					});
          TouchPay.pay(MainActivity.this, "Daoju", "30", "001", "130608005756", "90234616120130608134128735800001", "1234567890", new PayCallBack() {
            @Override
            public void onPayResult(int resultCode, int operator, String code, String desc) {
              Toast.makeText(MainActivity.this, "result code=" + resultCode + ", and desc=" + desc, Toast.LENGTH_SHORT).show();
            }
          });
        } else if (8 == position) {
//					GameInfoBean infoBean = new GameInfoBean("86000692", "902346161","90234616120130608134128735800", "测试DEMO", "联通", "400-800-900", "UID");
			    	TouchPay.initSdk(MainActivity.this, infoBean, false,false, new PayCallBack() {
						@Override
						public void onPayResult(int resultCode, int operator, String code,String desc) {

						}
					});
          TouchPay.pay(MainActivity.this, "Daoju", "30", "001", "130608005756", "90234616120130608134128735800001", "1234567890", new PayCallBack() {
            @Override
            public void onPayResult(int resultCode, int operator, String code, String desc) {
              Toast.makeText(MainActivity.this, "result code=" + resultCode + ", and desc=" + desc, Toast.LENGTH_SHORT).show();
            }
          });
        } else if (9 == position) {
//					GameInfoBean infoBean = new GameInfoBean("86000692", "902346161","90234616120130608134128735800", "测试DEMO", "联通", "400-800-900", "UID");
			    	TouchPay.initSdk(MainActivity.this, infoBean, true,false, new PayCallBack() {
						@Override
						public void onPayResult(int resultCode, int operator, String code,String desc) {

						}
					});
          TouchPay.pay(MainActivity.this, "Daoju", "101", "001", "130608005756", "90234616120130608134128735800001", "1234567890", new PayCallBack() {
            @Override
            public void onPayResult(int resultCode, int operator, String code, String desc) {
              Toast.makeText(MainActivity.this, "result code=" + resultCode + ", and desc=" + desc, Toast.LENGTH_SHORT).show();
            }
          });
        } else if (10 == position) {
//					GameInfoBean infoBean = new GameInfoBean("86000692", "902346161","90234616120130608134128735800", "测试DEMO", "联通", "400-800-900", "UID");
			    	TouchPay.initSdk(MainActivity.this, infoBean, false,false, new PayCallBack() {
						@Override
						public void onPayResult(int resultCode, int operator, String code,String desc) {

						}
					});
          TouchPay.pay(MainActivity.this, "Daoju", "101", "001", "130608005756", "90234616120130608134128735800001", "1234567890", new PayCallBack() {
            @Override
            public void onPayResult(int resultCode, int operator, String code, String desc) {
              Toast.makeText(MainActivity.this, "result code=" + resultCode + ", and desc=" + desc, Toast.LENGTH_SHORT).show();
            }
          });
        } else if (7 == position) {
        } else if (8 == position) {
        } else if (9 == position) {
        } else if (10 == position) {
        } else if (11 == position) {
        } else if (12 == position) {
        }
      }
    });
  }

  private String getBillingIndex(int i) {
    if (i < 9) {
      return "00" + (++i);
    } else {
      return "0" + (++i);
    }
  }

  /**
   * Exit game.
   */
  private void exitGame() {
    Toast.makeText(MainActivity.this, "exiting game..", Toast.LENGTH_SHORT).show();
    TouchPay.exit(this, new ExitCallBack() {

      @Override
      public void onConfirmExit() {
        MainActivity.this.finish();
      }

      @Override
      public void onCancelExit() {

      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      exitGame();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}
