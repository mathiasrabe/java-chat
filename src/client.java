import java.net.*;
import java.util.*; //Bib für Eingabe über Konsole & Vector

//import java.util.Vector;
import java.io.*;

import com.google.api.detect.Detect;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;

public class client implements Runnable
{
	public static final int PORT = 8765;
	protected Socket socket;
	protected Scanner in;
	private volatile Thread connect;
	private String consoleinput;
	public String username;
        private String userlang = "DE";

	
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
			
                // Sprache ändern
                if(consoleinput.matches("/sp")) {

	                System.out.println("Bitte geben Sie ein Sprachkürzel ein:");
		        do {
			       if (userlang != null && userlang.contains(" "))
			       System.out.println("Das Sprachkürzel darf kein Leerzeichen enthalten sein."             
                               + " Bitte geben Sie erneut ein:");
			       userlang = in.nextLine();
		        } while( userlang.isEmpty() || userlang.contains(" ") );

               }
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
	
	/**
	 * 
	 * @param text der zu übersetzende Text
	 * @return übersetzer Text, oder leerer String, wenn nicht erfolgreich
	 */
	private String translate(String text)
	{
		Translate.setHttpReferrer("moep.de"); // wozu?
		Language lang = null;
             	String translatedText = new String();
		
		// Spracherkennung
		try {
			lang = Detect.execute(text).getLanguage();
		} catch (Exception e) {
			System.err.println("Fehler bei Spracherkennung: " + e);
			return translatedText;
		}
		
		//Übersetze Text
		try {
			translatedText = Translate.execute(text, lang, userlang);
			// FIXME Alles ins Englische übersetzen? bäää...
		} catch (Exception e) {
			System.err.println("Fehler bei Überstzung: " + e);
		}
		
		return translatedText;
		
	}
	
	public void display(String line)
	{
		if ( !line.startsWith(username) ) {
			String newtext = translate(line);
			if ( !newtext.isEmpty() ) {
				System.out.println(newtext);
			} else {
				System.out.println(line);
			}
		}
	}
}