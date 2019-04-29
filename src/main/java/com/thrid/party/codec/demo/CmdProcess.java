package com.thrid.party.codec.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
//这个类什么用处？这个是加密服务器下来的数据
public class CmdProcess {

    //private String identifier = "123";
    private String msgType = "deviceReq";
    //private String serviceId = "WaterMeterBasic";
    private String cmd = "COMMAND";
    private int hasMore = 0;
    private int errcode = 0;
    private int mid = 0;
    private JsonNode paras;


    public CmdProcess() {
    }

    public CmdProcess(ObjectNode input) {

        try {
            // this.identifier = input.get("identifier").asText();
            this.msgType = input.get("msgType").asText();
            /*
            骞冲彴鏀跺埌璁惧涓婃姤娑堟伅锛岀紪鐮丄CK
            {
                "identifier":"0",
                "msgType":"cloudRsp",
                "request": ***,//璁惧涓婃姤鐨勭爜娴�
                "errcode":0,
                "hasMore":0
            }
            * */
            if (msgType.equals("cloudRsp")) {
                //鍦ㄦ缁勮ACK鐨勫��
                this.errcode = input.get("errcode").asInt();
                this.hasMore = input.get("hasMore").asInt();
            }
            else {
            /*
            骞冲彴涓嬪彂鍛戒护鍒拌澶囷紝杈撳叆
            {
                "identifier":0,
                "msgType":"cloudReq",
                "serviceId":"WaterMeterBasic",
                "cmd":"NBWATERMETERCOMMAND",
                "paras":{"value":"20"},
                "hasMore":0,
                "mid":0

            }
            * */
                //姝ゅ闇�瑕佽�冭檻鍏煎鎬э紝濡傛灉娌℃湁浼爉Id锛屽垯涓嶅鍏惰繘琛岀紪鐮�

                if (input.get("mid") != null) {
                    this.mid = input.get("mid").intValue();
                }
                this.cmd = input.get("cmd").asText();
                this.paras = input.get("paras");
                this.hasMore = input.get("hasMore").asInt();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public byte[] toByte() {
        try {
            if (this.msgType.equals("cloudReq")) {
                /*
                搴旂敤鏈嶅姟鍣ㄤ笅鍙戠殑鎺у埗鍛戒护锛屾湰渚嬪彧鏈変竴鏉℃帶鍒跺懡浠わ細SET_DEVICE_LEVEL
                濡傛灉鏈夊叾浠栨帶鍒跺懡浠わ紝澧炲姞鍒ゆ柇鍗冲彲銆�
                * */
                if (this.cmd.equals("COMMAND")) {
                    String str = paras.get("value").asText();
                    
           
                    byte[] byteRead = hexStringToByteArray(str);

                    //byteRead[0] = (byte) 0xC1;
                    //byteRead[1] = (byte) 0x04;
                    //byteRead[2] = (byte) meterStaus;

                    //姝ゅ闇�瑕佽�冭檻鍏煎鎬э紝濡傛灉娌℃湁浼爉Id锛屽垯涓嶅鍏惰繘琛岀紪鐮�
                    if (Utilty.getInstance().isValidofMid(mid)) {
                    	//是不是数组需要这样写，单个字节不需要？
                        byte[] byteMid = new byte[2];
                        byteMid = Utilty.getInstance().int2Bytes(mid, 2);
                        //byteRead[3] = byteMid[0];
                        //byteRead[4] = byteMid[1];
                    }

                    return byteRead;
                }

            }

            /*
            骞冲彴鏀跺埌璁惧鐨勪笂鎶ユ暟鎹紝鏍规嵁闇�瑕佺紪鐮丄CK锛屽璁惧杩涜鍝嶅簲锛屽鏋滄澶勮繑鍥瀗ull锛岃〃绀轰笉闇�瑕佸璁惧鍝嶅簲銆�
            * */
            else if (this.msgType.equals("cloudRsp")) {
                byte[] ack = new byte[4];
                ack[0] = (byte) 0xCC;
                ack[1] = (byte) 0xCC;
                ack[2] = (byte) this.errcode;
                ack[3] = (byte) this.hasMore;
                return ack;
            }
            return null;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return null;
        }
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return b;
    }

}
