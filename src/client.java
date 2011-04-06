import java.net.*;
import java.util.*; //Eingabe über Konsole & Vector

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

	
	public client()
	{
		in = new Scanner(System.in);
		System.out.println("Bitte geben Sie eine IP-Adresse ein:");
		String input = in.nextLine();
		// TODO Die Variable input muss auf Richtigkeit überprüft werden!
		
		try
		{
			socket = new Socket( input, PORT);
		} catch (IOException e)
		{
			System.err.println("Verbindung fehlgeschlagen:"+ e); // TODO
			System.exit(1);
		}
		
		connect = new Thread(this);
		connect.start();
	}
	
	public void stop()
	{
		// Zeugs um Thread zu stoppen / Funktioniert irgendwie nicht?!
		System.out.println("Stop this Thread!");
		Thread moribund = connect;
		connect = null;
		moribund.interrupt();
	}
	
	public void run()
	{
		//consoleinput = new String();
		
		connection_client c = new connection_client(socket, this);
		
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
		if ( !consoleinput.matches( line ) ) {
			// könnte blöd werden, wenn anderer Teilnehmer das gleiche schreibt
			System.out.println(line);
			consoleinput = "";
		}
	}
}