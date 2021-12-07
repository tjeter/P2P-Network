import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class StartRemotePeers 
{
    private static final String scriptPrefix = "java -cp \".:jsch-0.1.54.jar\" PeerProcess ";

    public static class PeerInfo 
    {
        private String peerID;
        private String hostName;

        public PeerInfo(String peerID, String hostName) 
        {
            super();
            this.peerID = peerID;
            this.hostName = hostName;
        }

        public String getPeerID() 
        {
            return peerID;
        }

        public void setPeerID(String peerID) 
        {
            this.peerID = peerID;
        }

        public String getHostName() 
        {
            return hostName;
        }

        public void setHostName(String hostName) 
        {
            this.hostName = hostName;
        }
    }

    public static void main(String[] args) 
    {
        System.out.println(Logger.getTimestamp() + ": Starting remote peers...");

        ArrayList<PeerInfo> peerList = new ArrayList<>();

        //Change to your CISE username
        String ciseUser = "t.jeter"; 

        /*
         * Make sure the below peer hostnames and peerIDs match those in PeerInfo.cfg in
         * the remote CISE machines. Also make sure that the peers which have the file
         * initially have it under the 'peer_[peerID]' folder.
         */

        List<Integer> allPeerIDs = ConfigReader.getAllPeerIDs();

        for(int peerID : allPeerIDs) 
        {
            peerList.add(new PeerInfo("" + peerID, ConfigReader.getIPFromPeerID(peerID).getHostAddress()));
        }

        for(PeerInfo remotePeer : peerList) 
        {
            try 
            {
                System.out.println(Logger.getTimestamp() + ": Starting peer: " + remotePeer.peerID);
                JSch jsch = new JSch();
                /*
                 * Give the path to your private key. Make sure your public key is already
                 * within your remote CISE machine to ssh into it without a password. Or you can
                 * use the corressponding method of JSch which accepts a password.
                 */
                jsch.addIdentity("~/.ssh/id_rsa", "");
                System.out.println(Logger.getTimestamp() + ": Getting session...");
                Session session = jsch.getSession(ciseUser, remotePeer.getHostName(), 22);
                System.out.println(Logger.getTimestamp() + ": Session obtained.");
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);

                System.out.println(Logger.getTimestamp() + ": Connecting...");
                session.connect();
                System.out.println(Logger.getTimestamp() + ": Connected.");

                System.out.println(Logger.getTimestamp() + ": Session to peer# " + remotePeer.getPeerID() + " at " + remotePeer.getHostName());

                Channel channel = session.openChannel("exec");
                System.out.println(Logger.getTimestamp() + ": remotePeerID" + remotePeer.getPeerID());
                ((ChannelExec) channel).setCommand(scriptPrefix + remotePeer.getPeerID());

                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);

                InputStream input = channel.getInputStream();
                channel.connect();

                System.out.println(Logger.getTimestamp() + ": Channel Connected to peer# " + remotePeer.getPeerID() + " at "
                        + remotePeer.getHostName() + " server with commands");

                (new Thread() 
                {
                    @Override
                    public void run() 
                    {
                        InputStreamReader inputReader = new InputStreamReader(input);
                        BufferedReader bufferedReader = new BufferedReader(inputReader);
                        String line = null;

                        try 
                        {
                            while((line = bufferedReader.readLine()) != null) 
                            {
                                System.out.println(remotePeer.getPeerID() + ">:" + line);
                            }
                            bufferedReader.close();
                            inputReader.close();
                        } 
                        catch(Exception ex) 
                        {
                            System.out.println(remotePeer.getPeerID() + " Exception >:");
                            ex.printStackTrace();
                        }
                        channel.disconnect();
                        session.disconnect();
                    }
                }).start();
                System.out.println(Logger.getTimestamp() + ": Done with peer: " + remotePeer.peerID);
            } 
            catch(JSchException e) 
            {
                System.out.println(remotePeer.getPeerID() + " JSchException >:");
                e.printStackTrace();
            } 
            catch(IOException ex) 
            {
                System.out.println(remotePeer.getPeerID() + " Exception >:");
                ex.printStackTrace();
            }
        }
        System.out.println(Logger.getTimestamp() + ": ***** Finished starting remote peers! *****");
    }
}