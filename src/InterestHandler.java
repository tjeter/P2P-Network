import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//Contains functions for interested and not interested signals
public class InterestHandler 
{
    static volatile ArrayList<Integer> interestedPeers = new ArrayList<>();
    static volatile ArrayList<Integer> uninterestedPeers = new ArrayList<>();

    public static byte [] constructInterestMessage(boolean interested) 
    {
    	byte [] emptyByte = new byte[0];
    	if(interested) 
    	{
    		return ActualMessageHandler.addHeader(emptyByte, ActualMessageHandler.INTERESTED);
    	}
    	else
    	{
    		return ActualMessageHandler.addHeader(emptyByte, ActualMessageHandler.UNINTERESTED);
    	}
    }

    public static synchronized List<Integer> getUninterestedPeers() 
    {
        return new ArrayList<>(uninterestedPeers);
    }

    public static synchronized List<Integer> getInterestedPeers() 
    {
        return new ArrayList<>(interestedPeers);
    }

    public static synchronized void receivedInterestedMessage(int fromPeerID) 
    {
        Logger.logInterestedMessageReceived(fromPeerID);
        interestedPeers.add(fromPeerID);
        if(uninterestedPeers.contains(fromPeerID))
        {
            uninterestedPeers.remove(Integer.valueOf(fromPeerID));
        }
    }

    public static synchronized void receivedUninterestedMessage(int fromPeerID) 
    {
        Logger.logNotInterestedMessageReceived(fromPeerID);
        uninterestedPeers.add(fromPeerID);
        if(interestedPeers.contains(fromPeerID))
        {
            interestedPeers.remove(Integer.valueOf(fromPeerID));
        }
    }

    public static synchronized boolean peerIsInterestedInClient(int peerID) 
    {
        return interestedPeers.contains(peerID);
    }
}