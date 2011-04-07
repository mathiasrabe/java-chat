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
	//DataInputStream in;
	//PrintStream out;
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
		} while( username == null || username.contains(" ") );
		
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
	
	public void stop()
	{
		// TODO Zeugs um Thread zu stoppen / Funktioniert irgendwie nicht?!
		System.out.println("Stop this Thread!");
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
				stop();
			}
		}
	}
	
	public static void main(String[] args)
	{
		new client();
	}
	
	public void display(String line)
	{
		if (consoleinput != null) {
			if ( !consoleinput.matches( line ) ) {
				// könnte blöd werden, wenn anderer Teilnehmer das gleiche schreibt
				// mit Benutzernamen im String dürfte die Filterung einfacher werden
				System.out.println(line);
				consoleinput = "";
			}
		}
	}
}