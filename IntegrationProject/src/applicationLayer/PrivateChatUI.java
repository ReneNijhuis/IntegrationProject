package applicationLayer;

import java.awt.Button;
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

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
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

public class PrivateChatUI extends JFrame implements KeyListener, ActionListener{ // <-- should extend JFrame: easier and more clear implementation
	private static final long serialVersionUID = 5488009698932086488L;

	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private int windowWidth = (int)(screenSize.width * 0.75d);
	private int windowHeight = (int)(windowWidth / 1280 * 800);
	private static final int MAX_LENGTH = 184;
	private Dimension windowSize = new Dimension(windowWidth, windowHeight);
	private String name = null;
	private final Main main;

	private boolean sendEnabled = false;

	private JTextField textfield1 = new JTextField();
	private JTextArea textarea1 = new JTextArea();
	private JTextArea textarea2 = new JTextArea();
	private JButton button1 = new JButton();
	private JPanel panel1 = new JPanel(new GridBagLayout());
	private JMenuItem helpItem = new JMenuItem("Help");
	private JMenuItem priveChatItem = new JMenuItem("Switch");
	private JMenuItem logOutItem = new JMenuItem("Logout");
	private JPanel buttonpanel = new JPanel();
	private JPanel insertpanel = new JPanel();
	private GroupLayout layout = new GroupLayout(buttonpanel);
	private GridBagConstraints con1 = new GridBagConstraints();
	private GridBagConstraints con2 = new GridBagConstraints();
	private GridBagConstraints con3 = new GridBagConstraints();
	private GridBagConstraints con4 = new GridBagConstraints();
	private GridBagConstraints con5 = new GridBagConstraints();
	private GridBagConstraints con6 = new GridBagConstraints();
	private GridBagConstraints con7 = new GridBagConstraints();

	private int margin = 10;
	private Insets insets = new Insets(margin, margin, margin, margin);

