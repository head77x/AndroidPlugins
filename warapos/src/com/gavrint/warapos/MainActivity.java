package com.gavrint.warapos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "NEOSARCHIZO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Warapos Printer");

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
                        String[] urls = query[1].split(",");

                        PrintTask printTask = new PrintTask(MainActivity.this, urls);
                        new Thread(printTask).start();
                    }
                }
            }
        }


        finish();
    }
}