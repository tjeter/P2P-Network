import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.*;

public class ConfigReader 
{
     //static String pathToPeerInfoCfg = "./config/PeerInfo.cfg";
    //static String pathToCommonCfg = "./config/Common.cfg";
	
    static String pathToPeerInfoCfg = "C:/Users/Tre' Jeter/Desktop/Java Projects/Projects/Peer-To-Peer Network/src/PeerInfo.cfg";
    static String pathToCommonCfg = "C:/Users/Tre' Jeter/Desktop/Java Projects/Projects/Peer-To-Peer Network/src/Common.cfg";
    
    private static volatile List<Integer> allPeerIDs = new ArrayList<>();
    private static volatile ConcurrentHashMap<Integer, Integer> peerIDToPort = new ConcurrentHashMap<>();
    private static volatile ConcurrentHashMap<Integer, InetAddress> peerIDToIP = new ConcurrentHashMap<>();
    private static volatile ConcurrentHashMap<Integer, Integer> peerIDToState = new ConcurrentHashMap<>();
    private static volatile int numPreferredNeighbors = -1;
    private static volatile int unchokingInterval = -1;
    private static volatile int optimisticUnchokeInterval = -1;
    private static volatile String fileName = "";
    private static volatile int fileSize = -1;
    private static volatile int pieceSize = -1;

    public static List<Integer> getAllPeerIDs() 
    {
        if(!allPeerIDs.isEmpty())
        {
            return new ArrayList<>(allPeerIDs);
        }
        List<Integer> peerIDList = new ArrayList<>();
        Scanner scan = null;
        //Return list of client IDs (i.e. 1001, 1002, 1003, etc.)
        try 
        {
            int peerID;
            scan = new Scanner(new File(pathToPeerInfoCfg));
            String info = "";
            while(scan.hasNextLine()) 
            {
                info = scan.nextLine();
                String[] placeHolder = info.split("\\s+");
                peerID = Integer.parseInt(placeHolder[0]);

                peerIDList.add(peerID);
            }
            scan.close();
            allPeerIDs = peerIDList;
        } 
        catch(Exception e) 
        {
            throw new RuntimeException("Error reading config file in getAllPeerIDs:\n" + e.getStackTrace());
        }
        finally 
        {
            if(scan != null)
            {
            	scan.close();
            }
        }
        System.out.println("Read all peer IDs from the config file");
        return new ArrayList<>(allPeerIDs);
    }

    public static InetAddress getIPFromPeerID(int peerID) 
    {
        if(peerIDToIP.containsKey(peerID))
        {
            return peerIDToIP.get(peerID);
        }
        Scanner scan = null;
        try 
        {
            //Given a peerID, return the IP of that client (i.e. peerID = 1001, return IP of client 1001)
            scan = new Scanner(new File(pathToPeerInfoCfg));
            String info = "";
            while(scan.hasNextLine()) 
            {
                info = scan.nextLine();
                String[] placeHolder = info.split("\\s+");
                peerIDToIP.put(Integer.parseInt(placeHolder[0]), InetAddress.getByName(placeHolder[1]));
            }
            scan.close();
        } 
        catch(Exception e) 
        {
            throw new RuntimeException("Error reading config file in getIPFromPeerID:\n" + e.getStackTrace());
        }
        finally 
        {
            if(scan != null)
            {
            	scan.close();
            }
        }
        System.out.println("Assigned all IPs for each peer from config file");
        return peerIDToIP.get(peerID);
    }

    public static int getPortFromPeerID(int peerID) 
    {
        if(peerIDToPort.containsKey(peerID))
        {
            return peerIDToPort.get(peerID);
        }
        Scanner scan = null;
        try 
        {
            //Given a peerID, return the IP of that client (i.e. peerID = 1001, return IP of client 1001)
            scan = new Scanner(new File(pathToPeerInfoCfg));
            String info = "";
            while(scan.hasNextLine()) 
            {
                info = scan.nextLine();
                String[] placeHolder = info.split("\\s+");
                peerIDToPort.put(Integer.parseInt(placeHolder[0]), Integer.parseInt(placeHolder[2]));

            }
            scan.close();
        } 
        catch(Exception e) 
        {
            throw new RuntimeException("Error reading config file in getIPFromPeerID:\n" + e.getStackTrace());
        }
        finally 
        {
            if(scan != null)
            {
            	scan.close();
            }
        }
        System.out.println("Assigned all ports for each peer from config file");
        return peerIDToPort.get(peerID);
    }

    public static int getStateFromPeerID(int peerID) 
    {
        if(peerIDToState.containsKey(peerID))
        {
            return peerIDToState.get(peerID);
        }
        System.out.println("Beginning to read state from config file...");

        Scanner scan = null;
        try 
        {
            // Given a peerID, return the IP of that client (i.e. peerID = 1001, return IP of client 1001)
            scan = new Scanner(new File(pathToPeerInfoCfg));
            String info = "";
            while(scan.hasNextLine()) 
            {
                info = scan.nextLine();
                String[] placeHolder = info.split("\\s+");
                peerIDToState.put(Integer.parseInt(placeHolder[0]), Integer.parseInt(placeHolder[3]));
            }
            scan.close();
        } 
        catch(Exception e) 
        {
            throw new RuntimeException("Error reading config file in getIPFromPeerID:\n" + e.getStackTrace());
        }
        finally 
        {
            if(scan != null)
            {
            	scan.close();
            }
        }
        System.out.println("Assigned all file possessions (states) for each peer from config file");
        return peerIDToState.get(peerID);
    }

