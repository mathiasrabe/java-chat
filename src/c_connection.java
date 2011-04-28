import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;


public class c_connection {
	
	protected Socket socket;
	protected BufferedReader in;
	protected PrintStream out;
	protected String ip;
	protected int port;
	protected client client;
	protected boolean closed = false;
	
	public c_connection(String ip, int port, client client)
	{
		this.ip = ip;
		this.port = port;
		this.client = client;
	}
	
	public void close() throws IOException
	{
		out.println("/quit");
		
		closed = true;
		
		if(socket != null) {
			try {
				socket.close();
			} catch(IOException e) {
				throw e;
			}
		}
	}
	
	public void connect() throws IOException
	{
		try
		{
			socket = new Socket( ip, port);
		} catch (IOException e)
		{
			throw e;
		}
	}
	
	public void enableStream() throws IOException
	{
		try
		{
			in = new BufferedReader(new InputStreamReader( socket.getInputStream() ));
			out = new PrintStream(socket.getOutputStream());
		} catch (IOException e)
		{
			try { socket.close(); } catch (IOException e2) {} ;
			throw e;
		}
	}
	
	protected void waitForServer()
	{
		String line;

		try
		{
			while (!closed)
			{
				line=in.readLine();
				if(line!=null) {
					client.print(line);
				}
			}
		} catch(IOException e)
		{
			if (!socket.isClosed())
				System.out.println("Fehler: " + e);
		}
	}
	
	public void sendMsg(String msg)
	{
		out.println(msg);
	}
	
	public boolean isConnected()
	{
		if (socket != null) {
			return true;
		}
		return false;
	}
	
	public boolean isStreamable()
	{
		if (out != null) {
			return true;
		}
		return false;
	}

}
