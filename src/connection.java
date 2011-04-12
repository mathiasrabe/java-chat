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
		
		// zufälligen Benutzernamen erstellen und testen ob schon vorhanden
		String newnick;
		do {
			newnick = "USER" + (int) (Math.random() * 1000);
		} while( server.userexists(newnick) );
		nickname = newnick;
		
		System.out.println(nickname);

		connect = new Thread(this);
		connect.start();
	}
	
	public void sendServerMsg(String line)
	{
		out.println("*** "+ line + " ***");
	}
	
	public String getNickname()
	{
		return nickname;
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
				if(line.startsWith("/name ")) {
					String newnick = line.substring(6);
					if(newnick.contains(" ")) {
						sendServerMsg("Name Darf keine Leerzeichen enthalten");
					} else if (server.userexists( newnick )) {
						sendServerMsg("Name schon vergeben");
					} else {
						nickname = newnick;
						sendServerMsg("Ihr Benutzername lautet " + nickname);
						System.out.println("Nickname: " + nickname);
					}
					// TODO hier könnten alle anderen Serverbefehle hin
				} else if(line.startsWith("/quit")) {
					this.done();
				} else {
					// TODO an Absender zurück: Unbekannter Befehl
				}
			} else {
				System.out.println(nickname + ": " + line); //könnte gelöscht werden
				server.broadcast(nickname + ": " + line);
			}
		}
	}
}