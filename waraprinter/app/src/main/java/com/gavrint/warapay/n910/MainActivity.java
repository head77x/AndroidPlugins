package com.gavrint.warapay.n910;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gavrint.warapay.n910.R;
import com.newland.me.ConnUtils;
import com.newland.me.DeviceManager;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.Device;
import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.printer.FontSettingScope;
import com.newland.mtype.module.common.printer.FontType;
import com.newland.mtype.module.common.printer.LiteralType;
import com.newland.mtype.module.common.printer.Printer;
import com.newland.mtype.module.common.printer.PrinterStatus;
import com.newland.mtype.module.common.printer.ThrowType;
import com.newland.mtype.module.common.printer.WordStockType;
import com.newland.mtypex.nseries3.NS3ConnParams;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.smartdevice.aidl.IZKCService;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WARAPOS_MAIN";

    public static boolean PrintingFlag = false;

    public static String MODULE_FLAG = "module_flag";
    public static int module_flag = 0;
    public static int DEVICE_MODEL = 0;
    private Handler mhanlder;

    public static IZKCService mIzkcService;

    public String[] urls;
    public Bitmap[] bitmaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Warapos Printer Called :");

        Intent intent = getIntent();
        String action = intent.getAction();

        if (action.equals(Intent.ACTION_VIEW)) {
            final String uri = intent.getDataString();

            Log.d(TAG, "uri : "+ uri);

            if (uri.startsWith("warapos://")) {

                String[] arr = uri.split("warapos://printer");

                if (arr.length > 1) {
                    String[] query = arr[1].split("\\?url=");

                    if (query.length > 1) {
                        urls = query[1].split(",");

                        bitmaps = new Bitmap[urls.length];

                        try {
                            for (int i=0;i<urls.length;i++) {
                                bitmaps[i] = getBitMaps(urls[i]);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            Log.d(TAG, "Connected abnormally,please check the device or reconnection..."+e1);
                        }

                        module_flag = getIntent().getIntExtra(MODULE_FLAG, 8);
                        bindService();

                        PrintingFlag = false;


/*
                        PrintTask printTask = new PrintTask(MainActivity.this, bitmaps);
                        Thread temp = new Thread(printTask);
                        temp.start();
*/

                    }
                }
            }
        }

        MainRoop();
    }

    private void MainRoop()
    {
        finish();
    }

    private Bitmap getBitMaps(String url) {

        Log.d(TAG, "Get Bitmap!");

        Bitmap bitmap = getBitmap(url);

        return bitmap;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Destroy" );

        unbindService();
        super.onDestroy();
    }

    protected void handleStateMessage(Message message)
    {
        switch (message.what){
            //服务绑定成功 service bind success
            case MessageType.BaiscMessage.SEVICE_BIND_SUCCESS:
//				Toast.makeText(this, getString(R.string.service_bind_success), Toast.LENGTH_SHORT).show();
                PrintTask printTask = new PrintTask(MainActivity.this, bitmaps);
                Thread temp = new Thread(printTask);
                temp.start();

                try
                {
                    temp.join();
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }


                break;
            //服务绑定失败 service bind fail
            case MessageType.BaiscMessage.SEVICE_BIND_FAIL:
//				Toast.makeText(this, getString(R.string.service_bind_fail), Toast.LENGTH_SHORT).show();
                break;
            //打印机连接成功 printer link success
            case MessageType.BaiscMessage.DETECT_PRINTER_SUCCESS:
//                checkPrintStateAndDisplayPrinterInfo(msg);
                break;
            //打印机连接超时 printer link timeout
            case MessageType.BaiscMessage.PRINTER_LINK_TIMEOUT:
//                Toast.makeText(this, getString(R.string.printer_link_timeout), Toast.LENGTH_SHORT).show();
                break;
        }
    }
    /** handler */
    protected Handler getHandler() {
        if (mhanlder == null) {
            mhanlder = new Handler() {
                public void handleMessage(Message msg) {
                    handleStateMessage(msg);
                }
            };
        }
        return mhanlder;
    }

    protected void sendMessage(Message message) {
        getHandler().sendMessage(message);
    }

    protected void sendMessage(int what, Object obj) {
        Message message = new Message();
        message.what = what;
        message.obj = obj;
        getHandler().sendMessage(message);
    }

    protected void sendEmptyMessage(int what) {
        getHandler().sendEmptyMessage(what);
    }

    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected");
            mIzkcService = null;
            //发送消息绑定失败 send message to notify bind fail
            sendEmptyMessage(MessageType.BaiscMessage.SEVICE_BIND_FAIL);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected");
            mIzkcService = IZKCService.Stub.asInterface(service);
            if(mIzkcService!=null){
                try {
                    //获取产品型号 get product model
                    DEVICE_MODEL = mIzkcService.getDeviceModel();
                    //设置当前模块 set current function module
                    mIzkcService.setModuleFlag(module_flag);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                //发送消息绑定成功 send message to notify bind success
                sendEmptyMessage(MessageType.BaiscMessage.SEVICE_BIND_SUCCESS);
            }
        }
    };

    public void bindService() {
        Log.e(TAG, "Start bindService");

        //com.zkc.aidl.all为远程服务的名称，不可更改
        //com.smartdevice.aidl为远程服务声明所在的包名，不可更改，
        // 对应的项目所导入的AIDL文件也应该在该包名下
        Intent intent = new Intent("com.zkc.aidl.all");
        intent.setPackage("com.smartdevice.aidl");
        bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    public void unbindService() {
        Log.e(TAG, "End Service");

        unbindService(mServiceConn);
    }

    public Bitmap bitmap_d;
    public String bitmap_name;

    private Bitmap getBitmap(String url) {
        bitmap_name = url;
        Log.d(TAG, "Load Bitmap" + url);
        Bitmap tempmap = null;
        bitmap_d = null;

        Thread mThread = new Thread() {
            @Override
            public void run() {

                try {
                    InputStream is = (InputStream) new URL(bitmap_name).getContent();

                    BitmapFactory.Options opts = new BitmapFactory.Options();

                    // Calculate inSampleSize
                    opts.inSampleSize = 1;

                    bitmap_d = BitmapFactory.decodeStream(is, null, opts);

                    is.close();

                    Log.d(TAG, "Bitmap Loaded!");
                } catch (Exception e) {
                    Log.d(TAG, "Print Bitmap Error!" + e);
                }
            }
        };

        mThread.start();

        try
        {
            mThread.join();
            tempmap = bitmap_d;
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }

        return tempmap;
    }

}

