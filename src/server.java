import java.net.*;
import java.io.*;
import java.util.*;

public class server implements Runnable
{
	public static final int PORT = 8765;
	protected ServerSocket listen;
	protected Vector<connection> connections;
	private volatile Thread connect;

	public server()
	{
		System.out.println("Server wird gestartet ...");
        
		try
		{
			listen = new ServerSocket(PORT);
		} catch (IOException e)
		{
			System.err.println("Fehler beim Erzeugen der Sockets: "+e);
			System.exit(1);
		}

		connections = new Vector<connection>();

		connect = new Thread(this);
		connect.start();
	}
	
	//public void done()

	public void run()
	{
		try
		{
			while(true)
			{
				Socket client=listen.accept();
				
				System.out.println("Eingehende Verbindung...");

				connection c = new connection(this, client);
				connections.addElement(c);
			}
		} catch (IOException e)
		{
			System.err.println("Fehler beim Warten auf Verbindungen: "+e);
			System.exit(1);
		}
	}

	public static void main(String[] args)
	{
		new server();
	}
	
	public void broadcast(String msg, String username)
	{
		int i;
		connection you;
		
		msg = "Private Nachricht von " + username + ": " + msg;
		
		for (i=0; i<connections.size(); i++) {
			you = (connection) connections.elementAt(i);
			if ( you.nickname.equals( username ) ) {
				you.out.println(msg);
				break;
			}
		}
	}

	public void broadcast(String msg)
	{
		int i;
		connection you;

		for (i=0; i<connections.size(); i++) {
			//System.out.println("Nachricht verschicken!");
			you = (connection) connections.elementAt(i);
			you.out.println(msg);
		}
	}
	
	public Vector<String> getUserNames()
	{
		int i;
		Vector<String> names = new Vector<String>( connections.size() );
		
		for (i=0; i < connections.size(); i++) {
			names.addElement( (String) connections.elementAt(i).nickname );
		}
		
		return names;
	}
	
	public void removeConnection(connection c)
	{
		connections.removeElement(c);
	}
	
	public boolean userExists(String name)
	{
		int i;
		connection you;

		for (i=0; i<connections.size(); i++)
		{
			you = (connection) connections.elementAt(i);
			if( you.getNickname().equals(name) ) {
				return true;
			}
		}
		return false;
	}
}