import java.net.*;
import java.io.*;

class connection_client extends Thread
{
	protected Socket server;
	protected BufferedReader in;
	protected PrintStream out;
	protected client client;
	private volatile Thread connect;
	
	public connection_client(Socket server, client client, String username)
	{
		this.server=server;
		this.client=client;

		try
		{
			in = new BufferedReader(new InputStreamReader( server.getInputStream() ));
			out = new PrintStream(server.getOutputStream());
		} catch (IOException e)
		{
			try { server.close(); } catch (IOException e2) {} ;
			System.err.println("Fehler beim Erzeugen der Streams: " + e);
			return;
		}
		
		// Anmeldung beim Server mit Benutzername
		out.println("/name " + username);

		connect = new Thread(this);
		connect.start();
	}
	
	public void done()
	{
		// Zeugs um Thread zu stoppen
		out.println("/quit"); //abmelden beim Server
		try {
			server.close();
		} catch (IOException e) {
			System.err.println("Fehler beim Schließen der Verbindung: " + e);
		}
		
		Thread moribund = connect;
		connect = null;
		moribund.interrupt();
	}
	
	public void run()
	{
		String line;
		Thread thisThread = Thread.currentThread();

		try
		{
			while (connect == thisThread)
			{
				line=in.readLine();
				if(line!=null)
					client.display(line);
			}
		} catch(IOException e)
		{
			if (!server.isClosed())
				System.out.println("Fehler: " + e);
		}
	}
}