package com.haloai.hud.androidendpoint.connmanager;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.haloai.hud.androidendpoint.util.HaloAndroidLogger;
import com.haloai.hud.androidendpoint.util.Phone2HudMessagesUtils;
import com.haloai.hud.lib.transportlayer.bluetooth.PhoneEndpointTransportBTSpp;
import com.haloai.hud.lib.transportlayer.bluetooth_ble.PhoneEndpointTransportBTBle;
import com.haloai.hud.lib.transportlayer.exceptions.HudTransportException;
import com.haloai.hud.lib.transportlayer.interfaces.EndpointDeviceInfo;
import com.haloai.hud.lib.transportlayer.interfaces.IPhoneEndpointTransportInterface;
import com.haloai.hud.lib.transportlayer.interfaces.IPhoneEndpointTransportNotification;
import com.haloai.hud.lib.transportlayer.wifi.PhoneEndpointTransportWifi;
import com.haloai.hud.model.v2.Hud2PhoneMessages;
import com.haloai.hud.model.v2.Phone2HudMessages;
import com.haloai.hud.proto.v2.Hud2PhoneMessagesProtoDef.Hud2PhoneMessagesProto;
import com.haloai.hud.utils.IHaloLogger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wangshengxing on 16/4/19.
 */
public class HudConnectionManager implements IPhoneEndpointTransportInterface, IPhoneEndpointTransportNotification {


    public interface IDataDispatcher {
        public void dispatchH2PData(Hud2PhoneMessages hud2PhoneMessages);
    }

    public interface ConnecttionListener {
        public void ConnectResult(boolean result);
    }

    public enum HudConnectionStatus {
        HUD_CONNECTION_STATUS_CONNECTED,
        HUD_CONNECTION_STATUS_CONNECTING,
        HUD_CONNECTION_STATUS_CONNECT_FAILED,
        HUD_CONNECTION_STATUS_CONNECTION_LOST,
        HUD_CONNECTION_STATUS_DISCONNECTED
    }

    public enum HudConnectionStrategy {
        HUD_CONNECTION_WIFI_FIRST,
        HUD_CONNECTION_WIFI_ONLY,
        HUD_CONNECTION_BTSPP_FIRST,
        HUD_CONNECTION_BTSPP_ONLY,
    }

    public enum HudConnectionType {
        TRANSPORT_WIFI,
        TRANSPORT_BTSPP,
        TRANSPORT_BTBLE
    }

    private enum HudConnectingTraceStatus {
        HUDC_STATUS_NONE,
        HUDC_STATUS_CONNECTING_RECENT_DEVICE,
        HUDC_STATUS_CONNECTING_PAIRED_DEVICE,
        HUDC_STATUS_SEARCHING,
        HUDC_STATUS_CONNECTING_SEARCHED_DEVICE
    }

    private final static String TAG = HudConnectionManager.class.getName();

    private static final HudConnectionType[] HUD_CONNECTION_WIFI_FIRST = {HudConnectionType.TRANSPORT_WIFI, HudConnectionType.TRANSPORT_BTBLE, HudConnectionType.TRANSPORT_BTBLE};
    private static final HudConnectionType[] HUD_CONNECTION_BLE_FIRST = {HudConnectionType.TRANSPORT_BTBLE, HudConnectionType.TRANSPORT_WIFI, HudConnectionType.TRANSPORT_BTBLE};

    private static final String HARDWARE_BT_NAME = "HALO HUD";

    private Context mAppContext;

    private Map<HudConnectionType, IPhoneEndpointTransportInterface> mTransportInstanceMap;
    private List<HudConnectionType> mCurentTransportTypeList;
    private IPhoneEndpointTransportInterface mTransport;
    private HudConnectionStrategy mHudConnectionStrategy = HudConnectionStrategy.HUD_CONNECTION_WIFI_FIRST;
    private HudConnectionType mCurrentHudConnectionType = HudConnectionType.TRANSPORT_WIFI;


    private List<EndpointDeviceInfo> mHaloaiHudDevicesList = new ArrayList<EndpointDeviceInfo>();
    private EndpointDeviceInfo mCurrentOperatingDevice; //当前被操作的HUD设备，包括正在连接中的、断开的。

    private List<IDataDispatcher> dataDispatcherList = new ArrayList<IDataDispatcher>();
    private List<ConnecttionListener> mConnecttionListenerList = new ArrayList<ConnecttionListener>();

    private String mLastPhoneRequestIdStr;

    //重连
    private Timer mReconnectTimer = new Timer();
    private TimerTask mReconnectTask;
    private int mReconnectCnt;


