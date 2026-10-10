package server;

import java.util.ArrayList;

public class Mano {
	private ArrayList<Carta> mano;
	
	public Mano(ArrayList mano) {
		this.mano = mano;
	}
	
	 public void aggiungiCarta(Carta carta) {
		 mano.add(carta);
	 }
	 
	 public ArrayList<Carta> getCarte() {return mano;}
	 
	 public int punteggio() {
		 int totale = 0;
		 int contaAssi = 0 ;
		 
		 for(int c = 0; c < mano.size(); c++) {
			 Carta cartaCorrente = mano.get(c);
			 totale += cartaCorrente.getValore();
			 
			 if(cartaCorrente.getValore() == 11) {
				 contaAssi ++;
			 }
		 }
		 
		 while(totale > 21 && contaAssi > 0) {
			 totale -= 10;
			 contaAssi --;
		 }
		 
		 return totale;
	 }
}
