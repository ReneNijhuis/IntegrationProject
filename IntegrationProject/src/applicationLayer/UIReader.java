package applicationLayer;

import java.util.Observable;
import java.util.Observer;

import NetwerkSystemen.UserInterface;

public class UIReader implements Observer{
	public UIReader(){
		MainUI ui = new MainUI();
		ui.addObserver(this);
	}
	public byte[] getUserInput(){
		return null;
	}
	@Override
	public void update(Observable arg0, Object arg1) {
		System.out.println((String)arg1);
		
	}

}
