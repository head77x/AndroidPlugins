package com.netmego.unityprintermanager;

import android.os.Handler;

import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.Device;
import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.CardReader;
import com.newland.mtype.module.common.cardreader.K21CardReader;
import com.newland.mtype.module.common.emv.EmvModule;
import com.newland.mtype.module.common.iccard.ICCardModule;
import com.newland.mtype.module.common.light.IndicatorLight;
import com.newland.mtype.module.common.pin.K21Pininput;
import com.newland.mtype.module.common.printer.Printer;
import com.newland.mtype.module.common.rfcard.RFCardModule;
import com.newland.mtype.module.common.scanner.BarcodeScanner;
import com.newland.mtype.module.common.scanner.BarcodeScannerManager;
import com.newland.mtype.module.common.security.SecurityModule;
import com.newland.mtype.module.common.storage.Storage;
import com.newland.mtype.module.common.swiper.K21Swiper;
import com.newland.mtypex.nseries.NSConnV100ConnParams;
import com.newland.mtypex.nseries3.NS3ConnParams;
import com.newland.me.ConnUtils;
import com.newland.me.DeviceManager;

public class N900Device extends AbstractDevice {
	private static final String K21_DRIVER_NAME = "com.newland.me.K21Driver";
	private static PrinterActivity baseActivity;
	private static N900Device n900Device=null;
	private static DeviceManager deviceManager = ConnUtils.getDeviceManager();

	private N900Device(PrinterActivity baseactivity) {
		N900Device.baseActivity = baseactivity;
	}

	public static N900Device getInstance(PrinterActivity baseactivity) {
		if (n900Device == null) {
			synchronized (N900Device.class) {
				if (n900Device == null) {
					n900Device = new N900Device(baseactivity);
				}
			}
		}
		N900Device.baseActivity = baseactivity;
		return n900Device;
	}

	@Override
	public void connectDevice() {
		baseActivity.showMessage("Device Connecting...", 0);
		try {
			deviceManager = ConnUtils.getDeviceManager();
			deviceManager.init(baseActivity, K21_DRIVER_NAME, new NS3ConnParams(), new DeviceEventListener<ConnectionCloseEvent>() {
				@Override
				public void onEvent(ConnectionCloseEvent event, Handler handler) {
					if (event.isSuccess()) {
						baseActivity.showMessage("Device is disconnected by customers!", 1);
					}
					if (event.isFailed()) {
						baseActivity.showMessage("Device is disconnected abnormally竊�", 2);
					}
				}

				@Override
				public Handler getUIHandler() {
					return null;
				}
			});
			baseActivity.showMessage("N900 device controller is initialized!", 0);
			deviceManager.connect();
			deviceManager.getDevice().setBundle(new NS3ConnParams());
			baseActivity.showMessage("Device is connected successfully!", 0);
//			baseActivity.btnStateToWaitingConn();
		} catch (Exception e1) {
			e1.printStackTrace();
			baseActivity.showMessage("Connected abnormally,please check the device or reconnection..."+e1, 2);
		}
	}

	@Override
	public void disconnect() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (deviceManager != null) {
						deviceManager.disconnect();				
						deviceManager = null;
						baseActivity.showMessage("Device is disconnectd successfully!", 0);
//						baseActivity.btnStateToWaitingInit();
					}
				} catch (Exception e) {
					baseActivity.showMessage("Device is disconnected abnormally:" + e, 0);
				}
			}
		}).start();
	}

	@Override
	public boolean isDeviceAlive() {
		boolean ifConnected = (deviceManager == null || deviceManager.getDevice()==null? false: deviceManager.getDevice().isAlive());
        return ifConnected;
	}

	@Override
	public CardReader getCardReaderModuleType() {
		K21CardReader cardReader=(K21CardReader) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_CARDREADER);
		return cardReader;
	}

	@Override
	public EmvModule getEmvModuleType() {
		EmvModule emvModule=(EmvModule) deviceManager.getDevice().getExModule("EMV_INNERLEVEL2");
		return emvModule;
	}

	@Override
	public ICCardModule getICCardModule() {
		ICCardModule iCCardModule=(ICCardModule) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_ICCARDREADER);
		return iCCardModule;
	}

	@Override
	public IndicatorLight getIndicatorLight() {
		IndicatorLight indicatorLight=(IndicatorLight) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_INDICATOR_LIGHT);
		return indicatorLight;
	}

	@Override
	public K21Pininput getK21Pininput() {
		K21Pininput k21Pininput=(K21Pininput) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_PININPUT);
		return k21Pininput;
	}

	@Override
	public Printer getPrinter() {
		Printer printer=(Printer) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_PRINTER);
		printer.init();
		return printer;
	}

	@Override
	public RFCardModule getRFCardModule() {
		RFCardModule rFCardModule=(RFCardModule) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_RFCARDREADER);
		return rFCardModule;
	}

	@Override
	public BarcodeScanner getBarcodeScanner() {
		BarcodeScannerManager barcodeScannerManager=(BarcodeScannerManager) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_BARCODESCANNER);
		BarcodeScanner scanner = barcodeScannerManager.getDefault();
		return scanner;
	}

	@Override
	public SecurityModule getSecurityModule() {
		SecurityModule securityModule=(SecurityModule) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_SECURITY);
		return securityModule;
	}

	@Override
	public Storage getStorage() {
		Storage storage=(Storage) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_STORAGE);
		return storage;
	}

	@Override
	public K21Swiper getK21Swiper() {
		K21Swiper k21Swiper=(K21Swiper) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_SWIPER);
		return k21Swiper;
	}

	@Override
	public Device getDevice() {
		return deviceManager.getDevice();
	}

}
