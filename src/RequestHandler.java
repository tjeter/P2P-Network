import java.nio.ByteBuffer;
import java.util.*;
import javax.sql.rowset.spi.SyncResolver;

public class RequestHandler 
{
	static volatile BitSet piecesWeHaveAlreadyRequested = new BitSet();

	public static synchronized int findNeededPieceIndexFromPeer(int peerID) 
	{
		BitSet ourBitfield = Bitfield.getSelfBitfield();
		BitSet peerBitfield = Bitfield.getPeerBitfield(peerID);

		BitSet lackingPieces = (BitSet)peerBitfield.clone();
		lackingPieces.andNot(ourBitfield);

		if(lackingPieces.cardinality() == 0)
		{
			return -1;
		}
		Random r = new Random();

		int numTriesLeft = 10;
		while(numTriesLeft > 0) 
		{
			int randomPick = r.nextInt(lackingPieces.cardinality()) + 1;
			int curIndex = -1;

			for(int i = 0; i < randomPick; i++)
			{
				curIndex = lackingPieces.nextSetBit(curIndex+1);
			}
			if(clientNeedsThisPiece(curIndex)) 
			{
				return curIndex;
			}
			numTriesLeft -= 1;
		}
		System.out.println("Failed to randomly select a piece; choosing the first available piece instead");
		return lackingPieces.nextSetBit(0); 
	}

	public static byte[] constructRequestMessage(int pieceIndex) 
	{
		byte[] pieceIndexBytes = ActualMessageHandler.convertIntTo4Bytes(pieceIndex);
		byte[] message = ActualMessageHandler.addHeader(pieceIndexBytes, ActualMessageHandler.REQUEST);

		piecesWeHaveAlreadyRequested.set(pieceIndex, true);
		return message;
	}

	public static boolean clientNeedsSomePieceFromPeer(int peerID) 
	{
		return findNeededPieceIndexFromPeer(peerID) != -1;
	}

	public static boolean clientNeedsThisPiece(int pieceIndex) 
	{
        	if(Bitfield.clientHasThisPiece(pieceIndex))
        	{
			return false;
        	}
		if(piecesWeHaveAlreadyRequested.get(pieceIndex))
		{
			return false;
		}
		return true;
    	}

	public static byte[] constructRequestMessageAndChooseRandomPiece(int peerID) 
	{
		int pieceIndex = findNeededPieceIndexFromPeer(peerID);
		if(pieceIndex == -1) 
		{
			System.out.println(Logger.getTimestamp() + ": FATAL: Cannot find the piece client needs from peer: " + peerID);
			return new byte[0];
		}
		return constructRequestMessage(pieceIndex);
	}

	public static void receivedRequestMessage(int peerID, byte[] msgPayload) 
	{
		if(ChokeHandler.getChokedNeighbors().contains(peerID)) 
		{
			System.out.println(Logger.getTimestamp() + ": REQUEST message from peer " + peerID + " will be ignored because it is choked.");
			return;
		}

		int pieceIndex = ActualMessageHandler.byteArrayToInt(msgPayload);
		System.out.println(Logger.getTimestamp() + ": Sending piece message (index " + pieceIndex + ") to " + peerID + "...");
		PeerProcess.sendMessageToPeer(peerID, PieceHandler.constructPieceMessage(pieceIndex));
		System.out.println(Logger.getTimestamp() + ": Piece (index " + pieceIndex + ") message sent!");
	}

	public static void clearPiecesAlreadyRequestedList() 
	{
		piecesWeHaveAlreadyRequested.clear();
	}
}
