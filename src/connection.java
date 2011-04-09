import java.net.*;
import java.io.*;

class connection extends Thread
{
	protected Socket client;
	protected BufferedReader in;
	protected PrintStream out;
	protected server server;
	protected String nickname;
	private volatile Thread connect;

	public connection(server server, Socket client)
	{
		this.server=server;
		this.client=client;

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

		connect = new Thread(this);
		connect.start();
	}
	
	public void done()
	{
		// Zeugs um Thread zu stoppen
		System.out.println("Stop the connection Thread!");
		
		try {
			client.close();
		} catch (IOException e) {
			System.err.println("Fehler beim Schließen der Verbindung: " + e);
		}
		
		server.removeconnection(this);
		
		Thread moribund = connect;
		connect = null;
		moribund.interrupt();
	}


	public void run()
	{
		String line;
		Thread thisThread = Thread.currentThread();

		// TODO mal ordentlichen Code draus machen

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
				if(line.startsWith("/name ")) {
					nickname = line.substring(6);
					System.out.println("Nickname: " + nickname);
					// TODO hier könnten alle anderen Serverbefehle hin
				} else if(line.startsWith("/quit")) {
					this.done();
				} else {
					// TODO an Absender zurück: Unbekannter Befehl
				}
			} else {
				System.out.println(line); //könnte gelöscht werden
				server.broadcast(line);
			}
		}
	}
}