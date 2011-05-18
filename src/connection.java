/* Java Chat
 * (C) 2010 
 * Mathias Rabe, Benjamin Lesky, Sebastian Stock, Michael Götz, Andreas Gröger
 *
 * This code is licenced under the GPL v3.
 */

import java.net.*;
import java.io.*;
import java.util.*;

class connection extends Thread
{
	protected Socket client;
	protected BufferedReader in;
	protected PrintStream out;
	protected server server;
	protected String nickname;
	protected int uid;
	private volatile Thread connect;

	/**
	 * Initialisierung
	 * baut Streams zum client auf und generiert zufälligen Benutzernamen
	 *  
	 * @param server Eltern-Klasse
	 * @param client socket zum client
	 */
	public connection(server server, Socket client, int uid)
	{
		this.server=server;
		this.client=client;
		this.uid = uid;

		try
		{
			in = new BufferedReader(new InputStreamReader( client.getInputStream() ));
			out = new PrintStream(client.getOutputStream());
		} catch (IOException e)
		{
			try { client.close(); } catch (IOException e2) {} ;
			System.err.println("Fehler beim Erzeugen der Streams: " + e);
			return;
		}
		
		// zufälligen Benutzernamen erstellen und testen ob schon vorhanden
		nickname = "USER" + uid;
		
		server.sendGlobalServerMsg("Benutzer " + nickname + " hat den Server betreten");

		connect = new Thread(this);
		connect.start();
	}
	
	/**
	 * Schickt Servernachricht an Benutzer
	 * @param line
	 */
	public void sendServerMsg(String line)
	{
		//out.println("Server: "+ line);
		String header = "<uid=0>";
		out.println(header + line);
	}
	
	/**
	 * liefert Benutzernamen
	 * @return
	 */
	public String getNickname()
	{
		return nickname;
	}
	
	/**
	 * Liefert uid
	 * @return
	 */
	public int getUid()
	{
		return uid;
	}
	
	/**
	 * beendet Verbindung und Thread
	 */
	public void done()
	{
		// Zeugs um Thread zu stoppen
		System.out.println("Stop the connection Thread of " + nickname);

		try {
			client.close();
		} catch (IOException e) {
			System.err.println("Fehler beim Schließen der Verbindung: " + e);
		}
		
		server.removeConnection(this);
		
		Thread moribund = connect;
		connect = null;
		moribund.interrupt();
	}
	
	/**
	 * Filtert einen String nach evtl. vorhandenen Befehlen
	 * @param line String mit zu überprüfender Optionen - Slash am Anfang entfernen!
	 */
	private void filterOptions( String line)
	{
		if(line.startsWith("name ")) {
			String newnick = line.substring(5);
			if(newnick.contains(" ")) {
				sendServerMsg("Name Darf keine Leerzeichen enthalten");
			} else if (server.userExists( newnick ) || newnick.equals("Server") ) {
				sendServerMsg("Name schon vergeben");
			} else {
				server.sendGlobalServerMsg("Der Benutzer " + nickname + " hat seinen Namen in " + newnick + " geändert");
				nickname = newnick;
				sendServerMsg("Ihr Benutzername lautet " + nickname);
				System.out.println("Nickname: " + nickname);
			}
		} else if(line.startsWith("msg ")) {
			// Mitteilung Aufteilen in Benutzer und Nachricht
			String[] splittedString = line.substring(4).split("[\\s]", 2);
			if (splittedString.length < 2) {
				sendServerMsg("Nicht genügend Argumente für eine Nachricht");
				return;
			}
			if (server.userExists(splittedString[0])) {
				server.broadcast(splittedString[1], splittedString[0], uid);
			} else {
				sendServerMsg("Benutzer " + splittedString[0] + " nicht gefunden");
			}
		} else if(line.startsWith("who")) {
			Vector<String> namelist = server.getUserNames();
			if(namelist.isEmpty()) {
				sendServerMsg("Keine Benutzer online");
				return;
			}
			sendServerMsg(namelist.size() + " Personen im Chat:");
			String names = new String();
			for( int i = 0; i < namelist.size(); i++ ) {
				names += namelist.elementAt(i);
				if ( i < namelist.size() - 1 ) {
					names += ", ";
					if ( names.length() > 40 ) {
						// neue Zeile anfangen
						sendServerMsg(names);
						names = "";
					}
				}
			}
			sendServerMsg(names);
		} else if(line.startsWith("quit")) {
			this.done();
		} else if(line.startsWith("help")) {
			sendServerMsg("Serverbefehle:");
			sendServerMsg("/name benutzername");
			sendServerMsg("   neuen Benutzernamen setzen");
			sendServerMsg("/who");
			sendServerMsg("   zeigt alle Benutzer an");
			sendServerMsg("/msg benutzer nachricht");
			sendServerMsg("   schickt nur an benutzer die nachricht");
			sendServerMsg("/setlang de");
			sendServerMsg("   (de-)aktiviert übersetzung");
			sendServerMsg("/quit");
			sendServerMsg("   Server verlassen und Client beenden");
			sendServerMsg("/help");
			sendServerMsg("   zeigt diese Hilfe an");
		} else {
			sendServerMsg("Unbekannter Befehl");
		}
	}

	/**
	 * Thread-Rutine
	 * Wartet auf Anfragen vom Client
	 */
	public void run()
	{
		String line;
		Thread thisThread = Thread.currentThread();
		
		sendServerMsg("Willkommen auf dem Server");

		while(connect == thisThread)
		{
			try {
				line=in.readLine();
			} catch (IOException e) {
				System.out.println("Fehler: " + e);
				this.done(); // funktioniert das hier?
				break; // sicher ist sicher?
			}
			
			if(line == null) {
				System.out.println("Verbindungsfehler - beende Verbindung");
				this.done();
			} else if( line.startsWith("/") ) {
				filterOptions( line.substring(1) );
			} else {
				System.out.println(nickname + ": " + line); //könnte gelöscht werden
				server.broadcast(nickname + ": " + line, uid);
			}
		}
	}
}