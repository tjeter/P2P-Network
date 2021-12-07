import java.nio.ByteBuffer;

public class Handshake 
{
	private static final String HEADER = "P2PFILESHARINGPROJ";
    private static final String BITS = "0000000000";

	public static byte[] constructHandshakeMessage(int selfClientID) 
	{
		return (HEADER + BITS + selfClientID).getBytes();
	}

	public static int receivedHandshakeResponseMessage(byte[] handshakeResponseMessage) 
	{
		System.out.println(Logger.getTimestamp() + ": Receiving handshake response with length " + handshakeResponseMessage.length);

		if(handshakeResponseMessage.length != 32) 
		{
			System.out.println(Logger.getTimestamp() + ": Non-handshake is of type: " + ActualMessageHandler.getMsgType(handshakeResponseMessage));
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(handshakeResponseMessage);
		byte[] headerbytes = new byte[18];
		byte[] zerobitsbytes = new byte[10];
		byte[] peerIdbytes = new byte[4];
		byteBuffer = byteBuffer.get(headerbytes, 0, 18);
		byteBuffer = byteBuffer.get(zerobitsbytes, 0, 10);
		byteBuffer = byteBuffer.get(peerIdbytes, 0, 4);
		String peerIdString = new String(peerIdbytes);
		int peerIdInt = Integer.parseInt(peerIdString);

		return peerIdInt;
	}

	public static boolean isHandshakeMessage(byte[] message) 
	{
		if(message.length != 32)
		{
			return false;
		}
		try 
		{
			ByteBuffer byteBuffer = ByteBuffer.wrap(message);
			byte[] headerbytes = new byte[18];
			byte[] zerobitsbytes = new byte[10];
			byte[] peerIdbytes = new byte[4];
			byteBuffer = byteBuffer.get(headerbytes, 0, 18);
			byteBuffer = byteBuffer.get(zerobitsbytes, 0, 10);
			byteBuffer = byteBuffer.get(peerIdbytes, 0, 4);
			String headerString = new String(headerbytes);
			String zerobitString = new String(zerobitsbytes);
			String peerIdString = new String(peerIdbytes);
			int peerIdInt = Integer.parseInt(peerIdString);
			if(headerString.equals(HEADER) && zerobitString.equals(BITS)) 
			{
				return true;
			}
		}
		catch(Exception e) 
		{
			return false;
		}
		return false;
	}
}