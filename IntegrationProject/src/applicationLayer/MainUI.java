package applicationLayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.channels.ShutdownChannelGroupException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class MainUI extends JFrame implements KeyListener, ActionListener{ // <-- should extend JFrame: easier and more clear implementation
	private static final long serialVersionUID = 5488009698932086488L;
	
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private int windowWidth = (int)(screenSize.width * 0.75d);
	private int windowHeight = (int)(windowWidth / 1280 * 800);
	private Dimension windowSize = new Dimension(windowWidth, windowHeight);
	
	private final Main main;
	
	private boolean sendEnabled = false;
	
	private JTextField textfield1 = new JTextField();
	private JTextArea textarea1 = new JTextArea();
	private JTextArea textarea2 = new JTextArea();
	private JButton button1 = new JButton();
	private JPanel panel1 = new JPanel(new GridBagLayout());
	
	private int margin = 10;
	private Insets insets = new Insets(margin, margin, margin, margin);
	
	public MainUI(Main main){
		super("Chatbox");
		this.main = main;
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				((MainUI) e.getWindow()).main.shutDown(true);
			}
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
		setSize(windowSize);
		setWindowLocation();
		createInterface();
		setVisible(false);
	}

	public void createInterface(){
				
		textarea1.setEditable(false);
		textarea2.setEditable(false);		

		GridBagConstraints con1 = new GridBagConstraints();
		GridBagConstraints con2 = new GridBagConstraints();
		GridBagConstraints con3 = new GridBagConstraints();
		GridBagConstraints con4 = new GridBagConstraints();
		GridBagConstraints con5 = new GridBagConstraints();
		
		con1.fill = GridBagConstraints.HORIZONTAL;
		con1.gridx = 0;
		con1.gridy = 1;
		con1.ipady = 385;
		con1.ipadx = 172;

		con2.fill = GridBagConstraints.HORIZONTAL;
		con2.weightx = 1.0;
		con2.weighty = 1.0;
		con2.gridx = 0;
		con2.gridy = 0;
		con2.ipady = 500;
		con2.ipadx = 950;
		con2.insets = insets;		

		con3.fill = GridBagConstraints.VERTICAL;
		con3.weightx = 1.0;
		con3.weighty = 1.0;
		con3.gridx = 1;
		con3.gridy = 1;
		con3.ipady = 172;
		con3.ipadx = 415;
		con3.insets = insets;

		con4.fill = GridBagConstraints.HORIZONTAL;
		con4.weightx = 1.0;
		con4.weighty = 1.0;
		con4.gridx = 0;
		con4.gridy = 1;
		con4.ipady = 155;
		con4.ipadx = 950;
		con4.insets = insets;		

		con5.fill = GridBagConstraints.HORIZONTAL;
		con5.gridx = 0;
		con5.gridy = 0;
		con5.ipady = 53;
		con5.ipadx = 200;
		
		JMenuBar menuBar = new JMenuBar();
		JMenu optionMenu = new JMenu("Options");
		JMenuItem helpItem = new JMenuItem("Help");
		helpItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addPopup("Help", 
						"Fill in a username and group password to login.\n", false
						);
			}
		});
		optionMenu.add(helpItem);
		JMenuItem priveChatItem = new JMenuItem("PriveChat");
		priveChatItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addPopup("PriveChat", 
						"Select a user for a privechat.\n", false
						);
			}
		});
		optionMenu.add(priveChatItem);
		JMenuItem logOutItem = new JMenuItem("Logout");
		logOutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addPopup("Logout", 
						"Click here to logout.\n", false
						);
			}
		});
		optionMenu.add(logOutItem);
		menuBar.add(optionMenu);
		setJMenuBar(menuBar);
		
		Border border1 = new LineBorder(new Color(34,220,214), 6 ,false);
		textarea1.setBorder(border1);
		textarea1.setFont(new Font("Calibri", Font.ITALIC, 22));
		JScrollPane scrollpane = new JScrollPane(textarea1);
		JPanel listPane1 = new JPanel();
		listPane1.setLayout(new BoxLayout(listPane1, BoxLayout.LINE_AXIS));
		listPane1.add(scrollpane);
		panel1.add(listPane1,con2);

		Border border2 = new LineBorder(new Color(34,121,220), 6 ,false);
		textarea2.setFont(new Font("Calibri", Font.ITALIC, 22));
		JScrollPane scrollpane2 = new JScrollPane(textarea2);
		JPanel insertpanel = new JPanel();
		insertpanel.setLayout(new GridBagLayout());
		JTextArea toparea = new JTextArea();
		toparea.setFont(new Font("Calibri", Font.ITALIC, 28));
		toparea.setBackground(new Color(34,121,220));
		toparea.setForeground(new Color(34,220,214));
		toparea.setEditable(false);
		toparea.setText("Connected Devices:");
		insertpanel.add(toparea,con5);
		insertpanel.add(scrollpane2,con1);
		insertpanel.setBorder(border2);
		panel1.add(insertpanel);		

		Border border3 = new LineBorder(new Color(34,220,214), 6 ,false);
		button1.addActionListener(this);
		button1.setBorder(border3);
		button1.setFont(new Font("Calibri", Font.ITALIC, 28));
		button1.setBackground(new Color(255,255,255));
		button1.setForeground(new Color(34,121,220));
		button1.setText("Send");

		JScrollPane scrollpane5 = new JScrollPane(button1);
		JPanel listPane4 = new JPanel();
		listPane4.setLayout(new BoxLayout(listPane4, BoxLayout.PAGE_AXIS));
		listPane4.add(scrollpane5);
		panel1.add(listPane4,con3);

		Border border4 = new LineBorder(new Color(34,121,220), 6 ,false);
		textfield1.setBorder(border4);
		textfield1.setText("Input text here:");
		MouseAdapter myListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				textfield1.setText(null);
				textfield1.removeMouseListener(this);
			}
		};
		textfield1.addMouseListener(myListener);
		textfield1.addKeyListener(this);
		textfield1.setFont(new Font("Calibri", Font.ITALIC, 22));

		JPanel listPane3 = new JPanel();
		listPane3.setLayout(new BoxLayout(listPane3, BoxLayout.PAGE_AXIS));
		listPane3.add(textfield1);
		panel1.add(listPane3,con4);

		panel1.setBackground(new Color(34,169,220));
		add(panel1);
	}
	
	public void addMessage(ChatMessage fullMessage) {
		textarea1.append(fullMessage.toString() + "\n");		
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == button1){
			String message = textfield1.getText();
			if (!message.equals("Input text here:")){
				textfield1.setText("");
				sendEnabled = false;
				updateSendButton();
				if (!main.sendMessage(message)) {
					addPopup("Network error", "Network not available, please connect and restart", true);
					main.shutDown(false);
					setVisible(false);
				}

			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char c;
		if ((c = e.getKeyChar()) != KeyEvent.CHAR_UNDEFINED) {
			if (e.getSource().equals(textfield1)) {
				if (c == '\n') {
					if (sendEnabled) {
						String message = textfield1.getText();
						if (!message.equals("Input text here:")) {
							main.sendMessage(message);
							textfield1.setText("");
							sendEnabled = false;
							updateSendButton();
						}
					}
				} else {	
					if (isLetterOrNumber(c)) {
						sendEnabled = true;
					} else if (containsLetterOrNumber(textfield1.getText())) {
						sendEnabled = true;
					} else {
						sendEnabled = false;
					}
				}
			}
		} 
		updateSendButton();
	}
	@Override
	public void keyPressed(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}

	private void updateSendButton() {
		if (sendEnabled && !button1.isEnabled()) {
			button1.setEnabled(true);
		} else if (!sendEnabled && button1.isEnabled()) {
			button1.setEnabled(false);
		}
	}
	
	private boolean containsLetterOrNumber(String s) {
		for (char c : s.toCharArray()) {
			if (isLetterOrNumber(c)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isLetterOrNumber(char c) {
		return Character.isLetter(c) || Character.isDigit(c);
	}
	
	/**
	 * Centers the window on the screen.
	 */
	private void setWindowLocation() {
		setLocation(
		(int)(screenSize.getWidth() / 2 - windowSize.width / 2),
		(int)(screenSize.getHeight() / 2 - windowSize.height / 2)
		);
	}
	
	public void addPopup(String title, String message, boolean warning) {
		if (!warning) {
			JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
		}

	}
}
