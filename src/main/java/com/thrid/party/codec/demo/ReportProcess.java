package com.thrid.party.codec.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportProcess {
    // private String identifier;
    private static final Logger logger1 = LoggerFactory.getLogger(ReportProcess.class);
    private String msgType = "deviceReq";
    private int hasMore = 0;
    private int errcode = 0;

    // SystemCmdRawData
    private String SystemCmdRawData = " ";

    // serviceId=Connectivity字段
    private int signalStrength = 0;
    private int linkQuality = 0;
    private int cellId = 0;

    // Location
    private String Laititude = " ";
    private String Longtitude = " ";

    private int pflen = 0; // 协议前缀的长度
    private byte noMid = 0x00;
    private byte hasMid = 0x01;
    private boolean isContainMid = false;
    private int mid = 0;

    /**
     * @param binaryData 设备发送给平台coap报文的payload部分
     *                   //////////////////////////////////////////
     *                   FFAAD0D000004D0F3836353335323033303030353035310000001E00000001000000000000015E60E4479DFC6DFFD501FF5B09B88F52030000020011063114000000471800000000000000000097
     *                   //////////////////////////////////////////
     * @return
     */

    public ReportProcess(byte[] binaryData) {
        // identifier参数可以根据入参的码流获得，本例指定默认值123
        // identifier = "123";

        /*
         * 如果是设备上报数据，返回格式为 { "identifier":"123", "msgType":"deviceReq", "hasMore":0,
         * "data":[{"serviceId":"Brightness", "serviceData":{"brightness":50}, {
         * "serviceId":"Electricity",
         * "serviceData":{"voltage":218.9,"current":800,"frequency":50.1,"powerfactor":0
         * .98}, { "serviceId":"Temperature", "serviceData":{"temperature":25}, ] }
         */

        if (binaryData[0] == (byte)0xB2) {

            byte[] LaititudeArray = new byte[5];
            for (int count = 0; count < 5; count++) {
                LaititudeArray[count] = binaryData[count + 1];
            }

            this.Laititude = new String(LaititudeArray);

            byte[] LongtitudeArray = new byte[5];
            for (int count = 0; count < 5; count++) {
                LongtitudeArray[count] = binaryData[count + 6];
            }

            this.Longtitude = new String(LongtitudeArray);

        }

        SystemCmdRawData = bytesToHexString(binaryData);

    }

    public ObjectNode toJsonNode() {
        try {
            // 组装body体
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();

            // 这里put了2次
            // root.put("identifier", this.identifier);
            root.put("msgType", this.msgType);

            // 根据msgType字段组装消息体
            if (this.msgType.equals("deviceReq")) {

                root.put("hasMore", this.hasMore);
                // 假设 arraynode 是个数组，和profile 里面的数组有啥关系
                ArrayNode arrynode = mapper.createArrayNode();

                // Connectivity 数据组装
                ObjectNode ConnectivityNode = mapper.createObjectNode();
                ConnectivityNode.put("serviceId", "Connectivity");
                ObjectNode ConnectivityData = mapper.createObjectNode();
                ConnectivityData.put("signalStrength", this.signalStrength); // RSPR ,dBm
                ConnectivityData.put("linkQuality", this.linkQuality); // SINR, dB
                ConnectivityData.put("cellId", this.cellId); // 小区ID
                ConnectivityNode.put("serviceData", ConnectivityData);
                arrynode.add(ConnectivityNode);

                // Location
                ObjectNode LocationNode = mapper.createObjectNode();
                LocationNode.put("serviceId", "Location");
                ObjectNode LocationData = mapper.createObjectNode();
                LocationData.put("Laititude", this.Laititude); // 纬度
                LocationData.put("Longtitude", this.Longtitude); // 经度
                LocationNode.put("serviceData", LocationData);
                arrynode.add(LocationNode);

                // SystemCmdRawData
                ObjectNode SystemCmdRawDataNode = mapper.createObjectNode();
                SystemCmdRawDataNode.put("serviceId", "SystemCmdRawData");
                ObjectNode SystemCmdRawData = mapper.createObjectNode();
                SystemCmdRawData.put("SystemCmdRawData", this.SystemCmdRawData); // 透传数据包
                SystemCmdRawDataNode.put("serviceData", SystemCmdRawData);
                arrynode.add(SystemCmdRawDataNode);

                root.put("data", arrynode);

            }

            else {
                root.put("errcode", this.errcode);
                // 此处需要考虑兼容性，如果没有传mid，则不对其进行解码
                if (isContainMid) {
                    root.put("mid", this.mid);// mid
                }
                // 组装body体，只能为ObjectNode对象
                ObjectNode body = mapper.createObjectNode();
                body.put("result", 0);
                root.put("body", body);
            }
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    /**
     * 把byte转为字符串的bit
     * 
     * public static String byteToBit(byte b) { return "" + (byte) ((b >> 7) & 0x1)
     * + (byte) ((b >> 6) & 0x1) + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
     * + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1) + (byte) ((b >> 1) & 0x1)
     * + (byte) ((b >> 0) & 0x1); }
     */

    public static String AToString(int i) {
        return Character.toString((char) i);
    }

    private short CheckSum(byte[] msg, int length) {
        int all = 0;
        for (int i = 2; i < length - 1; i++) {
            all += msg[i];

        }
        return (short) (all & 0xff);
    }

    /*
     * public static short byteToShort(byte[] b) { short s = 0; short s0 = (short)
     * (b[0] & 0xff);// 最低位 short s1 = (short) (b[1] & 0xff); s1 <<= 8; s = (short)
     * (s0 | s1); return s; }
     */

    public static int byte2Int(byte b) {
        int r = (int) b;
        return r;
    }

    public static String bytes2hex02(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String tmp = null;
        for (byte b : bytes) {
            // 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
            tmp = Integer.toHexString(0xFF & b);
            if (tmp.length() == 1)// 每个字节8为，转为16进制标志，2个16进制位
            {
                tmp = "0" + tmp;
            }
            sb.append(tmp);
        }

        return sb.toString();
    }

}
