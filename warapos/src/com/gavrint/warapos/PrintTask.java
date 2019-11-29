package com.gavrint.warapos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.newland.me.ConnUtils;
import com.newland.me.DeviceManager;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.printer.Printer;
import com.newland.mtype.module.common.printer.PrinterStatus;
import com.newland.mtype.module.common.printer.ThrowType;
import com.newland.mtypex.nseries3.NS3ConnParams;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class PrintTask implements Runnable {
    private static final String K21_DRIVER_NAME = "com.newland.me.K21Driver";
    private static final String TAG = "NEOSARCHIZO";


    Context context;
    String[] urls;
    DeviceManager deviceManager = ConnUtils.getDeviceManager();
    Printer printer;

    public PrintTask(Context context, String[] urls) {
        this.context = context;
        this.urls = urls;
    }

    @Override
    public void run() {
        try {

            deviceManager = ConnUtils.getDeviceManager();
            deviceManager.init(context, K21_DRIVER_NAME, new NS3ConnParams(), new DeviceEventListener<ConnectionCloseEvent>() {
                @Override
                public void onEvent(ConnectionCloseEvent event, Handler handler) {
                    if (event.isSuccess()) {
                        Log.d(TAG, "Device is disconnected by customers!");
                    }
                    if (event.isFailed()) {
                        Log.d(TAG, "Device is disconnected abnormally£¡");
                    }
                }

                @Override
                public Handler getUIHandler() {
                    return null;
                }
            });
            Log.d(TAG, "N900 device controller is initialized!");
            deviceManager.connect();
            deviceManager.getDevice().setBundle(new NS3ConnParams());
            printer=(Printer) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_PRINTER);
            printer.init();
            printer.setDensity(15);

            Log.d(TAG, "Device is connected successfully!");

            for (int i=0;i<urls.length;i++) {
                print(urls[i]);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            Log.d(TAG, "Connected abnormally,please check the device or reconnection..."+e1);
        }

        try {
            if (deviceManager != null) {
                deviceManager.disconnect();
                deviceManager = null;
                Log.d(TAG, "Device is disconnectd successfully!");
            }
        } catch (Exception e) {
            Log.d(TAG, "Device is disconnected abnormally:" + e);
        }
    }

    private void print(String url) {
        Bitmap bitmap = getBitmap(url);

        if (bitmap == null) return;

        if (printer.getStatus() != PrinterStatus.NORMAL) {
            Log.d(TAG, "Print failed£¡the status of printer is abnormal!");
        } else {
            try {
                Log.d(TAG, "Printint image...");
                printer.print(0,bitmap,30, TimeUnit.SECONDS);
                printer.paperThrow(ThrowType.BY_LINE, 2);// ñËòµ
                Log.d(TAG, "Printed image successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Printed image abnormally! the exception is " + e);
            }
        }
    }

    private static Bitmap getBitmap(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Bitmap d = BitmapFactory.decodeStream(is);
            is.close();
            return d;
        } catch (Exception e) {
            return null;
        }
    }
}