    private IHaloLogger mLogger = HaloAndroidLogger.logger;

    HudConnectingTraceStatus mHudConnectionStatus = HudConnectingTraceStatus.HUDC_STATUS_NONE;


    //****Singleton variables and methods
    private static HudConnectionManager theInstance;

    //Not allow to construct this class explicitly.
    private HudConnectionManager(Context appContext) {
        mAppContext = appContext;
        initTransportInstance();

    }

    ;

    private void initTransportInstance() {
        IPhoneEndpointTransportInterface transport;
        mTransportInstanceMap = new HashMap<HudConnectionType, IPhoneEndpointTransportInterface>();
        transport = new PhoneEndpointTransportWifi(mAppContext);
        transport.setLogger(mLogger);
        mTransportInstanceMap.put(HudConnectionType.TRANSPORT_WIFI, transport);
//        transport = new PhoneEndpointTransportBTSpp(mAppContext);
//        transport.setLogger(mLogger);
//        mTransportInstanceMap.put(HudConnectionType.TRANSPORT_BTSPP, transport);
//        transport = new PhoneEndpointTransportBTBle(mAppContext);
//        transport.setLogger(mLogger);
//        mTransportInstanceMap.put(HudConnectionType.TRANSPORT_BTBLE, transport);

        setCurrentHudConnectionType(HudConnectionType.TRANSPORT_WIFI);
    }

    //A single point to create the instance
    public static HudConnectionManager getInstance(Context appContext) {
        if (theInstance == null) {
            theInstance = new HudConnectionManager(appContext);
        }
        return theInstance;
    }

    /*
        当上一个连接方式确认无法连接时，重新开始另一种方式
     */
    private boolean getNextTransport() {
        boolean hasNext = false;
        int index = mCurentTransportTypeList.indexOf(mCurrentHudConnectionType);
        if (index == mCurentTransportTypeList.size() || index == -1) {
            hasNext = false;
            mCurrentHudConnectionType = mCurentTransportTypeList.get(0);
        } else {
            hasNext = true;
            mCurrentHudConnectionType = mCurentTransportTypeList.get(index + 1);
        }
        mTransport = mTransportInstanceMap.get(mCurrentHudConnectionType);
        return hasNext;
    }


    /*
    * 设置创建连接的策略优先级，如默认WIFI优先
    * */
    public void setConnectStrategy(HudConnectionStrategy strategy) {
        mHudConnectionStrategy = strategy;
        //TODO
        switch (strategy) {
            case HUD_CONNECTION_WIFI_FIRST:
                mCurentTransportTypeList = Arrays.asList(HUD_CONNECTION_WIFI_FIRST);
                break;
            case HUD_CONNECTION_BTSPP_FIRST:
                mCurentTransportTypeList = Arrays.asList(HUD_CONNECTION_BLE_FIRST);
                break;
            default:
                mCurentTransportTypeList = Arrays.asList(HUD_CONNECTION_WIFI_FIRST);
                break;
        }
        mCurrentHudConnectionType = mCurentTransportTypeList.get(0);
    }

    public HudConnectionType getCurrentHudConnectionType() {
        return mCurrentHudConnectionType;
    }

    /*
    * 根据传输的数据环境类型大小，设置当前传输的类型
    * */
    public void setCurrentHudConnectionType(HudConnectionType currentHudConnectionType) {
        this.mCurrentHudConnectionType = currentHudConnectionType;
        //TODO
        mTransport = mTransportInstanceMap.get(mCurrentHudConnectionType);
    }

    public void connectServer() {
        mTransport.connect(this, new EndpointDeviceInfo());//wifi 传输时，hudDeviceInfo可为null
        //hardwareConnect();
//        startReconnectTask(1);
    }

    //Return the data sender instance.
    public static IPhoneEndpointTransportInterface getDataSender() {
        return theInstance;
    }

    public void searchDevice() {
        mTransport.searchForHudDevices(this);
    }


    public String getLastPhoneRequestIdStr() {
        return mLastPhoneRequestIdStr;
    }

    public void addConnecttionListener(ConnecttionListener connecttionListener) {

        if(-1 == mConnecttionListenerList.indexOf(connecttionListener)){
            mConnecttionListenerList.add(connecttionListener);
        }
    }
    public void removeConnecttionListener(ConnecttionListener connecttionListener) {

        mConnecttionListenerList.remove(connecttionListener);
    }

