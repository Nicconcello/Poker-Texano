package server;

import java.util.ArrayList;
import java.util.Collections;

public class Mazzo {
	private ArrayList<Carta> mazzo;
	
	public Mazzo() {
		mazzo = new ArrayList<>();
		String[] semi = {"cuori", "quadri", "fiori", "picche"};
		String[] ranghi = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "q", "k", "a"};
		
		for(int se = 0; se < 4; se++) {
			for(int ra = 0; ra < 13; ra++) {
				if(ranghi[ra].equals("j") || ranghi[ra].equals("q") || ranghi[ra].equals("k")) {
					Carta fig = new Carta(ranghi[ra], 10, semi[se]);
					mazzo.add(fig);
				} else if(ranghi[ra].equals("a")) {
					Carta asso = new Carta(ranghi[ra], 11, semi[se]);
					mazzo.add(asso);
				} else {
					int num = Integer.parseInt(ranghi[ra]);
					Carta numero = new Carta(ranghi[ra], num, semi[se]);
					mazzo.add(numero);
				}
			}
		}
	}
	
	public void mischia() {
        Collections.shuffle(mazzo);
    }
	
	public Carta pesca() {
		if(!mazzo.isEmpty()) {
			return mazzo.remove(0);
		} else {
			System.out.println("IL MAZZO è VUOTO");
			return null;
		}
	}
}
