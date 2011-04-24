import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.net.*;
import java.util.*;
import java.io.*;

public class client extends JFrame
{
	public static final int PORT = 8765;
	protected Socket socket;
	protected BufferedReader in;
	protected PrintStream out;
	
	JMenuBar menuBar;
	JMenu menu;
	JTextArea outputarea, userlistarea;
	JTextField inputfield;
	JButton sendButton;
	
	public client()
 	{
		super("Java-Chat");
 		//setSize(300,300);
 		//setLocation(300,300);
	   
 		// http://download.oracle.com/javase/tutorial/uiswing/layout/border.html
 		getContentPane().setLayout(new BorderLayout(1, 1));
 		
 		menuBar = new JMenuBar();
 		menu = new JMenu("A Menu");
 		menu.setMnemonic(KeyEvent.VK_A);
 		menu.getAccessibleContext().setAccessibleDescription(
 		        "The only menu in this program that has menu items");
 		menuBar.add(menu);
 		this.setJMenuBar(menuBar);
	   
 		// http://download.oracle.com/javase/1.5.0/docs/api/javax/swing/JTextArea.html
 		outputarea = new JTextArea();
 		outputarea.setPreferredSize(new Dimension(500, 250));
 		outputarea.setEditable(false);
 		outputarea.setLineWrap(true);
 		outputarea.setWrapStyleWord(true);
 		outputarea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
 		JScrollPane scrollPane = new JScrollPane(outputarea);
 		getContentPane().add(scrollPane, BorderLayout.LINE_START); //FIXME
 		
 		userlistarea = new JTextArea();
 		userlistarea.setPreferredSize(new Dimension(150, 100));
 		userlistarea.setEditable(false);
 		getContentPane().add(userlistarea, BorderLayout.CENTER);
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(new FlowLayout());
	   
 		inputfield = new JTextField();
 		inputfield.setPreferredSize(new Dimension(600, 25));
 		panel.add(inputfield);
 		//getContentPane().add(inputfield);
	   
 		Action sendText = new sendText();
 		sendButton = new JButton(sendText);
 		panel.add(sendButton);
 		getContentPane().add(panel, BorderLayout.PAGE_END);
 	}
	
	public void connect()
	{
		try
		{
			socket = new Socket( "localhost", PORT);
		} catch (IOException e)
		{
			System.err.println("Verbindung fehlgeschlagen:"+ e);
			System.exit(1);
		}
		
		try
		{
			in = new BufferedReader(new InputStreamReader( socket.getInputStream() ));
			out = new PrintStream(socket.getOutputStream());
		} catch (IOException e)
		{
			try { socket.close(); } catch (IOException e2) {} ;
			System.err.println("Fehler beim Erzeugen der Streams: " + e);
			return;
		}
	}
	
	public void waitForServer()
	{
		String line;

		try
		{
			while (true)
			{
				line=in.readLine();
				if(line!=null)
					outputarea.append(line + "\n");
			}
		} catch(IOException e)
		{
			if (!socket.isClosed())
				System.out.println("Fehler: " + e);
		}
	}
	
	public void launchFrame()
	{
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       
 		pack();
 		setVisible(true);
	}

	public static void main(String[] args)
	{
		client c = new client();
		c.launchFrame();
		c.connect();
		c.waitForServer();
	}
	

	public class sendText extends AbstractAction {
		
	    public sendText()
	    {
	        super("Senden");
	    }
	    
	    public void actionPerformed(ActionEvent e) {
	    	String input = inputfield.getText();
	    	if (input != null) {
	    		out.println(input);
	    		inputfield.setText("");
	    	}
	    }
	}
}
 