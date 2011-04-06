import java.net.*;
import java.io.*;

class connection_client extends Thread
{
	protected Socket server;
	protected DataInputStream in;
	protected PrintStream out;
	protected client client;
	
	public connection_client(Socket server, client client)
	{
		this.server=server;
		this.client=client;

		try
		{
			in = new DataInputStream(server.getInputStream());
			out = new PrintStream(server.getOutputStream());
		} catch (IOException e)
		{
			try { server.close(); } catch (IOException e2) {} ;
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
				if(line!=null)
					client.display(line);
			}
		} catch (IOException e)
		{
			System.out.println("Fehler:" + e);
		}
	}
}