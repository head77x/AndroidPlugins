package com.netmego.unityprintermanager;

import com.newland.mtype.Device;
import com.newland.mtype.module.common.cardreader.CardReader;
import com.newland.mtype.module.common.emv.EmvModule;
import com.newland.mtype.module.common.iccard.ICCardModule;
import com.newland.mtype.module.common.light.IndicatorLight;
import com.newland.mtype.module.common.pin.K21Pininput;
import com.newland.mtype.module.common.printer.Printer;
import com.newland.mtype.module.common.rfcard.RFCardModule;
import com.newland.mtype.module.common.scanner.BarcodeScanner;
import com.newland.mtype.module.common.security.SecurityModule;
import com.newland.mtype.module.common.storage.Storage;
import com.newland.mtype.module.common.swiper.K21Swiper;

/**
 * Created by YJF on 2015/8/11 0011.
 */
public abstract class AbstractDevice {

	/**
	 *  Disconnect
	 */
	public abstract void disconnect();

	/**
	 *  Whether the device is connected.
	 *  @return
	 */
	public abstract boolean isDeviceAlive();

	/**
	 *  Connect device.
	 */
	public abstract void connectDevice();
	
	public abstract Device getDevice();
	/**
	 *  Get Card Reader Module
	 *  @return CardReader
	 */
	public abstract CardReader getCardReaderModuleType();
	/**
	 *  Get EMV Module
	 *  @return EmvModule
	 */
	public abstract EmvModule getEmvModuleType();
	/**
	 *  Get IC Card Module
	 *  @return
	 */
	public abstract ICCardModule getICCardModule();
	/**
	 *  Get Indicator Light Module
	 *  @return
	 */
	public abstract IndicatorLight getIndicatorLight();
	
	/**
	 *  Password Input Module
	 *  @return
	 */
	public abstract K21Pininput getK21Pininput();
	/**
	 *  Print Module
	 *  @return
	 */
	public abstract Printer getPrinter();
	/**
	 *  NFC Card Module
	 *  @return
	 */
	public abstract RFCardModule getRFCardModule();
	/**
	 *  Scan Module
	 *  @return
	 */
	public abstract  BarcodeScanner getBarcodeScanner();
	/**
	 *  Security Authentication Module
	 *  @return
	 */
	public abstract SecurityModule getSecurityModule();
	/**
	 *  Storage Module
	 *  @return
	 */
	public abstract Storage getStorage();
	/**
	 *  Swipe Card Module
	 *  @return
	 */
	public abstract K21Swiper getK21Swiper();
}
