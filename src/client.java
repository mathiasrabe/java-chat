import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.net.*;
import java.io.*;

public class client extends JFrame implements KeyListener
{
	public static final int PORT = 8765;
	protected Socket socket;
	protected BufferedReader in;
	protected PrintStream out;
	
	protected JMenuBar menuBar;
	protected JMenu menu;
	protected JScrollPane scrollPane;
	protected JTextArea outputarea, userlistarea;
	protected JTextField inputfield;
	protected JButton sendButton;
	
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
 		
 		JPanel upperPanel = new JPanel();
 		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));
	   
 		// http://download.oracle.com/javase/1.5.0/docs/api/javax/swing/JTextArea.html
 		outputarea = new JTextArea();
 		outputarea.setEditable(false);
 		outputarea.setLineWrap(true);
 		outputarea.setWrapStyleWord(true);
 		outputarea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
 		scrollPane = new JScrollPane(outputarea);
 		scrollPane.setPreferredSize(new Dimension(500, 250));
 		scrollPane.setMinimumSize(new Dimension(100, 50));
 		upperPanel.add(scrollPane);
 		
 		userlistarea = new JTextArea();
 		//userlistarea.setPreferredSize(new Dimension(150, 100));
 		userlistarea.setMinimumSize(new Dimension(50, 50));
 		userlistarea.setEditable(false);
 		upperPanel.add(userlistarea);
 		getContentPane().add(upperPanel, BorderLayout.CENTER);
 		
 		
 		JPanel lowerPanel = new JPanel();
 		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.X_AXIS));
	   
 		inputfield = new JTextField();
 		inputfield.setPreferredSize(new Dimension(600, 25));
 		inputfield.addKeyListener(this);
 		lowerPanel.add(inputfield);
 		//getContentPane().add(inputfield);
	   
 		Action sendAction = new sendAction();
 		sendButton = new JButton(sendAction);
 		lowerPanel.add(sendButton);
 		getContentPane().add(lowerPanel, BorderLayout.PAGE_END);
 	}
	
	protected void connect()
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
	
	protected void waitForServer()
	{
		String line;

		try
		{
			while (true)
			{
				line=in.readLine();
				if(line!=null) {
					outputarea.setText( outputarea.getText() + line + "\n" ); //sollte dafür sorgen dass immer die unterste Zeile angezeigt wird
					//outputarea.append(line + "\n");
					outputarea.setCaretPosition(outputarea.getDocument().getLength());
					//outputarea.setPreferredSize( outputarea.getPreferredScrollableViewportSize() );
					scrollPane.revalidate();
				}
			}
		} catch(IOException e)
		{
			if (!socket.isClosed())
				System.out.println("Fehler: " + e);
		}
	}
	
	protected void launchFrame()
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
	
	private void sendText(String text) {
		out.println(text);
		inputfield.setText("");
	}

	public void keyPressed(KeyEvent e) {			
	}
	
	public void keyTyped(KeyEvent e) {
	}
	
	// wenn Enter gedrückt wird, wird das Zeug weg geschickt
	public void keyReleased(KeyEvent e) {
		String input = inputfield.getText();
		int key = e.getKeyCode();
		if(key == KeyEvent.VK_ENTER && input != null) {
			sendText(input);
		}
	}
	
	private class sendAction extends AbstractAction {
		
	    public sendAction()
	    {
	        super("Senden");
	    }
	    
	    public void actionPerformed(ActionEvent e) {
	    	String input = inputfield.getText();
	    	if (input != null) {
	    		sendText(input);
	    	}
	    }
	}
}
 