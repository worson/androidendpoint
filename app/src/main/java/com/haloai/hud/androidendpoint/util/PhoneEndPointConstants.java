package com.haloai.hud.androidendpoint.util;

import android.util.SparseArray;

import com.haloai.hud.utils.HaloLoggerConsole;
import com.haloai.hud.utils.IHaloLogger;

/**
 * Created by wangshengxing on 16/4/20.
 */
public class PhoneEndPointConstants {

    //Debug Constants.
    public final static IHaloLogger gLogger = new HaloLoggerConsole();

    //Client to service message code define
    public final static int C2S_MSG_REGISTER_CLIENT_LISTENER	=	1;		//登记Client的Listener.
    public final static int C2S_MSG_UNREGISTER_CLIENT_LISTENER	=	2;		//注销Client的Listener.
    public final static int C2S_MSG_HUD_CONNECT					=	3;		//主动开始HUD连接。
    public final static int C2S_MSG_HUD_DISCONNECT				=	4;		//主动断开HUD连接。
    public final static int C2S_MSG_HUD_COMMAND_DATA			=	5;		//HUD命令数据
    //Service to client message code define
    public final static int S2C_MSG_HUD_CONNECTION_STATUS				= 	100;	//HUD硬件状态消息
    public final static String MSG_KEY_CONNECTION_STATUS_CODE			=	"S2C_msg_data_hardware_status_code";
    public final static String MSG_KEY_CONNECTION_STATUS_DEVICE		=	"S2C_msg_data_hardware_status_device";
    //TODO 需要针对多个client端建立一个有效的分发机制
    public final static int S2C_MSG_HUD_REQUEST_PHONE_RESOURCES		=	101;	//HUD请求Phone上的资源数据
    public final static String MSG_KEY_REQUEST_PHONE_RES_REQID	=	"S2C_msg_data_request_phone_res_reqid";
    public final static String MSG_KEY_REQUEST_PHONE_RES_TIMESTAMP	=	"S2C_msg_data_request_phone_res_timestamp";
    public final static String MSG_KEY_REQUEST_PHONE_RES_NAME	=	"S2C_msg_data_request_phone_res_name";
    public final static int S2C_MSG_HUD_UPDATE_HUD_INFO_CAPABILITY	=	102;	//HUD设备信息更新，可以同时包括Version和Capability
    public final static String MSG_KEY_HUD_DEVICE_INFO	=	"S2C_msg_data_hud_device_info";
    public final static String MSG_KEY_HUD_CAPABILITY	=	"S2C_msg_data_hud_capability";

    //用于HaloHudSupervisorService通讯的常量和变量
    public static String MAIN_PROC_PACKAGE_NAME;
    public static String HALO_HUD_SUPERVISOR_SERVICE_NAME;
    public final static String EXTRA_DATA_HARDWARE_INTERFACE_TYPE = "HudManagerService_extra_hardwareinterfactype";
    public final static String EXTRA_DATA_LOGGER_ENGINE_CLAA_NAME = "HudManagerService_extra_loggerengineclassname";

    public static enum HudHardwareInterfaceType {
        BLUETOOTH_SSP,
        BLUETOOTH_BLT,
        USB
    }

    /**
     * Hud连接状态。
     */
//    public static enum HudConnectionStatus
//    {
//        /** Hud已连接状态。 */
//        HUD_CONNECTION_STATUS_CONNECTED,
//        /** Hud连接中状态。 */
//        HUD_CONNECTION_STATUS_CONNECTING,
//        /** Hud断开状态。 */
//        HUD_CONNECTION_STATUS_DISCONNECTED,
//        /** Hud断开状态，由连接失败导致。 */
//        HUD_CONNECTION_STATUS_DISCONNECTED_FAILED,
//        /** Hud断开状态，连接后断开的，比如Hud设备关闭。*/
//        HUD_CONNECTION_STATUS_DISCONNECTED_LOST,
//    }

    public static enum HudConnectionStatus { //HudHardwareStatus {
        /** Hud已连接状态。 */
        HUD_CONNECTION_STATUS_CONNECTED(0),
        /** Hud连接中状态。 */
        HUD_CONNECTION_STATUS_CONNECTING(1),
        /** Hud断开状态。 */
        HUD_CONNECTION_STATUS_DISCONNECTED(2),
        /** Hud断开状态，由连接失败导致。 */
        HUD_CONNECTION_STATUS_CONNECT_FAILED(3),
        /** Hud断开状态，连接后断开的，比如Hud设备关闭。*/
        HUD_CONNECTION_STATUS_CONNECTION_LOST(4);

        private final int value;
        HudConnectionStatus(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
//        public HudConnectionStatus mapToHudConnectionStatus() {
//        	if (this.value == 0)
//        		return HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECTED;
//        	else if (this.value == 1)
//        		return HudConnectionStatus.HUD_CONNECTION_STATUS_CONNECTING;
//        	else if (this.value == 2)
//        		return HudConnectionStatus.HUD_CONNECTION_STATUS_DISCONNECTED;
//        	else if (this.value == 3)
//				return HudConnectionStatus.HUD_CONNECTION_STATUS_DISCONNECTED_FAILED;
//        	else if (this.value == 4)
//        		return HudConnectionStatus.HUD_CONNECTION_STATUS_DISCONNECTED_LOST;
//        	else
//        		return HudConnectionStatus.HUD_CONNECTION_STATUS_DISCONNECTED;
//        }
    }

    public static final SparseArray<HudConnectionStatus> hudHardwareStatusMap = new SparseArray<HudConnectionStatus>();//new HashMap<Integer, HudHardwareStatus>();
    static {
        for (HudConnectionStatus status : HudConnectionStatus.values()) {
            hudHardwareStatusMap.put(status.value, status);
        }
    }

    public static String getPhoneRequestId(){
        return ""+System.currentTimeMillis();
    }
//	public static enum HudHardwareStatus {
//		HUD_STATUS_UNKNOWN,
//		HUD_STATUS_DISCONNECTED,
//		HUD_STATUS_CONNECTING,
//		HUD_STATUS_CONNECTED,
//		HUD_STATUS_SCANNING
//	}
}
