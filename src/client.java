/* Java Chat
 * (C) 2010 
 * Mathias Rabe, Benjamin Lesky, Sebastian Stock, Michael Götz, Andreas Gröger
 *
 * This code is licenced under the GPL v3.
 */

import java.net.*;
import java.util.*; //Lib für Eingabe über Konsole & Vector

//import java.util.Vector;
import java.io.*;

public class client implements Runnable
{
	public static int port = 8765;
	public static String ip;
	protected Socket socket;
	protected Scanner in;
	private volatile Thread connect;
	private String consoleinput;
	public String username;
	private TranslationEngine translateng;

	
	/**
	 * Initialisierung
	 * Verbindung aufbauen
	 */
	public client()
	{
		in = new Scanner(System.in);
		System.out.println("Bitte geben Sie eine IP-Adresse ein:");
		ip = in.nextLine();
		// TODO Die Variable input muss auf Richtigkeit überprüft werden!
		System.out.println("Bitte geben Sie Ihren Nickname ein:");
		do {
			if (username != null && username.contains(" "))
				System.out.println("In Ihrem Benutzernamen darf kein Leerzeichen enthalten sein." +
						           " Bitte geben Sie erneut ein:");
			username = in.nextLine();
		} while( username.isEmpty() || username.contains(" ") );
		
		try
		{
			socket = new Socket( ip, port);
		} catch (IOException e)
		{
			System.err.println("Verbindung fehlgeschlagen:"+ e);
			System.exit(1);
		}
		
		translateng = new TranslationEngine();
		connect = new Thread(this);
		connect.start();
	}
	
	/**
	 * Beenden
	 */
	public void done()
	{
		// Zeugs um Thread zu stoppen
		Thread moribund = connect;
		connect = null;
		moribund.interrupt();
	}
	
	/**
	 * Wartet auf Konsoleneingabe
	 */
	public void run()
	{		
		connection_client c = new connection_client(socket, this, username);
		
		// Zeugs um Thread zu stoppen
		// siehe auch http://download.oracle.com/javase/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
		Thread thisThread = Thread.currentThread();
		while (connect == thisThread) {
			//Abfrage der Konsoleneingabe
			consoleinput = in.nextLine();
			
			if(consoleinput.matches("/quit")) {
				//alle Threads beenden						
				c.out.println( consoleinput );
				c.done();
				this.done();
			}
			if (consoleinput.startsWith("/setlang")) {
				//Übersetzung einschalten
				if (consoleinput.length() < 9) {
					// erhalte aktuelle Sprache
					String clang = translateng.getClientLanguage();
					if (clang == null) {
						display("Keine Übersetzung aktiviert");
					} else {
						display("Übersetzung nach " + clang + " aktiviert");
					}
				} else {
					//neue Sprache speichern
					String lang = consoleinput.substring(9);
					if (lang.equals("off")) {
						// Übersetzung ausschalten
						translateng.setClientLanguage(null);
						display("Übersetzung deaktiviert");
						continue;
					}
					System.out.println(lang);
					translateng.setClientLanguage(lang);
					if (translateng.getClientLanguage() == null) {
						display("Sprache " + lang + " nicht verfügbar");
					}
				}
			} else {
				c.out.println( consoleinput );
			}
		}
		System.out.println("bye bye ...");
	}
	
	/**
	 * Überprüfung der Startparameter
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
			//if( args[i].equals("--ip")) {
			//	ip = args[i+1];
			//}
		}
	}
		new client();
	}
	
	/**
	 * Zeige String in der Konsole, finde Optionen und übersetze ihn evtl.
	 * @param line
	 */
	public void display(String line)
	{
		int uid;
		
		//Optionen herausfiltern
		if (line.startsWith("<")) {
			int endindex = line.indexOf(">");
			String options = line.substring(1, endindex);
			//FIXME funktioniert nur wenn wirklich nur, und wirklich nur, die uid geschickt wird!
			int evenindex = options.indexOf("=");
			uid = Integer.parseInt( line.substring(evenindex+2, options.length()+1) ); //keine Ahnung warum +2 und +1 ...
			
			line = line.substring(endindex+1); // Header entfernen line.length()
			if (uid == 0) {
				line = "Server: " + line;
			}
		} else {
			uid = 0;
		}
		if ( translateng.isClientLanguageSet() ) { //Übersetzung!
			if ( !translateng.userExists(uid) ) {
				translateng.addUser(uid);
			}
			String newline = translateng.translate(uid, line);
			if (newline != null) {
				line = newline;
			}
		}
		
		System.out.println(line);
	}
}