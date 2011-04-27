import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.net.*;
import java.io.*;

public class client extends JFrame implements KeyListener, WindowListener
{
	private int port = 8765;
	private String ip = "localhost";
	protected Socket socket;
	protected BufferedReader in;
	protected PrintStream out;
	private boolean connected;
	
	protected JMenuBar menuBar;
	protected JMenu menu;
	protected JScrollPane scrollPane;
	protected JTextArea outputarea, userlistarea;
	protected JTextField inputfield;
	protected JButton sendButton;
	
	public client()
 	{
		super("Java-Chat");
 		
 		buildGUI();
		launchFrame();
		
		// Starteinstellungen abfragen und versuchen zu verbinden
		// inkl. Fehlerbehandlung
		do {
			int status = showStartsettings();
			if (status == startsettings.CANCELED) {
				this.close();
				System.exit(0);
			} else if (status == startsettings.ERROR) {
				continue;
			}
			connect();
		} while (!connected);
		outputarea.setText("");
		waitForServer();
 	}
	
	protected void buildGUI()
	{
 		//setSize(300,300);
 		//setLocation(300,300);
	   
 		// http://download.oracle.com/javase/tutorial/uiswing/layout/border.html
 		getContentPane().setLayout(new BorderLayout(1, 1));
 		
 		menuBar = new JMenuBar();
 		menu = new JMenu("Datei");
 		menu.setMnemonic(KeyEvent.VK_D);
 		menu.getAccessibleContext().setAccessibleDescription(
 		        "The only menu in this program that has menu items"); //FIXME
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
 		
 		this.addWindowListener(this);
	}
	
	protected void connect()
	{
		try
		{
			socket = new Socket( ip, port);
		} catch (IOException e)
		{
			print("Fehler beim Verbindungsaufbau: " + e);
			System.err.println("Verbindung fehlgeschlagen: "+ e);
			return;
		}
		
		try
		{
			in = new BufferedReader(new InputStreamReader( socket.getInputStream() ));
			out = new PrintStream(socket.getOutputStream());
		} catch (IOException e)
		{
			try { socket.close(); } catch (IOException e2) {} ;
			print("Fehler beim erzeugen des Streams: " + e);
			System.err.println("Fehler beim Erzeugen der Streams: " + e);
			return;
		}
		
		connected = true;
	}
	
	protected void waitForServer()
	{
		String line;

		try
		{
			while (connected)
			{
				line=in.readLine();
				if(line!=null) {
					print(line);
				}
			}
		} catch(IOException e)
		{
			if (!socket.isClosed())
				System.out.println("Fehler: " + e);
		}
	}
	
	protected void print(String text)
	{
		outputarea.setText( outputarea.getText() + text + "\n" ); //sollte dafür sorgen dass immer die unterste Zeile angezeigt wird
		outputarea.setCaretPosition(outputarea.getDocument().getLength());
		scrollPane.revalidate();
	}
	
	protected void launchFrame()
	{
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // this.close()    
 		pack();
 		setVisible(true);
	}
	
	public void saveSettings(String ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}
	
	/**
	* 
	* @return 0 wenn Eingaben gespeichert wurde, 
	*         1 wenn abbrechen gedrückt wurde, 
	*         2 wenn port kein integer war
	*/
	protected int showStartsettings()
	{
		startsettings s = new startsettings(this, ip, port);
		
		int saved = s.handleInput();
		
		return saved;
	}

	public static void main(String[] args)
	{
		new client();
	}
	
	public void close()
	{
		System.out.println("Beende chat-client");
		print("bye bye ...");
		if(socket != null) {
			try {
				socket.close();
			} catch(IOException e) {
				System.err.println("Fehler beim Beenden der Verbindung: " + e);
			}
		}
		connected = false;
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
	
	public void windowClosing(WindowEvent e)
	{
		this.close();
	}
	
	public void windowDeactivated(WindowEvent e)
	{
	}
	
	public void windowDeiconified(WindowEvent e)
	{
	}
	
	public void windowOpened(WindowEvent e)
	{
	}
	
	public void windowIconified(WindowEvent e)
	{
	}
	
	public void windowClosed(WindowEvent e)
	{
	}
	
	public void windowActivated(WindowEvent e)
	{
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
 