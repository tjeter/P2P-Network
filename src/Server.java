import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server extends Thread 
{
	public static volatile int selfClientID = -1;
	public static volatile ServerSocket listener;

	@Override
	public void run() 
	{
		long startTime = System.currentTimeMillis();
		System.out.println(Logger.getTimestamp() + ": " + startTime + ": The server is running.");
		try 
		{
			listener = new ServerSocket(ConfigReader.getPortFromPeerID(selfClientID));
		} 
		catch(IOException e) 
		{
			e.printStackTrace();
		}

		try 
		{
			while(PeerProcess.isRunning) 
			{
				System.out.println(Logger.getTimestamp() + ": Waiting to accept peer connections...");
				Client c;
				try 
				{
					c = new Client(listener.accept(), false);
					System.out.println(Logger.getTimestamp() + ": Unknown peer made a TCP connection!");
					c.start();
				} 
				catch(IOException e) 
				{
					e.printStackTrace();
					break;
				}

				if(System.currentTimeMillis() > startTime + 120000) 
				{
					System.out.println(Logger.getTimestamp() + ": " + System.currentTimeMillis() + ": Stopping server on time.");
					break;
				}
			}
		} 
		finally 
		{
			try 
			{
				if(listener != null)
				{
					listener.close();
				}
			} 
			catch(IOException e) 
			{
				e.printStackTrace();
			}
			System.out.println(Logger.getTimestamp() + ": The server stopped.");
		} 
	}
}