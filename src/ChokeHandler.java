import java.util.*;

public class ChokeHandler 
{
	volatile static List<Integer> clientIsChokedBy = Collections.synchronizedList(new ArrayList<>());
	volatile static List<Integer> neighborsChokedByClient = Collections.synchronizedList(new ArrayList<>());

	public static synchronized List<Integer> getChokedNeighbors() 
	{
		return new ArrayList<>(neighborsChokedByClient);
	}

	public static synchronized void receivedChokeMessage(int otherPeerID) 
	{
		Logger.logChokedBy(otherPeerID);
		
		if(!clientIsChokedBy.contains(otherPeerID)) 
		{
			clientIsChokedBy.add(otherPeerID);
		}
	}

	public static synchronized void receivedUnchokeMessage(int otherPeerID) 
	{
		Logger.logUnchokedBy(otherPeerID);

		if(RequestHandler.clientNeedsSomePieceFromPeer(otherPeerID)) 
		{
			byte[] requestMessage = RequestHandler.constructRequestMessageAndChooseRandomPiece(otherPeerID);
			if(requestMessage.length != 0) 
			{
				System.out.println(Logger.getTimestamp() + ": Sending a REQUEST for piece from " + otherPeerID + " after just being unchoked");
				PeerProcess.sendMessageToPeer(otherPeerID, requestMessage);
			}
		}
		if(clientIsChokedBy.contains(otherPeerID)) 
		{
			clientIsChokedBy.remove(Integer.valueOf(otherPeerID));
		}
	}

	public static byte[] constructChokeMessage(int peerID, boolean choke) 
	{ 
		byte [] message = new byte[0];
		byte [] chokeMessage = null;

		//If choke is false, unchoke
		if(!choke) 
		{
        	chokeMessage = ActualMessageHandler.addHeader(message, ActualMessageHandler.UNCHOKE);
        	}
        	else 
        	{
        		chokeMessage = ActualMessageHandler.addHeader(message, ActualMessageHandler.CHOKE);
        	}
		return chokeMessage;
    	}

	public static synchronized boolean chokePeer(int peerID) 
	{
		if(neighborsChokedByClient.contains(peerID))
		{
			return false;
		}
		neighborsChokedByClient.add(peerID);
		return true;
	}

	public static synchronized boolean unchokePeer(int peerID) 
	{
		if(!neighborsChokedByClient.contains(peerID))
		{
			return false;
		}
		neighborsChokedByClient.remove(Integer.valueOf(peerID));
		return true;
	}
}
