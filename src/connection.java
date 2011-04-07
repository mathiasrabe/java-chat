import java.net.*;
import java.io.*;

class connection extends Thread
{
	protected Socket client;
	protected BufferedReader in;
	protected PrintStream out;
	protected server server;
	protected String nickname;

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

		this.start();
	}


	public void run()
	{
		String line;

		try
		{
			while(true)
			{
				line=in.readLine();
				System.out.println(line); // TODO ein geschlossener Client schickt hier ne Menge null's
				if(line!=null)
					if(line.startsWith("/name ")) {
						nickname = line.substring(6);
						System.out.println("Nickname: " + nickname);
						// TODO hier könnten alle anderen Serverbefehle hin
					} else {
						server.broadcast(line);
					}
			}
		} catch (IOException e)
		{
			System.out.println("Fehler:" + e);
		}
	}
}