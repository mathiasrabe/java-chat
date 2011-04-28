import java.net.*;
import java.io.*;
import java.util.*;

public class server implements Runnable
{
	public static int port = 8765;
	protected ServerSocket listen;
	protected Vector<connection> connections;
	private volatile Thread connect;

	public server()
	{
		System.out.println("Server wird auf Port " + port + " gestartet ...");
        
		try
		{
			listen = new ServerSocket(port);
		} catch (IOException e)
		{
			System.err.println("Fehler beim Erzeugen der Sockets: "+e);
			System.exit(1);
		}
		
		System.out.println("Verfügbare Netzwerkschnittstellen:");
		showNIC();

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
		if (args.length > 0) {
			for ( int i = 0; i < args.length - 1; i++) { // -1 da zwei argumente vorhanden sein müssen: flag und option
				if( args[i].equals("--port")) {
					try {
						port = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException e) {
						System.err.println("Portnummer ist kein Integer");
						System.exit(1);
					}
				}
			}
		}
		
		new server();
	}
	
	public static void showNIC() {
		// http://www.informatik-blog.net/2009/01/28/informationen-der-netzwerkkarten-auslesen/
		try {
			Enumeration<NetworkInterface> interfaceNIC = NetworkInterface.getNetworkInterfaces();
	        // Alle Schnittstellen durchlaufen
			while (interfaceNIC.hasMoreElements()) {
	            //Elemente abfragen und ausgeben
	            NetworkInterface n = interfaceNIC.nextElement();
	            System.out.println(String.format("Netzwerk-Interface: %s (%s)", n.getName(), n.getDisplayName()));
	            // Adressen abrufen
	            Enumeration<InetAddress> addresses = n.getInetAddresses();
	            // Adressen durchlaufen
	            while (addresses.hasMoreElements()) {
	                InetAddress address = addresses.nextElement();
	                System.out.println(String.format("- %s", address.getHostAddress()));
	            }
	        }
            System.out.println();
	    } catch (SocketException e) {
	        e.printStackTrace();
	    }
	}
	
	/**
	 * 
	 * @param msg zu senende Nachricht
	 * @param username Benutzer an den die Nachricht geschickt werden soll
	 */
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

	/**
	 * 
	 * @param msg Sende msg an alle Teilnehmer
	 */
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
	
	public void sendGlobalServerMsg(String msg)
	{
		int i;
		connection you;
		
		for (i=0; i<connections.size(); i++) {
			you = (connection) connections.elementAt(i);
			you.sendServerMsg(msg);
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
		sendGlobalServerMsg("Benutzer " + c.getNickname() + " hat den Server verlassen");
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