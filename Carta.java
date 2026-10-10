package server;

public class Carta {
	private String rango;
	private int valore;
	private String seme;
	
	public Carta(String rango, int valore, String seme) {
		this.rango = rango;
		this.valore = valore;
		this.seme = seme;
	}
	
	public int getValore() {return valore;}
	
	public String getNomeFile() {
		return rango + "_" + seme;
	}
}
