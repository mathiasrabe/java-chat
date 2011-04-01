import java.net.*;
//import java.util.Vector;
import java.io.*;
import java.awt.*;
import java.applet.*;
import java.awt.event.*;

public class client extends Applet implements WindowListener
{
	public static final int PORT = 8765;
	Socket socket;
	DataInputStream in;
	PrintStream out;
	TextField inputfield;
	TextArea outputarea;
	Thread thread;
	Frame myframe;

	public void init()
	{
		inputfield = new TextField();
		outputarea = new TextArea();
		outputarea.setFont( new Font("Dialog", Font.PLAIN, 12));
		outputarea.setEditable(false);

		//myframe.setLayout(new BorderLayout());
		myframe.add("South", inputfield);
		myframe.add("Center", outputarea);

		myframe.setBackground(Color.lightGray);
		myframe.setForeground(Color.black);
		inputfield.setBackground(Color.white);
		outputarea.setBackground(Color.white);
	}

	public void start()
	{
		try
		{
			//socket = new Socket(this.getCodeBase().getHost(), PORT);
			socket = new Socket("localhost", PORT);
			in = new DataInputStream(socket.getInputStream());
			out = new PrintStream(socket.getOutputStream());
		} catch (IOException e)
		{
			this.showStatus(e.toString());
			say("Verbindung zum Server fehlgeschlagen!");
			System.out.println("Verbindung fehlgeschlagen!"); // TODO
			System.exit(1);
		}

		say("Verbindung zum Server aufgenommen...");
/*
		if (thread == null)
		{
			thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}*/
	}


	public void stop()
	{
		System.out.println("Schließe Verbindung"); // TODO
		try
		{
			socket.close();
		} catch (IOException e)
		{
			this.showStatus(e.toString());
		}

		if ((thread !=null) && thread.isAlive())
		{
			thread.stop();
			thread = null;
		}
	}


	public void run()
	{
		String line;

		try
		{
			while(true)
			{
				line = in.readLine();
				if(line!=null)
					outputarea.appendText(line+'\n' );
			}
		} catch (IOException e) { say("Verbindung zum Server abgebrochen"); }
	}


	public boolean action(Event e, Object what)
	{
		if (e.target==inputfield)
		{
			String inp=(String) e.arg;

			out.println(inp);
			inputfield.setText("");
			return true;
		}

		return false;
	}
	
	// TODO wie bekommt man das lauffähig?
	public void main(String[] args)
	{
		client c = new client();
		myframe = new Frame("Hermes-Chat-Client");
		c.init();
		myframe.addWindowListener(c);
		myframe.setVisible(true);
		c.start();
	}
	
	public void windowClosing (WindowEvent e) {
	    this.stop();
	    this.destroy();
	    System.exit(0);
    }
	
	public void windowDeactivated(WindowEvent e) {	
	}
	
	public void windowDeiconified(WindowEvent e) {
	}
	
	public void windowOpened(WindowEvent e) {
	}
	
	public void windowIconified(WindowEvent e) {
	}
	
	public void windowClosed(WindowEvent e) {
	}
	
	public void windowActivated(WindowEvent e) {
	}


	public void say(String msg)
	{
		outputarea.appendText("*** "+msg+" ***\n");
	}
}