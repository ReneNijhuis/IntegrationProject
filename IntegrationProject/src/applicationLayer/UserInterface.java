package applicationLayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
				
		button1.setText("Send");
		button1.addActionListener(this);
		textarea1.setEditable(false);
		textarea2.setEditable(false);
		textarea2.append("Connected devices:");
		
		panel1 = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();
		GridBagConstraints e = new GridBagConstraints();
		GridBagConstraints f = new GridBagConstraints();
		
		panel1.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
		d.fill = GridBagConstraints.HORIZONTAL;
		d.weightx = 1.0;
		d.gridx = 0;
		d.gridy = 0;
		d.ipady = 500;
		d.ipadx = 800;
		d.insets = new Insets(10,10,10,10);
		Border border1 = new LineBorder(new Color(34,220,214), 5 ,false);
		textarea1.setBorder(border1);
		JScrollPane scrollpane = new JScrollPane(textarea1);
		JPanel listPane1 = new JPanel();
		listPane1.setLayout(new BoxLayout(listPane1, BoxLayout.LINE_AXIS));
		listPane1.add(scrollpane);
		listPane1.setPreferredSize(new Dimension(100, 50));
		listPane1.setMaximumSize(listPane1.getPreferredSize());
		panel1.add(listPane1,d);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = 0;
		c.ipady = 500;
		c.ipadx = 175;
		c.insets = new Insets(10,10,10,10);
		Border border2 = new LineBorder(new Color(34,121,220), 5 ,false);
		textarea2.setBorder(border2);
		JScrollPane scrollpane2 = new JScrollPane(textarea2);
		JPanel listPane2 = new JPanel();
		listPane2.setLayout(new BoxLayout(listPane2, BoxLayout.Y_AXIS));
		listPane2.add(scrollpane2);
		listPane2.setPreferredSize(new Dimension(50, 25));
		panel1.add(listPane2,c);
		
		e.fill = GridBagConstraints.HORIZONTAL;
		e.weightx = 1.0;
		e.gridx = 1;
		e.gridy = 1;
		e.ipady = 150;
		e.ipadx = 175;
		e.insets = new Insets(10,10,10,10);
		Border border3 = new LineBorder(new Color(34,220,214), 5 ,false);
		button1.setBorder(border3);
		button1.setBackground(new Color(34,121,220));
		panel1.add(button1, e);
		
		f.fill = GridBagConstraints.HORIZONTAL;
		f.weightx = 1.0;
		f.gridx = 0;
		f.gridy = 1;
		f.ipady = 150;
		f.ipadx = 800;
		f.insets = new Insets(10,10,10,10);
		Border border4 = new LineBorder(new Color(34,121,220), 5 ,false);
		textfield1.setBorder(border4);
		JPanel listPane3 = new JPanel();
		listPane3.setLayout(new BoxLayout(listPane3, BoxLayout.PAGE_AXIS));
		listPane3.add(textfield1);
		listPane3.setPreferredSize(new Dimension(50, 25));
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
		textarea1.append(textfield1.getText()+"\n");
	}
	
		
	public static void main(String[] args) {
		
	    try {
	            // Set System L&F
	        UIManager.setLookAndFeel(
	            UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (UnsupportedLookAndFeelException e) {
	       // handle exception
	    }
	    catch (ClassNotFoundException e) {
	       // handle exception
	    }
	    catch (InstantiationException e) {
	       // handle exception
	    }
	    catch (IllegalAccessException e) {
	       // handle exception
	    }

	    UserInterface ui = new UserInterface();
		ui.createInterface();
		
	}
}
