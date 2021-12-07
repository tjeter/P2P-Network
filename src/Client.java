import java.net.*;
import java.util.concurrent.atomic.AtomicLong;
import java.io.*;

public class Client extends Thread 
{
	volatile Socket connection; //Socket connect to the server
	volatile ObjectOutputStream out; //Stream write to the socket
	volatile ObjectInputStream in; //Stream read from the socket

	volatile int selfClientID = -1; //This is own client's ID
	public volatile int otherPeerID = -1; //ID of peer that self is connecting to

	volatile AtomicLong cumulativeDownloadTimeNanoseconds = new AtomicLong(1);
	volatile AtomicLong cumulativeBytesDownloaded = new AtomicLong(1);

	volatile boolean shouldInitiateHandshake = false;

	public Client(Socket connection, boolean shouldInitiateHandshake) 
	{
		this.selfClientID = PeerProcess.selfClientID;
		this.connection = connection;
		this.shouldInitiateHandshake = shouldInitiateHandshake;
	}

	@Override
	public void run() 
	{
		try 
		{
			//Initialize input and output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());

			try 
			{
				if(shouldInitiateHandshake) 
				{
					System.out.println(Logger.getTimestamp() + ": Initiated handhshake with " + otherPeerID);
					sendMessage(Handshake.constructHandshakeMessage(selfClientID));
					System.out.println(Logger.getTimestamp() + ": Waiting for handshake response from " + otherPeerID);
					this.otherPeerID = waitForAndHandleHandshake();
					Logger.logTcpConnectionTo(this.otherPeerID);
				} 
				else 
				{
					System.out.println(Logger.getTimestamp() + ": Waiting for handshake initiation from " + (otherPeerID == -1 ? "unknown peer" : otherPeerID));
					this.otherPeerID = waitForAndHandleHandshake();
					System.out.println(Logger.getTimestamp() + ": Received handshake initiation from " + otherPeerID + ". Sending handhshake reponse...");
					sendMessage(Handshake.constructHandshakeMessage(selfClientID));
					Logger.logTcpConnectionFrom(this.otherPeerID);
					PeerProcess.connectionFromNewPeer(otherPeerID, this);
				}
				System.out.println(Logger.getTimestamp() + ": Peer " + otherPeerID + " has finished handshaking!");

				System.out.println(Logger.getTimestamp() + ": Sending BITFIELD message to " + otherPeerID);
				sendMessage(Bitfield.constructBitfieldMessage());

				while(PeerProcess.isRunning) 
				{
					try 
					{
						try 
						{
							handleAnyMessage(receiveMessage());
						}
						catch(EOFException eofException) 
						{
							
						}
					}
					catch(Exception e) 
					{
						e.printStackTrace();
						break;
					}

				}
			} 
			catch(ClassNotFoundException classnot) 
			{
				System.out.println(Logger.getTimestamp() + ": Data received in unknown format");
				classnot.printStackTrace();
			} 
			catch(Exception e) 
			{
				System.out.println(Logger.getTimestamp() + ": Exception in second big try statement");
				e.printStackTrace();
			}
		} 
		catch(IOException ioException) 
		{
			System.out.println(Logger.getTimestamp() + ": Disconnect with Client " + otherPeerID);
		} 
		finally 
		{
			//Close connections
			try 
			{
				in.close();
				out.close();
				connection.close();
			} 
			catch(IOException ioException) 
			{
				System.out.println(Logger.getTimestamp() + ": Disconnect with Client " + otherPeerID);
			}
			System.out.println(Logger.getTimestamp() + ": Connection to " + otherPeerID + " closed.");
		}
		System.out.println(Logger.getTimestamp() + ": Client process terminated.");
	}

	//Send a message to the output stream
	void sendMessage(byte[] msg) 
	{
		try 
		{
			//Stream write the message
			out.writeObject(msg);
			out.flush();
		} 
		catch(IOException ioException) 
		{
			ioException.printStackTrace();
		}
	}

