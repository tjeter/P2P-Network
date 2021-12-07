import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PeerProcess 
{
    static volatile ConcurrentHashMap<Integer, Client> allClients = new ConcurrentHashMap<>();
    public static volatile int selfClientID = -1;

    static volatile OptimisticUnchokeHandler ouh = null;
    static volatile FindPreferredNeighbors fpn = null;

    public static volatile boolean isRunning = true;

    public static volatile Server server;

    public static void main(String args[]) 
    {
        selfClientID = Integer.parseInt(args[0]);
        DebugLogger.initializeLogger(selfClientID);
        
        System.out.println(Logger.getTimestamp() + ": : Initializing peer...");
        Logger.initializeLogger(selfClientID);

        Bitfield.init(selfClientID);
        int state = ConfigReader.getStateFromPeerID(selfClientID);
        FileHandler.init(selfClientID, state == 1);

        startServer();
        try 
        {
            //Sleep here because all servers start in parallel and need time before connecting to each other
            Thread.sleep(15000);     
        } 
        catch(InterruptedException e2) 
        {
            e2.printStackTrace();
        }
        connectToPeers();

        ouh = new OptimisticUnchokeHandler();
        ouh.start();
        fpn = new FindPreferredNeighbors();
        fpn.start();
        System.out.println(Logger.getTimestamp() + ": Done initializing!");
        try 
        {
            Thread.sleep(450000);
        } 
        catch(InterruptedException e) 
        {
            e.printStackTrace();
        }
        finally 
        {
            System.out.println(Logger.getTimestamp() + ": Process has reached the allotted running time. Shutting down all processes...");
            stopAllProcesses();
        }
    }

    public static void connectToPeers() 
    {
        List<Integer> peerIDs = ConfigReader.getAllPeerIDs();
        
        for(Integer peerID : peerIDs) 
        {
            try 
            {
                if(peerID < selfClientID) 
                {
                    System.out.println(Logger.getTimestamp() + ": Connecting to peer " + peerID + "...");
                    Socket connection = new Socket(ConfigReader.getIPFromPeerID(peerID), ConfigReader.getPortFromPeerID(peerID));
                    Client c = new Client(connection, true);
                    c.start();
                    allClients.put(peerID, c);
                    System.out.println(Logger.getTimestamp() + ": TCP connection to peer " + peerID + " established.");
                }
                else 
                {
                    
                }
            }
            catch(Exception e) 
            {
                System.out.println(Logger.getTimestamp() + ": Failed to connect to peer: " + peerID);
                e.printStackTrace();
            }
        }
    }

    public static List<Integer> getPeerIDList() 
    {
        return Arrays.asList(allClients.keySet().toArray(new Integer[0]));
    }

    public static double getDownloadRateOfPeer(int peerID) 
    {
        return allClients.get(peerID).getDownloadRateInKBps();
    }

    public static void unchokePeer(int peerID) 
    {
        allClients.get(peerID).unchokePeer();
    }

    public static void chokePeer(int peerID) 
    {
        allClients.get(peerID).chokePeer();
    }

    public static void setPreferredNeighbors(List<Integer> preferredNeighbors) 
    {
        Logger.logChangedPreferredNeighbors(preferredNeighbors);

	List<Integer> peerIDList = getPeerIDList();

        for(Integer peerID : peerIDList) 
        {
            if(preferredNeighbors.contains(peerID) && ChokeHandler.getChokedNeighbors().contains(peerID))
            {
                unchokePeer(peerID);
            }
            else if(peerID != OptimisticUnchokeHandler.getOptimisticallyUnchokedNeighbor())
            {
                chokePeer(peerID);
            }
        }
	}

    public static void startServer() 
    {
        Server.selfClientID = selfClientID;
        server = new Server();
        server.start();
    }

    public static void connectionFromNewPeer(int peerID, Client c) 
    {
        allClients.put(peerID, c);
    }

    public static void broadcastHaveMessage(int pieceIndex) 
    {
        byte[] haveMessage = HaveHandler.constructHaveMessage(pieceIndex);
        
        for(Client c : allClients.values()) 
        {
            System.out.println(Logger.getTimestamp() + ": Sending HAVE message to " + c.otherPeerID);
            c.sendMessage(haveMessage);
        }
    }

    public static void sendMessageToPeer(int peerID, byte[] message) 
    {
        allClients.get(peerID).sendMessage(message);
    }

    public static synchronized void pieceSharingHasCompleted() 
    {
        System.out.println(Logger.getTimestamp() + ": ALL PIECE SHARING IS COMPLETE! Stopping all processes...");
        stopAllProcesses();
    }

    public static synchronized void stopAllProcesses() 
    {
        isRunning = false;
        System.out.println(Logger.getTimestamp() + ": Stopping server...");
        try 
        {
            Server.listener.close();
        } 
        catch(IOException e1) 
        {
            e1.printStackTrace();
        }
        
        for(Client client: allClients.values()) 
        {
            client.interrupt();
        }

        System.out.println(Logger.getTimestamp() + ": PeerProcess stopping...");
        try 
        {
            Thread.sleep(2000);
        }
        catch(InterruptedException e) 
        {
            e.printStackTrace();
        }
        System.out.println(Logger.getTimestamp() + ": PeerProcess stopped.");
    }
}
