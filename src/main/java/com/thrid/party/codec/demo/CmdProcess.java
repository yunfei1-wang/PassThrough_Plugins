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
            {
                "identifier":"0",
                "msgType":"cloudRsp",
                "request": ***,
                "errcode":0,
                "hasMore":0
            }
            * */
            if (msgType.equals("cloudRsp")) {
                this.errcode = input.get("errcode").asInt();
                this.hasMore = input.get("hasMore").asInt();
            }
            else {
            /*
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
                if (this.cmd.equals("COMMAND")) {
                    String str = paras.get("value").asText();
                    
           
                    byte[] byteRead = hexStringToByteArray(str);

                    //byteRead[0] = (byte) 0xC1;
                    //byteRead[1] = (byte) 0x04;
                    //byteRead[2] = (byte) meterStaus;

                    if (Utilty.getInstance().isValidofMid(mid)) {

                        byte[] byteMid = new byte[2];
                        byteMid = Utilty.getInstance().int2Bytes(mid, 2);
                        //byteRead[3] = byteMid[0];
                        //byteRead[4] = byteMid[1];
                    }

                    return byteRead;
                }

            }

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
