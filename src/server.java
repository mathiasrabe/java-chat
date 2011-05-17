import java.net.*;
import java.io.*;
import java.util.*;

public class server implements Runnable
{
	public static int port = 8765;
	protected ServerSocket listen;
	protected Vector<connection> connections;
	private volatile Thread connect;

	/**
	 * Initialisierung
	 * Port öffnen, Netzwerkschnittstellen anzeigen & Thread starten
	 */
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

	/**
	 * Thread-Rutine
	 * Eingehenden Verbindungen einen connection-thread zuweisen
	 */
	public void run()
	{
		try
		{
			while(true)
			{
				Socket client=listen.accept();
				
				System.out.println("Eingehende Verbindung...");
				
				int uid;
				do {
					uid = (int) (Math.random() * 1000);
				} while( userExists(uid) );

				connection c = new connection(this, client, uid);
				connections.addElement(c);
			}
		} catch (IOException e)
		{
			System.err.println("Fehler beim Warten auf Verbindungen: "+e);
			System.exit(1);
		}
	}

	/**
	 * main-schleife
	 * Startargumente überprüfen
	 * initalisierung aufrufen
	 * 
	 * @param args
	 */
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
					if (port < 1 || port > 65535) {
						System.err.println("Portnummer ist ungültig");
						System.exit(1);
					}
				}
			}
		}
		
		new server();
	}
	
	/**
	 * Netzwerkschnittstellen auflisten
	 * danke an:
	 * http://www.informatik-blog.net/2009/01/28/informationen-der-netzwerkkarten-auslesen/
	 */
	public static void showNIC() {
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
	 * Nachricht an bestimmten User schicken
	 * @param msg zu senende Nachricht
	 * @param username Benutzer an den die Nachricht geschickt werden soll
	 * @param uid uid des Benutzers der die Nachricht verschickt
	 */
	public void broadcast(String msg, String username, int uid)
	{
		int i;
		connection you;
		
		String header =  "<uid=" + uid + ">";
		msg = header + "Private Nachricht von " + username + ": " + msg;
		
		for (i=0; i<connections.size(); i++) {
			you = (connection) connections.elementAt(i);
			if ( you.getNickname().equals(username) ) {
				you.out.println(msg);
				break;
			}
		}
	}

	/**
	 * Nachricht an alle User schicken
	 * @param msg
	 * @param uid uid des Benutzers der die Nachricht verschickt
	 */
	public void broadcast(String msg, int uid)
	{
		int i;
		connection you;
		
		String header =  "<uid=" + uid + ">";
		msg = header + msg;

		for (i=0; i<connections.size(); i++) {
			//System.out.println("Nachricht verschicken!");
			you = (connection) connections.elementAt(i);
			if (you.getUid() == uid)
				continue;
			you.out.println(msg);
		}
	}
	
	/**
	 * Servernachricht an alle User schicken
	 * @param msg
	 */
	public void sendGlobalServerMsg(String msg)
	{
		int i;
		connection you;
		
		for (i=0; i<connections.size(); i++) {
			you = (connection) connections.elementAt(i);
			you.sendServerMsg(msg);
		}
	}
	
	/**
	 * Liefert alle Benutzernamen
	 * @return Vector of usernames
	 */
	public Vector<String> getUserNames()
	{
		int i;
		Vector<String> names = new Vector<String>( connections.size() );
		
		for (i=0; i < connections.size(); i++) {
			names.addElement( (String) connections.elementAt(i).nickname );
		}
		
		return names;
	}
	
	/**
	 * Löscht Connectionthread aus Liste
	 * @param c
	 */
	public void removeConnection(connection c)
	{
		connections.removeElement(c);
		sendGlobalServerMsg("Benutzer " + c.getNickname() + " hat den Server verlassen");
	}
	
	/**
	 * testet ob Benutzer existiert
	 * @param uid zu testende uid des Benutzers
	 * @return true if user exists or false
	 */
	public boolean userExists(int uid)
	{
		if (uid == 0)
			return true; // 0 ist für Server reserviert
		
		int i;
		connection you;

		for (i=0; i<connections.size(); i++)
		{
			you = (connection) connections.elementAt(i);
			if( you.getUid() == uid ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * testet ob Benutzer existiert
	 * @param name zu testender Benutzername
	 * @return true if user exists or false
	 */
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