    public static int getNumPreferredNeighbors() 
    {
        if(numPreferredNeighbors != -1)
        {
            return numPreferredNeighbors;
        }
        Scanner scan = null;
        try 
        {
            //Get number of preferred neighbors from Common.cfg
            int newNumPreferredNeighbors = 0;
            scan = new Scanner(new File(pathToCommonCfg));
            String file = scan.nextLine();
            String[] neighbors = file.split("\\s+");
            newNumPreferredNeighbors = Integer.parseInt(neighbors[1]);

            scan.close();
            numPreferredNeighbors = newNumPreferredNeighbors;
        } 
        catch(Exception e) 
        {
            throw new RuntimeException("Error reading config file:\n" + e.getMessage() + "\n" + e.getStackTrace());
        }
        finally 
        {
            if(scan != null)
            {
            	scan.close();
            }
        }
        System.out.println("Set number of preferred neighbors to " + numPreferredNeighbors + " from config file");
        return numPreferredNeighbors;
    }

    public static int getUnchokingInterval() 
    {
        if(unchokingInterval != -1)
        {
            return unchokingInterval;
        }
        Scanner scan = null;
        try 
        {
            //Get the unchoking interval from Common.cfg
            int newUnchokingInterval = 0;
            scan = new Scanner(new File(pathToCommonCfg));
            scan.nextLine();
            String file = scan.nextLine();
            String[] unchoke = file.split("\\s+");
            newUnchokingInterval = Integer.parseInt(unchoke[1]);

            scan.close();
            unchokingInterval = newUnchokingInterval;
        } 
        catch(Exception e) 
        {
            throw new RuntimeException("Error reading config file:\n" + e.getMessage() + "\n" + e.getStackTrace());
        }
        finally 
        {
            if(scan != null)
            {
            	scan.close();
            }
        }
        System.out.println("Set nchoking interval to " + unchokingInterval + " from config file");
        return unchokingInterval;
    }

    public static int getOptimisticUnchokingInterval() 
    {
        if(optimisticUnchokeInterval != -1)
        {
            return optimisticUnchokeInterval;
        }
        Scanner scan = null;
        try 
        {
            //Get the optimistic unchoking interval from Common.cfg
            int newOptimisticUnchokeInterval = 0;
            scan = new Scanner(new File(pathToCommonCfg));
            for(int i = 0; i <= 1; i++) 
            {
                scan.nextLine();
            }
            String file = scan.nextLine();
            String[] optimisticUnchoke = file.split("\\s+");
            newOptimisticUnchokeInterval = Integer.parseInt(optimisticUnchoke[1]);

            scan.close();
            optimisticUnchokeInterval = newOptimisticUnchokeInterval;
        } 
        catch(Exception e) 
        {
            throw new RuntimeException("Error reading config file:\n" + e.getMessage() + "\n" + e.getStackTrace());
        }
        finally 
        {
            if(scan != null)
            {
            	scan.close();
            }
        }
        System.out.println("Set optimistic unchoking interval to " + optimisticUnchokeInterval + " from config file");
        return optimisticUnchokeInterval;
    }

    public static String getFileName() 
    {
        if(!fileName.equals(""))
        {
            return fileName;
        }
        Scanner scan = null;
        try 
        {
            //Get the name of the file from Common.cfg
            scan = new Scanner(new File(pathToCommonCfg));
            for(int i = 0; i <= 2; i++) 
            {
                scan.nextLine();
            }
            String newFileName = scan.nextLine();
            String[] unchoke = newFileName.split("\\s+");
            newFileName = unchoke[1];

            scan.close();
            
            fileName = newFileName;
        } 
        catch(Exception e) 
        {
            throw new RuntimeException("Error reading config file:\n" + e.getMessage() + "\n" + e.getStackTrace());
        }
        finally 
        {
            if(scan != null)
            {
                scan.close();
            }
        }
        System.out.println("Retrieved filename " + fileName + " from config file");
        return fileName;
    }

    public static int getFileSize() 
    {
        if(fileSize != -1)
        {
            return fileSize;
        }
        Scanner scan = null;
        try 
        {
            //Get the specified size of the file from Common.cfg
            int newFileSize = 0;
            scan = new Scanner(new File(pathToCommonCfg));
            for(int i = 0; i <= 3; i++) 
            {
                scan.nextLine();
            }
            String file = scan.nextLine();
            String[] size = file.split("\\s+");
            newFileSize = Integer.parseInt(size[1]);

            scan.close();
            fileSize = newFileSize;
        } 
        catch(Exception e) 
        {
            throw new RuntimeException("Error reading config file:\n" + e.getMessage() + "\n" + e.getStackTrace());
        }
        finally 
        {
            if(scan != null)
            {
                scan.close();
            }
        }
        System.out.println("Retrieved file size " + fileSize + " from config file");
        return fileSize;
    }

    public static int getPieceSize() 
    {
        if(pieceSize != -1)
        {
            return pieceSize;
        }
        Scanner scan = null;
        try 
        {
            //Get the specified piece size of the file from Common.cfg
            int newPieceSize = 0;
            scan = new Scanner(new File(pathToCommonCfg));
            for(int i = 0; i <= 4; i++) 
            {
                scan.nextLine();
            }
            String file = scan.nextLine();
            String[] size = file.split("\\s+");
            newPieceSize = Integer.parseInt(size[1]);

            scan.close();
            pieceSize = newPieceSize;
        } 
        catch(Exception e) 
        {
            throw new RuntimeException("Error reading config file:\n" + e.getMessage() + "\n" + e.getStackTrace());
        }
        finally 
        {
            if(scan != null)
            {
            	scan.close();
            }
        }
        System.out.println("Retrieved piece size " + pieceSize + " from config file");
        return pieceSize;
    }
}