    public void addDataDispatcher(IDataDispatcher dataDispatcher) {
        //不允许添加同类型的Dispatcher
        for (IDataDispatcher dispatcher : dataDispatcherList) {
            if (dataDispatcher.getClass().getName().equalsIgnoreCase(dispatcher.getClass().getName())) {
//                throw new IllegalArgumentException("Duplicated Dispatcher instance: " + dispatcher.getClass().getName());
            }
        }
        dataDispatcherList.add(dataDispatcher);
    }

    public void removeDataDispatcher(IDataDispatcher dataDispatcher) {
        dataDispatcherList.remove(dataDispatcher);
    }

    public void send(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        send(buffer);
    }

    private void hardwareConnect() {
        //TODO 处理之前已经连接上的情况
        if (this.mTransport.isConnected()) {
            LOGI("[hardwareConnect] It's already connected.");
            notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECTED);
            return;
        }
        if (this.mTransport.isSearching()) {
            LOGI("[CONNECTIONConnect] It's searching.");
            notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECTING);
            return;
        }

        //To connect the recent device.
        if (tryToConnectRecentHud())
            return;

        //To connect paired haloai HUD devices.
        if (tryToConnectPairedDevice())
            return;

        //To searching haloai HUD devices.biceps
        tryToSearchHudDevices();
    }

    private void hardwareDisconnect() {
        mTransport.disconnect();
        mCurrentOperatingDevice = null;
    }

    private boolean tryToConnectRecentHud() {
        mCurrentOperatingDevice = mTransport.getRecentDevice();
        if (mCurrentOperatingDevice != null) {
            LOGI("[tryToConnectRecentHud]To connect recent device:" + mCurrentOperatingDevice.getUniqueAddress());
            mHudConnectionStatus = HudConnectingTraceStatus.HUDC_STATUS_CONNECTING_RECENT_DEVICE;
            notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECTING);
            mTransport.connect(this, mCurrentOperatingDevice);
            return true;
        }
        LOGI("[tryToConnectRecentHud] no recent device.");

        return false;
    }

    private boolean tryToConnectPairedDevice() {
        mHaloaiHudDevicesList.clear();
        List<EndpointDeviceInfo> pairedDevices = mTransport.retrievePairedDevices();
        for (int i = 0; i < pairedDevices.size(); i++) {
            EndpointDeviceInfo hudDeviceInfo = pairedDevices.get(i);
            LOGI("Paired bluetooth device " + i + hudDeviceInfo.getName());
//			if (hudDeviceInfo.getName().equalsIgnoreCase(HARDWARE_BT_NAME)) {
            if (hudDeviceInfo.getName().contains(HARDWARE_BT_NAME)) {
                mHaloaiHudDevicesList.add(hudDeviceInfo);
            }
        }
        LOGI(String.format("[tryToConnectPairedDevice] found %d hud in paired devices list.", mHaloaiHudDevicesList.size()));
        mCurrentOperatingDevice = getNextOperatingHudDevice();
        if (mCurrentOperatingDevice != null) {
            LOGI(String.format("[tryToConnectPairedDevice] to connecte paired device-  :" + mCurrentOperatingDevice.getName() + "  " + mCurrentOperatingDevice.getUniqueAddress()));
            mHudConnectionStatus = HudConnectingTraceStatus.HUDC_STATUS_CONNECTING_PAIRED_DEVICE;
            notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECTING);
            mTransport.connect(this, mCurrentOperatingDevice);
            return true;
        }

        return false;
    }

    private void tryToSearchHudDevices() {
        LOGI("[tryToSearchHudDevices] begin to search.");
        notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECTING);
        mHudConnectionStatus = HudConnectingTraceStatus.HUDC_STATUS_SEARCHING;
        mTransport.searchForHudDevices(this);
    }

    private void connectToSearchedHaloaiHud() {
        mCurrentOperatingDevice = getNextOperatingHudDevice();
        if (mCurrentOperatingDevice != null) {
            LOGI("[connectToSearchedhaloaiHud] connect to another hud-" + mCurrentOperatingDevice.getUniqueAddress());
            mHudConnectionStatus = HudConnectingTraceStatus.HUDC_STATUS_CONNECTING_SEARCHED_DEVICE;
            notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECTING);
            mTransport.connect(this, mCurrentOperatingDevice);
        } else {
            LOGI("[connectToSearchedhaloaiHud] no more searched hud.");
            notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECT_FAILED);
        }
    }

    private void notifyHardwareStatus(HudConnectionStatus status) {
        switch (status){
            case HUD_CONNECTION_STATUS_CONNECTED :
                for (ConnecttionListener listener :mConnecttionListenerList){
                    listener.ConnectResult(true);
                }
                break;
            case HUD_CONNECTION_STATUS_CONNECTING :
                break;
            case HUD_CONNECTION_STATUS_CONNECT_FAILED :
                break;
            case HUD_CONNECTION_STATUS_DISCONNECTED :
                break;
            case HUD_CONNECTION_STATUS_CONNECTION_LOST:
                for (ConnecttionListener listener :mConnecttionListenerList){
                    listener.ConnectResult(false);
                }
                break;
            default:
                break;
        }


    }

    private EndpointDeviceInfo getNextOperatingHudDevice() {
        if (mHaloaiHudDevicesList.size() != 0) {
            EndpointDeviceInfo haloaiHud = mHaloaiHudDevicesList.get(0);
            mHaloaiHudDevicesList.remove(0);//remove this device from devices list so that it wouldn't be processed again.
            return haloaiHud;
        }

        return null;
    }

    private void startReconnectTask(int seconds){
        if(mReconnectTask == null) {
            mReconnectTask = new TimerTask() {
                @Override
                public void run() {
                    if (mReconnectCnt > 0) {
                        LOGI("第" + mReconnectCnt + "次重连....");
                        mReconnectCnt--;
                    }
                }
            };
        }
        mReconnectCnt = 3;
        mReconnectTimer.schedule(mReconnectTask,0,seconds*1000);
    }


    @Override
    public void newDeviceAvailable(EndpointDeviceInfo hudDeviceInfo) {
        LOGI("[newDeviceAvailable] found new device-" + hudDeviceInfo.getName());

        if (hudDeviceInfo.getName().equalsIgnoreCase(HARDWARE_BT_NAME)) {
            mHaloaiHudDevicesList.add(hudDeviceInfo);
        }
    }

    @Override
    public void updateDeviceInfo(EndpointDeviceInfo hudDeviceInfo) {
        if (hudDeviceInfo.getName().equalsIgnoreCase(HARDWARE_BT_NAME)) {
            for (int i = 0; i < mHaloaiHudDevicesList.size(); i++) {
                if (hudDeviceInfo.getUniqueAddress().equals(mHaloaiHudDevicesList.get(i).getUniqueAddress())) {
                    mHaloaiHudDevicesList.remove(i);
                    break;
                }
            }
            mHaloaiHudDevicesList.add(hudDeviceInfo);
        }
    }

    @Override
    public void hardwareStatus(HardwareStatus hardwareStatus, HardwareStatusReason hardwareStatusReason) {
        LOGI("new hardware status - " + hardwareStatus + " statusReason - " + hardwareStatusReason);
        if (hardwareStatus == HardwareStatus.DEVICE_SEARCH_STATUS_CHANGED) {
            switch (hardwareStatusReason) {
                case STARTED:
                    mHaloaiHudDevicesList.clear();
                    break;
                case ENDED:
                    connectToSearchedHaloaiHud();
                    break;
                default:
                    break;
            }
        } else if (hardwareStatus == HardwareStatus.DEVICE_DISCONNECTED) {
            if (hardwareStatusReason == HardwareStatusReason.FAILED) {
                if (false){ //TODO  蓝牙连接时判断
                    if (mHudConnectionStatus == HudConnectingTraceStatus.HUDC_STATUS_CONNECTING_RECENT_DEVICE) {
                        //Failed to connect recent device, then try to connect to paired device.
                        if (!tryToConnectPairedDevice()) {
                            //There isn't paired device be able to connect, then begin to search devices.
                            tryToSearchHudDevices();
                        }
                    } else if (mHudConnectionStatus == HudConnectingTraceStatus.HUDC_STATUS_CONNECTING_PAIRED_DEVICE) {
                        //Failed to connect a paired HUD device, then try to connect to another paired device or try to searching device to connect.
                        mCurrentOperatingDevice = getNextOperatingHudDevice();
                        if (mCurrentOperatingDevice != null) {
                            mHudConnectionStatus = HudConnectingTraceStatus.HUDC_STATUS_CONNECTING_PAIRED_DEVICE;
                            mTransport.connect(this, mCurrentOperatingDevice);
                        } else {
                            tryToSearchHudDevices();
                        }
                    } else if (mHudConnectionStatus == HudConnectingTraceStatus.HUDC_STATUS_CONNECTING_SEARCHED_DEVICE) {
                        //Failed to connect a searched HUD device, then try to connect to another searched device or report failed if there isn't more devices.
                        mCurrentOperatingDevice = getNextOperatingHudDevice();
                        if (mCurrentOperatingDevice != null) {
                            mHudConnectionStatus = HudConnectingTraceStatus.HUDC_STATUS_CONNECTING_SEARCHED_DEVICE;
                            mTransport.connect(this, mCurrentOperatingDevice);
                        } else {
                            notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECT_FAILED);
                        }
                    } else {
                        notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECT_FAILED);
                    }
                }

            } else if (hardwareStatusReason == HardwareStatusReason.LOST_CONNECTION) {
                LOGI("hardwareStatus is HardwareStatusReason.LOST_CONNECTION");
                notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECTION_LOST);
            } else {
                LOGI("hardwareStatus is HudConnectionStatus.HUD_CONNECTION_STATUS_DISCONNECTED");
                notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_DISCONNECTED);
            }

            //release the phone call manager

