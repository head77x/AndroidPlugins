package cn.cmgame.demo;


import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import cn.cmgame.billing.api.BillingResult;
import cn.cmgame.billing.api.GameInterface;
import cn.cmgame.billing.api.GameInterface.*;
import cn.cmgame.billing.api.LoginResult;
import cn.cmgame.billing.util.ResourcesUtil;
import cn.cmgame.gamepad.api.Gamepad;
import cn.cmgame.leaderboard.api.GameLeaderboard;

import java.io.UnsupportedEncodingException;


public class BillingDemo extends ListActivity {

  String unityObject = "Main Camara";
  String runtimeScript = "OnBillingDemo";
//  public static final String CMGC_LOCAL_TEL = "local_tel";

  static final String[] BUTTONS = new String[]{
    "00.购买计费点：001",
    "01.购买计费点：002",
    "02.购买计费点：003",
    "03.购买计费点：004",
    "04.购买计费点：005",
    "05.购买计费点：006",
    "06.购买计费点：007",
    "07.购买计费点：008",
    "08.购买计费点：009",
    "09.购买计费点：010",
    "10.购买计费点：011",
    "11.购买计费点：012",
    "12.购买计费点：013",
    "13.购买计费点：014",
    "14.购买计费点：015",
    "15.初始化排行",
    "16.排行榜单",
    "17.初始化手柄",
    "18.查看手柄状态",
    "19.查看手柄电量",
    "20.查看手柄ID",
    "21.更多游戏",
    "22.音效开关",
    "23.玩家身份",
    "24.退出游戏"
  };


  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //不显示程序的标题栏
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    //不显示系统的标题栏
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    // SDK初始化接口，必须首先调用，且在主线程调用
    GameInterface.initializeApp(this);

    //*******************************网游登录流程相关接口******************************//
    // 用户登录唯一标识，每次登录，该值应不同或唯一
//    GameInterface.setExtraArguments(new String[]{"abc201311131352000"});
//
//    // 监听登录结果，游戏根据自身业务逻辑，使用移动游戏SDK提供的登录结果
//    GameInterface.setLoginListener(this, new ILoginCallback(){
//      @Override
//      public void onResult(int i, String s, Object o) {
//        System.out.println("Login.Result=" + s);
//        if(i == LoginResult.SUCCESS_EXPLICIT){
//          System.out.println("用户登录成功");
//        }
//        if(i == LoginResult.FAILED_EXPLICIT){
//          System.out.println("用户登录失败");
//        }
//        if(i == LoginResult.UNKOWN){
//          System.out.println("用户取消登录，或无网络状态，未触发登录");
//        }
//      }
//    });
    //*******************************网游登录流程相关接口******************************//

    // 支付或购买道具回调结果
    final IPayCallback payCallback = new IPayCallback() {
      @Override
      public void onResult(int resultCode, String billingIndex, Object obj) {
        String result = "";
        switch (resultCode) {
          case BillingResult.SUCCESS:
            result = "购买道具：[" + billingIndex + "] 成功！";
            break;
          case BillingResult.FAILED:
            result = "购买道具：[" + billingIndex + "] 失败！";
            break;
          default:
            result = "购买道具：[" + billingIndex + "] 取消！";
            break;
        }
        Toast.makeText(BillingDemo.this, result, Toast.LENGTH_SHORT).show();
      }
    };

    setListAdapter(new ArrayAdapter<String>(this, R.layout.game_item, BUTTONS));
    ListView lv = getListView();
    lv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (15 == position) {
          GameLeaderboard.initializeLeaderboard(BillingDemo.this, "000060069000", "/UZ3Kl4zOz7gUZnDPTDo8mt7slM=", "11214");
        } else if (16 == position) {
          GameLeaderboard.showLeaderboard(BillingDemo.this);
        } else if (17 == position) {
          Gamepad.initGamepad(BillingDemo.this);
          Gamepad.setJoyStickRadius(100, 100);
          Gamepad.setConnectionListener(new Gamepad.GamepadConnectionListener() {
            @Override
            public void onConnectionState(int i) {
              if(i==Gamepad.ConnectionState.CONNECTED){
                Toast.makeText(BillingDemo.this, "手柄自动连接成功", Toast.LENGTH_SHORT).show();
              } else {
                Toast.makeText(BillingDemo.this, "手柄自动连接失败", Toast.LENGTH_SHORT).show();
              }
            }
          });
        } else if (18 == position) {
          Toast.makeText(BillingDemo.this, "手柄是否准备好：" + Gamepad.isGamepadReady(BillingDemo.this), Toast.LENGTH_SHORT).show();
        } else if (19 == position) {
          Toast.makeText(BillingDemo.this, "手柄当前电量：" + Gamepad.getGamepadBattery(BillingDemo.this) + "%", Toast.LENGTH_SHORT).show();
        } else if (20 == position) {
          Toast.makeText(BillingDemo.this, "手柄硬件ID：" + Gamepad.getGamepadId(BillingDemo.this), Toast.LENGTH_SHORT).show();
        } else if (21 == position) {
          // 移动游戏SDK：提供的更多游戏接口
          GameInterface.retryBilling(BillingDemo.this, true, true, "001", null, payCallback);
        } else if (22 == position) {
          Toast.makeText(BillingDemo.this, "音效开关设置：" + GameInterface.isMusicEnabled(), Toast.LENGTH_SHORT).show();
        } else if (23 == position) {
          Toast.makeText(BillingDemo.this, "游戏玩家身份：" + GameInterface.getGamePlayerAuthState(), Toast.LENGTH_SHORT).show();
        } else if (24 == position) {
          GameInterface.exit(BillingDemo.this);
        } else {
          String billingIndex = getBillingIndex(position);
          boolean isRepeated = (position != 0);
          GameInterface.doBilling(BillingDemo.this, true, isRepeated, billingIndex, null, payCallback);
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

  @Override
  public void onResume() {
    super.onResume();
  }

  /**
   * 移动游戏SDK：提供的退出接口
   */
  private void exitGame() {
    GameInterface.exit(this, new GameExitCallback() {
      @Override
      public void onConfirmExit() {
        BillingDemo.this.finish();
      }

      @Override
      public void onCancelExit() {
        Toast.makeText(BillingDemo.this, "onCancelExit", Toast.LENGTH_SHORT).show();
      }
    });
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