	void handleAnyMessage(byte[] msg) 
	{
		int messageType = ActualMessageHandler.getMsgType(msg);

		switch(messageType) 
		{
			case 0:
				System.out.println(Logger.getTimestamp() + ": Message from " + otherPeerID + ": CHOKE");
				ChokeHandler.receivedChokeMessage(otherPeerID);
				break;
			case 1:
				System.out.println(Logger.getTimestamp() + ": Message from " + otherPeerID + ": UNCHOKE");
				ChokeHandler.receivedUnchokeMessage(otherPeerID);
				break;
			case 2:
				System.out.println(Logger.getTimestamp() + ": Message from " + otherPeerID + ": INTEREST");
				InterestHandler.receivedInterestedMessage(otherPeerID);
				break;
			case 3:
				System.out.println(Logger.getTimestamp() + ": Message from " + otherPeerID + ": NON-INTEREST");
				InterestHandler.receivedUninterestedMessage(otherPeerID);
				break;
			case 4:
				System.out.println(Logger.getTimestamp() + ": Message from " + otherPeerID + ": HAVE");
				HaveHandler.receivedHaveMessage(otherPeerID, ActualMessageHandler.extractPayload(msg));
				break;
			case 5:
				System.out.println(Logger.getTimestamp() + ": Message from " + otherPeerID + ": BITFIELD");
				Bitfield.receivedBitfieldMessage(otherPeerID, ActualMessageHandler.extractPayload(msg));
				break;
			case 6:
				System.out.println(Logger.getTimestamp() + ": Message from " + otherPeerID + ": REQUEST");
				RequestHandler.receivedRequestMessage(otherPeerID, ActualMessageHandler.extractPayload(msg));
				break;
			case 7:
				System.out.println(Logger.getTimestamp() + ": Message from " + otherPeerID + ": PIECE");
				PieceHandler.receivedPieceMessage(otherPeerID, ActualMessageHandler.extractPayload(msg));
				break;
			default:
				System.out.println("FATAL: Message received has invalid type");
				throw new RuntimeException("FATAL: Message received has invalid type");
		}
	}

	byte[] receiveMessage() throws IOException, ClassNotFoundException 
	{
		long startTime = System.nanoTime();
		byte[] obj = (byte[]) in.readObject();
		long endTime = System.nanoTime();

		cumulativeDownloadTimeNanoseconds = new AtomicLong(cumulativeDownloadTimeNanoseconds.get() + endTime - startTime);
		cumulativeBytesDownloaded = new AtomicLong(cumulativeBytesDownloaded.get() + obj.length);

		return obj;
	}

	int getPortFromPeerID(int id) 
	{
		return ConfigReader.getPortFromPeerID(id);
	}

	String getIPFromPeerID(int id) 
	{
		return ConfigReader.getIPFromPeerID(id).toString();
	}

	void sendBitfieldMessage() 
	{
		Bitfield.constructBitfieldMessage();
	}

	//KiloBytes per second
	public double getDownloadRateInKBps() 
	{
		return ((double) cumulativeBytesDownloaded.get() / 1000.0 * 1000000000
				/ (double) cumulativeDownloadTimeNanoseconds.get());
	}

	public void unchokePeer() 
	{
		byte[] unchokeMsg = ChokeHandler.constructChokeMessage(otherPeerID, false);
		ChokeHandler.unchokePeer(otherPeerID);
		System.out.println(Logger.getTimestamp() + ": Sending UNCHOKE message to " + otherPeerID);
		sendMessage(unchokeMsg);
	}

	public void chokePeer() 
	{
		byte[] chokeMsg = ChokeHandler.constructChokeMessage(otherPeerID, true);
		ChokeHandler.chokePeer(otherPeerID);
		System.out.println(Logger.getTimestamp() + ": Sending CHOKE message to " + otherPeerID);
		sendMessage(chokeMsg);
	}

	public synchronized int waitForAndHandleHandshake() throws ClassNotFoundException, IOException 
	{
		while(true) 
		{
			try 
			{
				byte[] message = receiveMessage();
				if(Handshake.isHandshakeMessage(message)) 
				{
					return Handshake.receivedHandshakeResponseMessage(message);
				}
			} 
			catch (EOFException e) 
			{
				
			}
		}
	}
}