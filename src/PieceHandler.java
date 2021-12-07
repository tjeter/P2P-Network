import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PieceHandler 
{
    public static synchronized void receivedPieceMessage(int peerID, byte[] msgPayload) 
    {
        int pieceIndex = getPieceIndexFromPiecePayload(msgPayload);
        Logger.logPieceDownloadComplete(peerID, pieceIndex, Bitfield.getNumberOfPiecesClientHas() + 1);
        FileHandler.storePiece(pieceIndex, getPieceBytesFromPiecePayload(msgPayload));
        Bitfield.selfReceivedPiece(pieceIndex);
        PeerProcess.broadcastHaveMessage(pieceIndex);

        if(RequestHandler.clientNeedsSomePieceFromPeer(peerID)) 
        {
            byte[] requestMessage = RequestHandler.constructRequestMessageAndChooseRandomPiece(peerID);
            if(requestMessage.length != 0) 
            {
                System.out.println(Logger.getTimestamp() + ": Sending ANOTHER request for piece from " + peerID);
                PeerProcess.sendMessageToPeer(peerID, requestMessage);
            }
            else 
            {
                throw new RuntimeException("Generated request message was empty");
            }
        }
    }

    public static synchronized byte[] constructPieceMessage(int pieceIndex, byte[] pieceBytes) 
    {
        try 
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write(ActualMessageHandler.convertIntTo4Bytes(pieceIndex));
            outputStream.write(pieceBytes);
    
            byte[] fullPayload = outputStream.toByteArray();
            return ActualMessageHandler.addHeader(fullPayload, ActualMessageHandler.PIECE);
        }
        catch(IOException exception) 
        {
            throw new RuntimeException("IO Exception in constructPieceMessage\n" + exception.getMessage());
        }
    }

    public static synchronized byte[] constructPieceMessage(int pieceIndex) 
    {
        byte[] pieceBytes = FileHandler.getFilePiece(pieceIndex);
        return constructPieceMessage(pieceIndex, pieceBytes);
    }

    public static int getPieceIndexFromPiecePayload(byte[] pieceMsgPayload) 
    {
        return ByteBuffer.wrap(Arrays.copyOfRange(pieceMsgPayload, 0, 4)).getInt();
    }

    public static byte[] getPieceBytesFromPiecePayload(byte[] pieceMsgPayload) 
    {
        return Arrays.copyOfRange(pieceMsgPayload, 4, pieceMsgPayload.length);
    }
}