package applicationLayer;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class UserInterface implements ActionListener{

	public UserInterface(){

	}

	JTextField textfield1 = new JTextField();
	JTextField textfield2 = new JTextField();
	JTextArea textarea1 = new JTextArea();
	JTextArea textarea2 = new JTextArea();
	JButton button1 = new JButton();
	JButton button2 = new JButton();
	JFrame frame1 = new JFrame();
	JPanel panel1;

	public void createInterface(){

		button1.addActionListener(this);
		textarea1.setEditable(false);
		textarea2.setEditable(false);
		textarea2.append("Connected devices:");

		panel1 = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();
		GridBagConstraints e = new GridBagConstraints();
		GridBagConstraints f = new GridBagConstraints();

		//panel1.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
		d.fill = GridBagConstraints.HORIZONTAL;
		d.weightx = 1.0;
		d.weighty = 1.0;
		d.gridx = 0;
		d.gridy = 0;
		d.ipady = 500;
		d.ipadx = 800;
		d.insets = new Insets(10,10,10,10);
		Border border1 = new LineBorder(new Color(34,220,214), 10 ,false);
		textarea1.setBorder(border1);
		textarea1.setFont(new Font("Calibri", Font.ITALIC, 22));
		JScrollPane scrollpane = new JScrollPane(textarea1);
		JPanel listPane1 = new JPanel();
		listPane1.setLayout(new BoxLayout(listPane1, BoxLayout.LINE_AXIS));
		listPane1.add(scrollpane);
		panel1.add(listPane1,d);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridx = 1;
		c.gridy = 0;
		c.ipady = 500;
		c.ipadx = 175;
		c.insets = new Insets(10,10,10,10);
		Border border2 = new LineBorder(new Color(34,121,220), 10 ,false);
		textarea2.setBorder(border2);
		textarea2.setFont(new Font("Calibri", Font.ITALIC, 22));
		JScrollPane scrollpane2 = new JScrollPane(textarea2);
		JPanel listPane2 = new JPanel();
		listPane2.setLayout(new BoxLayout(listPane2, BoxLayout.Y_AXIS));
		listPane2.add(scrollpane2);
		panel1.add(listPane2,c);

		e.fill = GridBagConstraints.HORIZONTAL;
		e.weightx = 1.0;
		e.weighty = 1.0;
		e.gridx = 1;
		e.gridy = 1;
		e.ipady = 150;
		e.ipadx = 175;
		e.insets = new Insets(10,10,10,10);
		Border border3 = new LineBorder(new Color(34,220,214), 10 ,false);
		
		button1.setBorder(border3);
		button1.setFont(new Font("Calibri", Font.ITALIC, 40));
		button1.setBackground(new Color(255,255,255));
		button1.setForeground(new Color(34,121,220));
		button1.setText("Send");
		JScrollPane scrollpane5 = new JScrollPane(button1);
		JPanel listPane4 = new JPanel();
		listPane4.setLayout(new BoxLayout(listPane4, BoxLayout.Y_AXIS));
		listPane4.add(scrollpane5);
		panel1.add(listPane4,e);

		f.fill = GridBagConstraints.HORIZONTAL;
		f.weightx = 1.0;
		f.weighty = 1.0;
		f.gridx = 0;
		f.gridy = 1;
		f.ipady = 150;
		f.ipadx = 800;
		f.insets = new Insets(10,10,10,10);
		Border border4 = new LineBorder(new Color(34,121,220), 10 ,false);
		textfield1.setBorder(border4);
		textfield1.setText("Input text here:");
		MouseAdapter myListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				textfield1.setText(null);
				textfield1.removeMouseListener(this);
			}
		};
		textfield1.addMouseListener(myListener);
		textfield1.setFont(new Font("Calibri", Font.ITALIC, 22));
		JPanel listPane3 = new JPanel();
		listPane3.setLayout(new BoxLayout(listPane3, BoxLayout.PAGE_AXIS));
		listPane3.add(textfield1);
		panel1.add(listPane3,f);

		panel1.setBackground(new Color(34,169,220));
		frame1.setVisible(true);
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame1.setResizable(true);
		frame1.add(panel1);
		frame1.pack();
		frame1.setSize(1280,800);

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == button1){
			String s = textfield1.getText();
			if (!s.equals("Input text here:")&&!s.equals("")){
				textarea1.append(s+"\n");
				textfield1.setText(null);
			}
		}
	}


	public static void main(String[] args) {
		UserInterface ui = new UserInterface();
		ui.createInterface();
	}
}
