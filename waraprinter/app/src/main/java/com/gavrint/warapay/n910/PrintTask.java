package com.gavrint.warapay.n910;

import android.content.Context;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.content.Intent;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.os.RemoteException;

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

import static com.gavrint.warapay.n910.MainActivity.mIzkcService;
import static com.gavrint.warapay.n910.MainActivity.PrintingFlag;

public class PrintTask implements Runnable {
    private static final String K21_DRIVER_NAME = "com.newland.me.K21Driver";
    private static final String TAG = "WARAPOS_PRINTER";

    Context context;
    Bitmap[] bitmaps;
    Printer printer;

    public PrintTask(Context context, Bitmap[] maps) {

        this.context = context;
        this.bitmaps = maps;
    }

    @Override
    public void run() {
        Log.e(TAG,"mIzkcService is " + mIzkcService);


        Log.e(TAG,"OK. Can Print It !!!");


        try {
            if ( mIzkcService.checkPrinterAvailable() == false ) {
                Log.e(TAG,"Cannot use printer");
                return;
            }

            for (int i=0;i<bitmaps.length;i++) {
                if ( bitmaps[i] != null ) {
                    mIzkcService.printBitmap(bitmaps[i]);
                    mIzkcService.generateSpace();
                    mIzkcService.generateSpace();
                    mIzkcService.generateSpace();
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            Log.d(TAG, "Connected abnormally,please check the device or reconnection..."+e1);
        }

        Log.d(TAG, "Printed image successfully!");

        PrintingFlag = true;
    }
}