	public PrivateChatUI(Main main){
		super("Chatbox");
		this.main = main;
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				((PrivateChatUI) e.getWindow()).main.shutDown(true);
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

						con1.fill = GridBagConstraints.BOTH;
		con1.gridx = 0;
		con1.gridy = 1;
		con1.ipady = (int) (windowHeight / 1.8);
		con1.ipadx = (int) (windowWidth / 5);

		con2.fill = GridBagConstraints.BOTH;
		con2.weightx = 1.0;
		con2.weighty = 1.0;
		con2.gridx = 0;
		con2.gridy = 0;
		con2.ipady = (int) (windowHeight / 1.6);
		con2.ipadx = (int) (windowWidth / 1.4);
		con2.insets = insets;		

		con3.fill = GridBagConstraints.BOTH;
		con3.weightx = 1.0;
		con3.weighty = 1.0;
		con3.gridx = 1;
		con3.gridy = 1;
		con3.ipady = (int) (windowHeight / 5);
		con3.ipadx = (int) (windowWidth / 5);
		con3.insets = insets;

		con4.fill = GridBagConstraints.BOTH;
		con4.weightx = 1.0;
		con4.weighty = 1.0;
		con4.gridx = 0;
		con4.gridy = 1;
		con4.ipady = (int) (windowHeight / 5);
		con4.ipadx = (int) (windowWidth / 1.4);
		con4.insets = insets;		

		con5.fill = GridBagConstraints.BOTH;
		con5.weightx = 1.0;
		con5.weighty = 1.0;
		con5.gridx = 0;
		con5.gridy = 0;
		con5.ipady = (int) (windowHeight / 17);

		con6.fill = GridBagConstraints.BOTH;
		con6.weightx = 1.0;
		con6.weighty = 1.0;
		con6.gridx = 1;
		con6.gridy = 0;
		con6.ipady = (int) (windowHeight / 5);
		con6.ipadx = (int) (windowWidth / 4.5);
		con6.insets = insets;

		con7.fill = GridBagConstraints.BOTH;
		con7.weightx = 1.0;
		con7.weighty = 1.0;
		con7.gridx = 0;
		con7.gridy = 0;

		JMenuBar menuBar = new JMenuBar();
		JMenu optionMenu = new JMenu("Options");

		helpItem.addActionListener(this);
		optionMenu.add(helpItem);
		priveChatItem.addActionListener(this);
		optionMenu.add(priveChatItem);
		logOutItem.addActionListener(this);
		optionMenu.add(logOutItem);
		menuBar.add(optionMenu);
		setJMenuBar(menuBar);

		Border border1 = new LineBorder(new Color(34,220,214), 6 ,false);
		textarea1.setBorder(border1);
		textarea1.setFont(new Font("Calibri", Font.ITALIC, 22));
		JScrollPane scrollpane = new JScrollPane(textarea1);
		JPanel listPane1 = new JPanel();
		listPane1.setLayout(new BoxLayout(listPane1, BoxLayout.PAGE_AXIS));
		listPane1.add(scrollpane);
		panel1.add(listPane1,con2);


		Border border2 = new LineBorder(new Color(34,121,220), 6 ,false);
		textarea2.setFont(new Font("Calibri", Font.ITALIC, 22));
		JTextArea toparea = new JTextArea();
		toparea.setFont(new Font("Calibri", Font.ITALIC, 28));
		toparea.setBackground(new Color(34,121,220));
		toparea.setForeground(new Color(34,220,214));
		toparea.setEditable(false);
		toparea.setText("You are now chatting with:");

		buttonpanel.add(textarea2);
		JScrollPane js = new JScrollPane(buttonpanel);

		buttonpanel.setLayout(layout);

		insertpanel = new JPanel(new GridBagLayout());
		insertpanel.setBackground(new Color(34,121,220));
		insertpanel.add(toparea,con5);
		insertpanel.add(js,con1);
		insertpanel.setBorder(border2);
		panel1.add(insertpanel,con6);

		Border border3 = new LineBorder(new Color(34,220,214), 6 ,false);
		button1.addActionListener(this);
		button1.setFont(new Font("Calibri", Font.ITALIC, 28));
		button1.setBackground(new Color(255,255,255));
		button1.setForeground(new Color(34,121,220));
		button1.setText("Send");
		button1.setEnabled(false);
		JPanel listPane4 = new JPanel(new GridBagLayout());
		listPane4.add(button1,con7);
		listPane4.setBorder(border3);
		panel1.add(listPane4,con3);

		Border border4 = new LineBorder(new Color(34,121,220), 6 ,false);
		textfield1.setBorder(border4);
		textfield1.setText("Input text here:");
		MouseAdapter myListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				textfield1.setText("");
				textfield1.removeMouseListener(this);
			}};
			textfield1.addMouseListener(myListener);
			textfield1.addKeyListener(this);
			textfield1.setFont(new Font("Calibri", Font.ITALIC, 22));

			JPanel listPane3 = new JPanel();
			listPane3.setLayout(new BoxLayout(listPane3, BoxLayout.PAGE_AXIS));
			listPane3.add(textfield1);
			panel1.add(listPane3,con4);
			panel1.setBackground(new Color(34,169,220));
			add(panel1);
			setResizable(true);
	}

	public void addMessage(String name, String message) {
		textarea1.append(name + ":\t" + message + "\n");		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == button1) {
			sendMessage(textfield1.getText());
		}
		else if (arg0.getSource() == priveChatItem){
			if (addQuestion("","Do you want to switch back to group chat?",false)){
				main.toPublic();
			}
		}
		else if (arg0.getSource() == logOutItem){
			if (addQuestion("","Do you want to log out?",false)){
				main.logout();
			}
		}
	}

	private void sendMessage(String message) {
		if (sendEnabled 
				&& !message.equals("Input text here:") 
				&& main.sendMessage(message)
				) {
			textfield1.setText("");
			sendEnabled = false;
			updateSendButton();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char c;
		if ((c = e.getKeyChar()) != KeyEvent.CHAR_UNDEFINED) {
			if (e.getSource().equals(textfield1)) {
				if (textfield1.getText().length() > MAX_LENGTH) {
					addPopup("Username too long", "Max message length is " + MAX_LENGTH, false);
					textfield1.setText(textfield1.getText().substring(0, MAX_LENGTH));
				}
				else {
					if (c == '\n') {
						sendMessage(textfield1.getText());
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
		setMinimumSize(new Dimension(1100,700));
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
	
	public void clear(){
		textfield1.setText("");
	}

	public void setCompagionName(String name){
		insertpanel.removeAll();
		panel1.remove(insertpanel);
		Border border2 = new LineBorder(new Color(34,121,220), 6 ,false);
		textarea2.setFont(new Font("Calibri", Font.ITALIC, 22));
		JTextArea toparea = new JTextArea();
		toparea.setFont(new Font("Calibri", Font.ITALIC, 28));
		toparea.setBackground(new Color(34,121,220));
		toparea.setForeground(new Color(34,220,214));
		toparea.setEditable(false);
		toparea.setText("You are now chatting with:");
		
		buttonpanel.add(textarea2);
		JScrollPane js = new JScrollPane(buttonpanel);
		buttonpanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		GroupLayout.SequentialGroup sgroup = layout.createSequentialGroup();
		GroupLayout.SequentialGroup vgroup = layout.createSequentialGroup();
		GroupLayout.SequentialGroup v2group = layout.createSequentialGroup();
		GroupLayout.ParallelGroup pgroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.ParallelGroup p2group = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		Button c = new Button(name);
		c.setFont(new Font("Calibri", Font.ITALIC, 40));
		c.setBackground(new Color(255,255,255));
		c.setMinimumSize(new Dimension(0,110));
		c.setMaximumSize(new Dimension(1000000,110));
		c.setForeground(new Color(34,121,220));
		pgroup.addComponent(c);
		v2group.addComponent(c);
		v2group.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE));
		layout.setHorizontalGroup(sgroup.addGroup(pgroup));
		layout.setVerticalGroup(vgroup.addGroup(p2group.addGroup(v2group)));
		
		insertpanel = new JPanel(new GridBagLayout());
		insertpanel.setBackground(new Color(34,121,220));
		insertpanel.add(toparea,con5);
		insertpanel.add(js,con1);
		insertpanel.setBorder(border2);
		panel1.add(insertpanel,con6);
		panel1.revalidate(); 
		panel1.repaint();
	}

	public boolean addQuestion(String title, String message, boolean warning) {
		int selection = JOptionPane.showConfirmDialog(null, message, title
				, JOptionPane.OK_CANCEL_OPTION
				, JOptionPane.INFORMATION_MESSAGE);
		if (selection == JOptionPane.OK_OPTION)	{
			return true;
		}
		return false;
	}
}
