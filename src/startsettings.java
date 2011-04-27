import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class startsettings extends JFrame {
	
	private int num, port;
	private String ip;
	private client c;
	private JTextField ipField, portField;

	public final static int SAVED = 0;
	public final static int CANCELED = 1;
	public final static int ERROR = 2;
	
	public startsettings(client client, String ip, int port) {

		this.c = client;
		this.ip = ip;
		this.port = port;
	    
	    JPanel ipPanel = new JPanel();
	    ipPanel.setLayout( new BoxLayout(ipPanel, BoxLayout.X_AXIS) );
	    JLabel ipLabel = new JLabel("Server-IP:");
	    ipPanel.add(ipLabel);
	    ipField = new JTextField( ip );
	    ipPanel.add(ipField);
	    
	    JPanel portPanel = new JPanel();
	    portPanel.setLayout( new BoxLayout(portPanel, BoxLayout.X_AXIS) );
	    JLabel portLabel = new JLabel("Server-Port:");
	    portPanel.add(portLabel);
	    portField = new JTextField( Integer.toString(port) );
	    portPanel.add(portField);

	    JPanel bigPanel = new JPanel();
	    bigPanel.setLayout( new BoxLayout(bigPanel, BoxLayout.Y_AXIS) );
	    bigPanel.add(ipPanel);
	    bigPanel.add(portPanel);
	    
	    Object[] options = {"Speichern", "Abbrechen"};
	    num = JOptionPane.showOptionDialog(this,
	    	  bigPanel,
	    	  "Startoptionen",
	    	  JOptionPane.YES_NO_CANCEL_OPTION,
	    	  JOptionPane.QUESTION_MESSAGE,
	    	  null,
	    	  options,
	    	  options[1]);
	  }
	  
	  /**
	   * 
	   * @return 0 oder SAVED wenn Eingaben gespeichert wurde, 
	   *         1 oder CANCELED wenn abbrechen gedrückt wurde, 
	   *         2 oder ERROR wenn port kein integer war
	   */
	  public int handleInput() {
		
	    if (num != 0) {
	    	// abbrechen gedrückt
	    	return CANCELED;
	    }
    	//System.out.println("Speichern");
        // Speichern
    	ip= ipField.getText() ;
    	try {
    		port = Integer.parseInt( portField.getText() );
    	} catch (NumberFormatException e) {
    		System.err.println(e + " ist kein Integer");
    		return ERROR;
    	}
    	c.saveSettings(ip, port);
		
		return SAVED;
	  }
}
