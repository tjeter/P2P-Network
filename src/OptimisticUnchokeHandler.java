import java.util.List;
import java.util.Random;

public class OptimisticUnchokeHandler extends Thread 
{
    private static volatile int optimisticallyUnchokedNeighbor = -1;

    @Override
    public void run() 
    {
        try 
        {
            Thread.sleep(2000);
        } 
        catch(InterruptedException e1) 
        {
            e1.printStackTrace();
        }

        while(PeerProcess.isRunning) 
        {
            unchokeRandomNeighbor();
            try 
            {
                Thread.sleep(ConfigReader.getOptimisticUnchokingInterval()*1000);
            } 
            catch(InterruptedException e) 
            {
                System.out.print("Optimistic unchoke sleep interrupted");
                e.printStackTrace();
                break;
            }
        }
    }

    public static void unchokeRandomNeighbor() 
    {
        List<Integer> eligibleNeighbors;

        eligibleNeighbors = ChokeHandler.getChokedNeighbors();
        eligibleNeighbors.removeAll(InterestHandler.getUninterestedPeers());
        
        if(eligibleNeighbors.isEmpty()) 
        {
            System.out.println(Logger.getTimestamp() + ": NO neighbors eligible for OPTIMISTIC UNCHOKE");
            return;
        }
            
        Random r = new Random();
        int randomEligibleNeighborIndex = r.nextInt(eligibleNeighbors.size());

        PeerProcess.unchokePeer(eligibleNeighbors.get(randomEligibleNeighborIndex));
        optimisticallyUnchokedNeighbor = eligibleNeighbors.get(randomEligibleNeighborIndex);
        Logger.logNewOptimisticallyUnchokedNeighbor(optimisticallyUnchokedNeighbor);
        System.out.println(Logger.getTimestamp() + ": Peer " + eligibleNeighbors.get(randomEligibleNeighborIndex) + " OPTIMISTICALLY UNCHOKED");
    }

    public static synchronized int getOptimisticallyUnchokedNeighbor() 
    {
        return optimisticallyUnchokedNeighbor;
    }
}