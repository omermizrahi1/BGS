package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class BidiMessageEncoderDecoder implements MessageEncoderDecoder<List<Object>> {

    private byte[] bytes = new byte[1 << 10];
    private int len = 0;
    private final char DELIM = ';';

    public List<Object> decodeNextByte(byte nextByte) {

        if (nextByte == DELIM) {
            return popMessage();
        }

        pushByte(nextByte);
        return null; //not a line yet
    }

    public byte[] encode(List<Object> message) {
        int size = 0;
        List<byte[]> encodedBytes = new LinkedList<>();
        for (Object o: message) {
            byte[] bytesArray;
            if (o instanceof String) {
                bytesArray = ((String) o).getBytes();
            }
            else if (o instanceof Short){
                bytesArray = shortToBytes((Short) o);
            }
            else  /*if (o instanceof Byte)*/{
                bytesArray = new byte[] {(Byte)o};
            }

            size += bytesArray.length;
            encodedBytes.add(bytesArray);
        }
        int i = 0;
        byte[] encodedMsg = new byte[size + 1];
        for (byte[] encBytes: encodedBytes){
            for (byte b: encBytes){
                encodedMsg[i] = b;
                i++;
            }
        }
        encodedMsg[encodedMsg.length - 1] = (byte)';';
        return encodedMsg;
    }

    private List<Object> popMessage() {
        byte[] opcodeBytes = new byte[2];
        opcodeBytes[0] = bytes[0];
        opcodeBytes[1] = bytes[1];
        short opcode = bytesToShort(opcodeBytes);
        List<Object> result = new LinkedList<>();
        result.add(opcode);
        List<Integer> zeroIndexes = new LinkedList<>();
        for (int i = 2; i <len ; i++) {
            if (bytes[i] == 0){
                zeroIndexes.add(i);
            }
        }
        int i;
        switch (opcode){
            case 1: //Register
            case 5: //Post
            case 6: //PM
            case 8: //STAT
            case 12: //BLOCK
                i = 2;
                for (int index: zeroIndexes) {
                    String s = new String(bytes, i, index - i, StandardCharsets.UTF_8);
                    result.add(s);
                    result.add((short)0);
                    i=index+1;
                }
                break;
            case 2: //Login
                i = 2;
                int lastIndex = zeroIndexes.get(zeroIndexes.size() - 1);
                int secondLastIndex = zeroIndexes.get(zeroIndexes.size() - 2);
                boolean isOne = false;
                if (lastIndex != secondLastIndex + 1){ //checks if captcha is 1 {
                    zeroIndexes.add(lastIndex + 1);
                    isOne = true;
                }
                for (int index: zeroIndexes) {
                    String s = new String(bytes, i, index - i, StandardCharsets.UTF_8);
                    if (!s.isEmpty()){result.add(s);}
                    result.add((short)0);
                    i = index + 1;
                }
                if  (isOne)  {result.set(result.size()-1, (short)1);}
                break;
            case 3: //Logout
                //has only opcode
                break;
            case 4: //Follow
                i=3;
                short follow = (short) (bytes[2] == 0 ? 0 : 1);
                result.add(follow);
                result.add(new String(bytes, i, len-i, StandardCharsets.UTF_8));
                break;
            case 7: //LOGSTAT
                //has only opcode
                break;


        }
        len = 0;
        return result;
    }

    private short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    private byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

}