//        	if (this.imageController != null){
//        		this.imageController.unsetup();
//        		this.imageController = null;
//        	}

        } else if (hardwareStatus == HardwareStatus.DEVICE_STATUS_CHANGED) {

        } else if (hardwareStatus == HardwareStatus.DEVICE_CONNECTED) {
            updatePhoneState();
            responsePhoneInfo();
            queryHudInfo();
            notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECTED);


            //Setup the test image controller
//        	if(this.imageController != null){
//        		this.imageController.unsetup();
//        		this.imageController = null;
//        	}
//        	this.imageController = new ImageController(this.getApplicationContext(), this.mTransport);
//        	this.imageController.setup();

            //this.transportGetInstance = new HaloHudStartupActivity.TransportGetInstance(this.mTransport);


        } else if (hardwareStatus == HardwareStatus.DEVICE_RECONNECTED) {
            responsePhoneInfo();
            queryHudInfo();
            notifyHardwareStatus(HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECTED);
        }


    }

    @Override
    public void dataArrival(byte[] newData) {
        LOGI("new data arrival, data length is " + newData.length);
        processH2PMessages(newData);

    }


    @Override
    public EndpointDeviceInfo getRecentDevice() {
        return mTransport.getRecentDevice();
    }

    @Override
    public void clearRecentDevice() {
        mTransport.clearRecentDevice();
    }

    @Override
    public void connect(IPhoneEndpointTransportNotification hudHardwareNotification, EndpointDeviceInfo deviceInfo) throws HudTransportException {

    }

    @Override
    public void searchForHudDevices(IPhoneEndpointTransportNotification hudHardwareNotification) throws HudTransportException {

    }

    @Override
    public List<EndpointDeviceInfo> retrievePairedDevices() {
        return mTransport.retrievePairedDevices();
    }

    @Override
    public void stopDeviceSearch() {
        mTransport.stopDeviceSearch();
    }

    @Override
    public boolean isConnected() {
        return mTransport.isConnected();
    }

    @Override
    public boolean isSearching() {
        return mTransport.isSearching();
    }

    @Override
    public void disconnect() {
        mTransport.disconnect();
    }

    @Override
    public void send(ByteBuffer byteBuffer) {
        mTransport.send(byteBuffer);
    }

    @Override
    public void setLogger(IHaloLogger engineLogger) {
        mTransport.setLogger(engineLogger);
    }


    private void LOGI(String msg) {
        if (mLogger != null) mLogger.logI(this.getClass().getName(), msg);
    }


    private void responsePhoneInfo() {
        LOGI("发送手机信息给HUD");
        Phone2HudMessages phone2HudMessages = Phone2HudMessagesUtils.preparePhoneInfoDeclaim(null, mAppContext);
        mLastPhoneRequestIdStr = Phone2HudMessagesUtils.getPhoneRequestIdStr();
        phone2HudMessages = Phone2HudMessagesUtils.addPhoneRequestId(phone2HudMessages, mLastPhoneRequestIdStr);
        ByteBuffer buffer = ByteBuffer.wrap(phone2HudMessages.encapsulateHudP2HData());
        mTransport.send(buffer);
    }

    private void queryHudInfo() {


    }

    private void processH2PMessages(byte[] newData) {
        try {
            Hud2PhoneMessagesProto hud2PhoneMessagesProto = Hud2PhoneMessagesProto.parseFrom(newData);
            Hud2PhoneMessages hud2PhoneMessages = new Hud2PhoneMessages(hud2PhoneMessagesProto);
            for (IDataDispatcher dispatcher : this.dataDispatcherList) {
                dispatcher.dispatchH2PData(hud2PhoneMessages);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void processHudQueryResponse() {

    }

    private void updatePhoneState() {

    }

}