08-20 15:30:20.871 3603-3603/? I/art: Late-enabling -Xcheck:jni
        08-20 15:30:20.939 3603-3603/com.gavrint.warapay.n910 D/Proxy: setHttpRequestCheckHandler
        08-20 15:30:20.945 3603-3603/com.gavrint.warapay.n910 D/ActivityThread: BIND_APPLICATION handled : 0 / AppBindData{appInfo=ApplicationInfo{2e48c8e2 com.gavrint.warapay.n910}}
        08-20 15:30:20.945 3603-3603/com.gavrint.warapay.n910 V/ActivityThread: Handling launch of ActivityRecord{22fe9473 token=android.os.BinderProxy@3b926f30 {com.gavrint.warapay.n910/com.gavrint.warapay.n910.MainActivity}}
        08-20 15:30:20.970 3603-3603/com.gavrint.warapay.n910 V/ActivityThread: ActivityRecord{22fe9473 token=android.os.BinderProxy@3b926f30 {com.gavrint.warapay.n910/com.gavrint.warapay.n910.MainActivity}}: app=android.app.Application@162e912e, appName=com.gavrint.warapay.n910, pkg=com.gavrint.warapay.n910, comp={com.gavrint.warapay.n910/com.gavrint.warapay.n910.MainActivity}, dir=/data/app/com.gavrint.warapay.n910-2/base.apk
        08-20 15:30:20.991 3603-3603/com.gavrint.warapay.n910 W/art: Before Android 4.1, method android.graphics.PorterDuffColorFilter android.support.graphics.drawable.VectorDrawableCompat.updateTintFilter(android.graphics.PorterDuffColorFilter, android.content.res.ColorStateList, android.graphics.PorterDuff$Mode) would have incorrectly overridden the package-private method in android.graphics.drawable.Drawable
        08-20 15:30:21.011 3603-3603/com.gavrint.warapay.n910 D/WARAPOS_MAIN: Warapos Printer Called :
        uri : warapos://printer?url=https://wara-kr.miguyouxi.com/uploadfile/print/order/2018082016301164730.jpg
        08-20 15:30:21.012 3603-3603/com.gavrint.warapay.n910 D/WARAPOS_MAIN: Get Bitmap!
        Load Bitmaphttps://wara-kr.miguyouxi.com/uploadfile/print/order/2018082016301164730.jpg
        08-20 15:30:21.026 3603-3622/com.gavrint.warapay.n910 D/libc-netbsd: [getaddrinfo]: hostname=wara-kr.miguyouxi.com; servname=(null); cache_mode=(null), netid=0; mark=0
        [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
        [getaddrinfo]: hostname=wara-kr.miguyouxi.com; servname=(null); cache_mode=(null), netid=0; mark=0
        [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
        08-20 15:30:21.030 3603-3622/com.gavrint.warapay.n910 D/libc-netbsd: getaddrinfo: wara-kr.miguyouxi.com get result from proxy >>
        08-20 15:30:21.030 3603-3622/com.gavrint.warapay.n910 I/System.out: propertyValue:true
        08-20 15:30:21.031 3603-3622/com.gavrint.warapay.n910 I/System.out: [CDS]rx timeout:0
        [socket][0] connection wara-kr.miguyouxi.com/47.75.143.41:443;LocalPort=48390(0)
        08-20 15:30:21.031 3603-3622/com.gavrint.warapay.n910 I/System.out: [CDS]connect[wara-kr.miguyouxi.com/47.75.143.41:443] tm:90
        08-20 15:30:21.032 3603-3622/com.gavrint.warapay.n910 D/Posix: [Posix_connect Debug]Process com.gavrint.warapay.n910 :443
        08-20 15:30:21.140 3603-3622/com.gavrint.warapay.n910 I/System.out: [socket][/192.168.0.143:48390] connected
        08-20 15:30:21.161 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 NativeCrypto_SSL_do_handshake fd=0xa62fd7d0 shc=0xa62fd7d4 timeout_millis=0 client_mode=1 npn=0x0
        doing handshake ++
        ssl=0xb8fc0b10 info_callback where=0x10 ret=1
        ssl=0xb8fc0b10 handshake start in UNKWN  before/connect initialization
        ssl=0xb8fc0b10 info_callback calling handshakeCompleted
        ssl=0xb8fc0b10 info_callback completed
        ssl=0xb8fc0b10 info_callback where=0x1001 ret=1
        ssl=0xb8fc0b10 SSL_connect:UNKWN  before/connect initialization
        ssl=0xb8fc0b10 info_callback ignored
        08-20 15:30:21.162 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 info_callback where=0x1001 ret=1
        ssl=0xb8fc0b10 SSL_connect:23WCHA SSLv2/v3 write client hello A
        ssl=0xb8fc0b10 info_callback ignored
        ssl=0xb8fc0b10 info_callback where=0x1002 ret=-1
        ssl=0xb8fc0b10 SSL_connect:error exit in 23RSHA SSLv2/v3 read server hello A
        ssl=0xb8fc0b10 info_callback ignored
        doing handshake -- ret=-1
        ssl=0xb8fc0b10 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=0
        08-20 15:30:21.594 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: doing handshake ++
        ssl=0xb8fc0b10 info_callback where=0x1001 ret=1
        08-20 15:30:21.595 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 SSL_connect:3RSH_A SSLv3 read server hello A
        ssl=0xb8fc0b10 info_callback ignored
        ssl=0xb8fc0b10 info_callback where=0x1002 ret=-1
        ssl=0xb8fc0b10 SSL_connect:error exit in 3RSC_A SSLv3 read server certificate A
        ssl=0xb8fc0b10 info_callback ignored
        ssl=0xb8fc0b10 info_callback where=0x1002 ret=-1
        ssl=0xb8fc0b10 SSL_connect:error exit in 3RSC_A SSLv3 read server certificate A
        ssl=0xb8fc0b10 info_callback ignored
        doing handshake -- ret=-1
        ssl=0xb8fc0b10 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=0
        08-20 15:30:21.703 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: doing handshake ++
        08-20 15:30:21.706 3603-3622/com.gavrint.warapay.n910 E/NativeCrypto: ssl=0xb8fc0b10 cert_verify_callback x509_store_ctx=0xa62fd628 arg=0x0
        ssl=0xb8fc0b10 cert_verify_callback calling verifyCertificateChain authMethod=ECDHE_RSA
        08-20 15:30:21.780 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 cert_verify_callback => 1
        ssl=0xb8fc0b10 info_callback where=0x1001 ret=1
        ssl=0xb8fc0b10 SSL_connect:3RSC_A SSLv3 read server certificate A
        ssl=0xb8fc0b10 info_callback ignored
        08-20 15:30:21.782 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 info_callback where=0x1001 ret=1
        ssl=0xb8fc0b10 SSL_connect:3RSKEA SSLv3 read server key exchange A
        ssl=0xb8fc0b10 info_callback ignored
        ssl=0xb8fc0b10 info_callback where=0x1001 ret=1
        ssl=0xb8fc0b10 SSL_connect:3RSD_A SSLv3 read server done A
        ssl=0xb8fc0b10 info_callback ignored
        08-20 15:30:21.889 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 info_callback where=0x1001 ret=1
        ssl=0xb8fc0b10 SSL_connect:3WCKEA SSLv3 write client key exchange A
        ssl=0xb8fc0b10 info_callback ignored
        ssl=0xb8fc0b10 info_callback where=0x1001 ret=1
        ssl=0xb8fc0b10 SSL_connect:3WCCSA SSLv3 write change cipher spec A
        ssl=0xb8fc0b10 info_callback ignored
        ssl=0xb8fc0b10 info_callback where=0x1001 ret=1
        ssl=0xb8fc0b10 SSL_connect:3WFINA SSLv3 write finished A
        ssl=0xb8fc0b10 info_callback ignored
        ssl=0xb8fc0b10 info_callback where=0x1001 ret=1
        ssl=0xb8fc0b10 SSL_connect:3FLUSH SSLv3 flush data
        ssl=0xb8fc0b10 info_callback ignored
        ssl=0xb8fc0b10 info_callback where=0x1002 ret=-1
        ssl=0xb8fc0b10 SSL_connect:error exit in UNKWN  SSLv3 read server session ticket A
        ssl=0xb8fc0b10 info_callback ignored
        doing handshake -- ret=-1
        ssl=0xb8fc0b10 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=0
        08-20 15:30:21.999 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: doing handshake ++
        ssl=0xb8fc0b10 info_callback where=0x1001 ret=1
        ssl=0xb8fc0b10 SSL_connect:UNKWN  SSLv3 read server session ticket A
        ssl=0xb8fc0b10 info_callback ignored
        08-20 15:30:22.000 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 info_callback where=0x1001 ret=1
        ssl=0xb8fc0b10 SSL_connect:3RFINA SSLv3 read finished A
        ssl=0xb8fc0b10 info_callback ignored
        ssl=0xb8fc0b10 info_callback where=0x20 ret=1
        ssl=0xb8fc0b10 handshake done in SSLOK  SSL negotiation finished successfully
        ssl=0xb8fc0b10 info_callback calling handshakeCompleted
        ssl=0xb8fc0b10 info_callback completed
        ssl=0xb8fc0b10 info_callback where=0x1002 ret=1
        ssl=0xb8fc0b10 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
        ssl=0xb8fc0b10 info_callback ignored
        doing handshake -- ret=1
        ssl=0xb8fc0b10 NativeCrypto_SSL_get_certificate => NULL
        08-20 15:30:22.000 3603-3622/com.gavrint.warapay.n910 I/System.out: gba_cipher_suite:TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
        08-20 15:30:22.002 3603-3622/com.gavrint.warapay.n910 I/System.out: [OkHttp] sendRequest>>
        [OkHttp] sendRequest<<
        08-20 15:30:22.002 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslWrite buf=0xb8fed2b8 len=209 write_timeout_millis=0
        08-20 15:30:22.003 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fed2b8 len=2048,timeo=0
        08-20 15:30:22.225 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe2c70 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8fe2c70 len=2048,timeo=0
        08-20 15:30:22.225 3603-3622/com.gavrint.warapay.n910 D/skia: jpeg_decoder mode 1, colorType 4, w 374, h 832, sample 1, bsLength 0!!
        08-20 15:30:22.229 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8dd5e78 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8dd5e78 len=2048,timeo=0
        08-20 15:30:22.231 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8dd5e78 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8dd5e78 len=2048,timeo=0
        08-20 15:30:22.232 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8dd5e78 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8dd5e78 len=2048,timeo=0
        08-20 15:30:22.362 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.364 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.365 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.366 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.367 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.547 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.549 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.551 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.552 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.553 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.654 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.655 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.656 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.658 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.660 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.761 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.762 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.764 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.766 3603-3622/com.gavrint.warapay.n910 D/NativeCrypto: ssl=0xb8fc0b10 sslRead buf=0xb8fe8690 len=2048,timeo=0
        08-20 15:30:22.771 3603-3622/com.gavrint.warapay.n910 D/skia: jpeg_decoder finish successfully, L:1831!!!
        08-20 15:30:22.772 3603-3622/com.gavrint.warapay.n910 D/WARAPOS_MAIN: Bitmap Loaded!
        08-20 15:30:22.773 3603-3603/com.gavrint.warapay.n910 E/WARAPOS_MAIN: Start bindService
        08-20 15:30:22.792 3603-3603/com.gavrint.warapay.n910 V/ActivityThread: Performing resume of ActivityRecord{22fe9473 token=android.os.BinderProxy@3b926f30 {com.gavrint.warapay.n910/com.gavrint.warapay.n910.MainActivity}} finished=true
        Resume ActivityRecord{22fe9473 token=android.os.BinderProxy@3b926f30 {com.gavrint.warapay.n910/com.gavrint.warapay.n910.MainActivity}} started activity: false, hideForNow: false, finished: true
        Scheduling idle handler for ActivityRecord{22fe9473 token=android.os.BinderProxy@3b926f30 {com.gavrint.warapay.n910/com.gavrint.warapay.n910.MainActivity}}
        08-20 15:30:22.792 3603-3603/com.gavrint.warapay.n910 D/ActivityThread: ACT-LAUNCH_ACTIVITY handled : 0 / ActivityRecord{22fe9473 token=android.os.BinderProxy@3b926f30 {com.gavrint.warapay.n910/com.gavrint.warapay.n910.MainActivity}}
        08-20 15:30:22.834 3603-3603/com.gavrint.warapay.n910 D/ActivityThread: ACT-PAUSE_ACTIVITY_FINISHING handled : 0 / android.os.BinderProxy@3b926f30
        08-20 15:30:22.840 3603-3603/com.gavrint.warapay.n910 E/WARAPOS_MAIN: onServiceConnected
        08-20 15:30:22.859 3603-3633/com.gavrint.warapay.n910 E/WARAPOS_PRINTER: mIzkcService is com.smartdevice.aidl.IZKCService$Stub$Proxy@30f5bfbf
        OK. Can Print It !!!
        08-20 15:30:25.361 3603-3633/com.gavrint.warapay.n910 D/WARAPOS_PRINTER: Printed image successfully!
        08-20 15:30:25.362 3603-3603/com.gavrint.warapay.n910 D/WARAPOS_MAIN: Destroy
        08-20 15:30:25.362 3603-3603/com.gavrint.warapay.n910 E/WARAPOS_MAIN: End Service
        08-20 15:30:25.368 3603-3603/com.gavrint.warapay.n910 D/ActivityThread: ACT-DESTROY_ACTIVITY handled : 1 / android.os.BinderProxy@3b926f30
