package applicationLayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginGUI extends JFrame implements ActionListener, KeyListener {
	private static final long serialVersionUID = -482910055372612747L;
	
	private static final int MAX_LENGTH = 16;
	
	private int windowWidth = 450;
	private int windowHeight = 150;
	private Dimension windowSize = new Dimension(windowWidth, windowHeight);
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
	private static final Insets PADDING = new Insets(10, 10, 10, 10);
	private JTextField name;
	private String nameToolTip = "Fill in a username";
	private JPasswordField password;
	private String passToolTip = "Fill in the group password";
	
	private JButton bLogin;
	
	private Main main;
	
	private boolean nameTyped = false;
	private boolean passwordTyped = false;

	/** Constructs a LoginGUI object. */
	public LoginGUI(Main main) {
		this.main = main;
		buildGUI();
		setResizable(false);
		setVisible(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				e.getWindow().dispose();
			}
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	/** builds the GUI. */
	public void buildGUI() {
		setSize(windowSize);
		setWindowLocation();
		// declare and create menu
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
		menuBar.add(optionMenu);
		setJMenuBar(menuBar);

		// declare all panels
		JPanel fullPanel = new JPanel(new FlowLayout());
		JPanel panels = new JPanel(new GridBagLayout());
		//panel1.setBackground(new Color(34,169,220));
		JPanel panelLabels = new JPanel(new GridLayout(2, 0, 2, 4));
		JPanel panelFields = new JPanel(new GridLayout(2, 0, 2, 4));
		JPanel panelButtons = new JPanel(new GridLayout(1, 0, 2, 4));

		GridBagConstraints panelLabelsC = new GridBagConstraints(
				0, 0, 1, 2, 1D, 1D, GridBagConstraints.CENTER, 0, PADDING, 0, 0);
		GridBagConstraints panelFieldsC = new GridBagConstraints(
				1, 0, 2, 2, 1D, 1D, GridBagConstraints.CENTER, 0, PADDING, 0, 0);
		GridBagConstraints panelButtonsC = new GridBagConstraints(
				3, 0, 1, 2, 1D, 1D, GridBagConstraints.CENTER, 0, PADDING, 0, 0);

		JLabel lbName = new JLabel("Username: ");
		name = new JTextField("",20);
		name.setToolTipText(nameToolTip);
		name.addKeyListener(this);

		panelLabels.add(lbName);
		panelFields.add(name);	

		// create pass panel
		JLabel lbPass = new JLabel("Password: ");
		password = new JPasswordField("",20);
		password.setToolTipText(passToolTip);
		password.addKeyListener(this);

		panelLabels.add(lbPass);
		panelFields.add(password);	

		panels.add(panelLabels, panelLabelsC);
		panels.add(panelFields, panelFieldsC);

		// create button panel
		bLogin = new JButton("Login");
		bLogin.setBackground(Color.WHITE);
		bLogin.addActionListener(this);
		bLogin.setEnabled(false);
		
		panelButtons.add(bLogin);
		
		panels.add(panelButtons, panelButtonsC);
		fullPanel.add(panels);
		add(fullPanel);
		
	}

	public boolean isValidPassword(String s){
		return s != null && s.length() >= 6 && !s.contains(" ");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src.equals(bLogin)) {
			login();
		}	
	}
	
	private void login() {
		String theName = name.getText();
		String thePass = new String(password.getPassword());
		if (isValidPassword(thePass)) {
			if (main.tryLogin(theName, thePass)) {
				setVisible(false);
				reset();
				main.login();
			}
		} else {
			addPopup("Password error", theName + ", the password is not valid!\n" +
					"The password should at least consist of 6 characters and " +
					"should contain no spaces", true);
		}
	}
	
	public void reset() {
		name.setText("");
		password.setText("");
		nameTyped = false;
		passwordTyped = false;
		updateLoginButton();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		Object trigger = e.getSource();
		if (e.getKeyChar() == '\n' && nameTyped && passwordTyped) {
			login();
		} else {
			updateFieldBooleans(e, (JTextField) trigger);
			updateLoginButton();
		}

	}

	private void updateFieldBooleans(KeyEvent e, JTextField item) {
		String s = item.getText() + e.getKeyChar();
		boolean validInput = containsLetterOrNumber(s);
		if (item.equals(name)) {
			nameTyped = validInput;
			if (name.getText().length() > MAX_LENGTH) {
				addPopup("Username too long", "Max username length is " + MAX_LENGTH, false);
				name.setText(name.getText().substring(0, MAX_LENGTH));
			}
		} else if (item.equals(password)) {
			passwordTyped = validInput;
			if (new String(password.getPassword()).length() > MAX_LENGTH) {
				addPopup("Username too long", "Max password length is " + MAX_LENGTH, false);
				password.setText(new String(password.getPassword()).substring(0, MAX_LENGTH));
			}
		}
		
	}

	private void updateLoginButton() {
		if (nameTyped && passwordTyped && !bLogin.isEnabled()) {
			bLogin.setEnabled(true);
		} else if ((!nameTyped || !passwordTyped) && bLogin.isEnabled()) {
			bLogin.setEnabled(false);
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

	@Override
	public void keyPressed(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}


	public void addPopup(String title, String message, boolean warning) {
		if (!warning) {
			JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
		}

	}

}
