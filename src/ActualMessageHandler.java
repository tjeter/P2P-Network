import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class ActualMessageHandler 
{
	public static final int CHOKE = 0;
	public static final int UNCHOKE = 1;
	public static final int INTERESTED = 2;
	public static final int UNINTERESTED = 3;
	public static final int HAVE = 4;
	public static final int BITFIELD = 5;
	public static final int REQUEST = 6;
	public static final int PIECE = 7;

    public static byte[] extractPayload(byte[] fullMessage) 
    {
        ByteBuffer bytearray = ByteBuffer.wrap(fullMessage);
	byte[] msglengthbytes = new byte[4];
        bytearray = bytearray.get(msglengthbytes, 0, msglengthbytes.length);
        int msgLengthInt = byteArrayToInt(msglengthbytes);
        bytearray = bytearray.get(new byte[1], 0, 1);
        if(msgLengthInt == 1) 
        {
            byte [] msgpayloadbytes = new byte[0];
            return msgpayloadbytes;
        } 
        else 
        {
            byte[] msgpayloadbytes = new byte[msgLengthInt-1];
            bytearray = bytearray.get(msgpayloadbytes, 0, msgpayloadbytes.length);
            return msgpayloadbytes;
        }
    }
  
    //Four-digit byte array
    public static byte[] convertIntTo4Bytes(int pieceIndex) 
	{
	    return new byte[] 
	    {
	        (byte)((pieceIndex >> 24) & 0xff),
	        (byte)((pieceIndex >> 16) & 0xff),
	        (byte)((pieceIndex >> 8) & 0xff),
	        (byte)((pieceIndex >> 0) & 0xff),
	    };
	}
    
    //One-digit byte array
    public static byte[] convertIntoTo1Byte(int pieceIndex) 
    {
        BigInteger bigPieceIndex = BigInteger.valueOf(pieceIndex);      
        return bigPieceIndex.toByteArray();
    }
    
    public static byte[] addHeader(byte[] messagePayload, int messageType) 
    {
    	int lengthOfPayload = messagePayload.length + 1;
    	byte [] newLength = convertIntTo4Bytes(lengthOfPayload);
    	byte [] newMessageType = convertIntoTo1Byte(messageType);
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	try 
    	{
		stream.write(newLength);
		stream.write(newMessageType);
	    	stream.write(messagePayload);
	} 
    	catch(IOException e) 
    	{
		e.printStackTrace();
	}
    	byte [] combined = stream.toByteArray();
    	return (combined);
    }

    public static int byteArrayToInt(byte[] byteArray) 
    {
        return new BigInteger(byteArray).intValue();
    }
    
    public static int getMsgType(byte[] fullMessage) 
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(fullMessage);
	byte[] msglengthbytes = new byte[4];
    	byte[] msgtypebytes = new byte[1];
        byteBuffer = byteBuffer.get(msglengthbytes, 0, msglengthbytes.length);
        byteBuffer = byteBuffer.get(msgtypebytes, 0, 1);
        int type = byteArrayToInt(msgtypebytes);
	return type;
    }
}
