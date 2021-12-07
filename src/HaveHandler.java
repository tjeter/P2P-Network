import java.math.BigInteger;
import java.nio.ByteBuffer;

public class HaveHandler 
{
    public static synchronized void receivedHaveMessage(int fromPeerID, byte[] msgPayload) 
    {
        int haveIndex = ActualMessageHandler.byteArrayToInt(msgPayload);
        Logger.logReceivedHaveMessage(fromPeerID, haveIndex);
        Bitfield.peerReceivedPiece(fromPeerID, haveIndex);

        //Send interested / non-interested message
        boolean interested = RequestHandler.clientNeedsSomePieceFromPeer(fromPeerID);
        System.out.println(Logger.getTimestamp() + ": Sending " + (interested ? "" : "NON-") + "INTERESTED message to " + fromPeerID);
        PeerProcess.sendMessageToPeer(fromPeerID, InterestHandler.constructInterestMessage(interested));
    }

    public static byte[] constructHaveMessage(int pieceIndex) 
    {
        byte[] pieceIndexByteArray = ActualMessageHandler.convertIntTo4Bytes(pieceIndex);
        return ActualMessageHandler.addHeader(pieceIndexByteArray, ActualMessageHandler.HAVE);
    }
}