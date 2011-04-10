import java.net.*;
import java.util.*; //Bib für Eingabe über Konsole & Vector

//import java.util.Vector;
import java.io.*;
//import java.awt.*;
//import java.applet.*;
//import java.awt.event.*;

public class client implements Runnable
{
	public static final int PORT = 8765;
	Socket socket;
	Scanner in;
	private volatile Thread connect;
	String consoleinput;
	String username;

	
	public client()
	{
		in = new Scanner(System.in);
		System.out.println("Bitte geben Sie eine IP-Adresse ein:");
		String input = in.nextLine();
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
			socket = new Socket( input, PORT);
		} catch (IOException e)
		{
			System.err.println("Verbindung fehlgeschlagen:"+ e);
			System.exit(1);
		}
		
		connect = new Thread(this);
		connect.start();
	}
	
	public void done()
	{
		// Zeugs um Thread zu stoppen
		Thread moribund = connect;
		connect = null;
		moribund.interrupt();
	}
	
	public void run()
	{		
		connection_client c = new connection_client(socket, this, username);
		
		// Zeugs um Thread zu stoppen
		// siehe auch http://download.oracle.com/javase/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
		Thread thisThread = Thread.currentThread();
		while (connect == thisThread) {
			//Abfrage der Konsoleneingabe
			consoleinput = in.nextLine();
						
			c.out.println( consoleinput );
			
			if(consoleinput.matches("/quit")) {
				//alle Threads beenden
				c.done();
				this.done();
			}
		}
		System.out.println("bye bye ...");
	}
	
	public static void main(String[] args)
	{
		new client();
	}
	
	public void display(String line)
	{
		if ( !line.startsWith(username) ) {
			//TODO Der Server sollte dann auch Daten schicken die mit 
			// dem Benutzernamen beginnen: USERNAME: hallo
			System.out.println(line);
		}
	}
}