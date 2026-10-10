package server;

import java.net.*;
import java.io.*;
import java.util.*;

public class ServerTexPoker {

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(7777);
            System.out.println("SERVER AVVIATO. Attendo connessioni...");

            while (true) {
                Socket s = ss.accept();
                System.out.println("Utente connesso");

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                pw.println("MSG:Benvenuto! Inizia la partita.");
                
                int saldo = 1000;
                
                // LOOP SESSIONE
                while (true) {
                	int puntataCorrente = 0;
                    boolean utenteDisconnesso = false;
                    
                    // 1. Inviamo lo stato iniziale a Python
                    System.out.println(">> INIZIO SCOMMESSE. Saldo: " + saldo);
                    pw.println("SALDO:" + saldo + ":" + puntataCorrente);
                    pw.println("MSG:Fai la tua puntata!"); // Scrive nella barra gialla
                    
                    // 2. CICLO DI ATTESA SCOMMESSA
                    while(true) {
                        System.out.println("... Java attende comando (BET/DEAL) ...");
                        String cmd = in.readLine();
                        
                        // Protezione: se Python si chiude improvvisamente
                        if (cmd == null) {
                            System.out.println("ERRORE: Client perso durante le scommesse.");
                            utenteDisconnesso = true;
                            break; // Esce dal main
                        }
                        
                        System.out.println("RICEVUTO: " + cmd);

                        // --- GESTIONE FICHES ---
                        if (cmd.equals("BET:10")) {
                            if (saldo >= 10) {
                                saldo -= 10;
                                puntataCorrente += 10;
                                System.out.println("-> Puntati 10. Nuovo saldo: " + saldo);
                            } else {
                                pw.println("MSG:Non hai abbastanza soldi!");
                            }
                        } 
                        else if (cmd.equals("BET:50")) {
                            if (saldo >= 50) {
                                saldo -= 50;
                                puntataCorrente += 50;
                                System.out.println("-> Puntati 50. Nuovo saldo: " + saldo);
                            } else {
                                pw.println("MSG:Non hai abbastanza soldi!");
                            }
                        } 
                        else if (cmd.equals("BET:100")) {
                            if (saldo >= 100) {
                                saldo -= 100;
                                puntataCorrente += 100;
                                System.out.println("-> Puntati 100. Nuovo saldo: " + saldo);
                            } else {
                                pw.println("MSG:Non hai abbastanza soldi!");
                            }
                        }
                        else if (cmd.equals("BET:ALL-IN")) {
                            if (saldo > 0) {
                                puntataCorrente += saldo;
                                saldo = 0;
                                System.out.println("-> Puntati tutti i soldi. Nuovo saldo: " + saldo);
                            } else {
                                pw.println("MSG:Non hai abbastanza soldi!");
                            }
                        }
                        
                        // --- GESTIONE START (DEAL) ---
                        else if (cmd.equals("DEAL")) {
                            if (puntataCorrente > 0) {
                                System.out.println("-> DEAL ACCETTATO! Si inizia.");
                                break; // <--- ESCE DAL CICLO SCOMMESSE -> VAI ALLE CARTE
                            } else {
                                pw.println("MSG:Devi puntare almeno 10 euro!");
                                System.out.println("-> DEAL Rifiutato (puntata 0)");
                            }
                        }
                        
                        // --- GESTIONE RESET (Mentre scommetti) ---
                        else if (cmd.equals("RESET")) {
                            System.out.println("-> RESET ricevuto durante le scommesse.");
                            // Restituisci i soldi puntati finora
                            saldo += puntataCorrente;
                            puntataCorrente = 0;
                            pw.println("MSG:Puntata annullata. Ricomincia.");
                        }
                        
                        // --- GESTIONE USCITA ---
                        else if (cmd.equals("ESCI") || cmd.equals("QUIT")) {
                            System.out.println("-> Utente vuole uscire.");
                            // Trucco: impostiamo una variabile per uscire anche dal loop esterno
                            // Ma per ora basta chiudere il socket qui o fare return
                            s.close();
                            utenteDisconnesso = true;
                            break; 
                        }

                        // AGGIORNA SEMPRE L'INTERFACCIA PYTHON
                        pw.println("SALDO:" + saldo + ":" + puntataCorrente);
                    }

                    if (utenteDisconnesso) {
                        break; // Rompe il LOOP SESSIONE e va dritto giù a fare s.close()
                    }
                	
                    Mazzo mazzo = new Mazzo();
                    mazzo.mischia();

                    // --- MODIFICA RICHIESTA: Creiamo le liste prima ---
                    ArrayList<Carta> listaPlayer = new ArrayList<>();
                    Mano mano_player = new Mano(listaPlayer); 

                    ArrayList<Carta> listaBanco = new ArrayList<>();
                    Mano mano_banco = new Mano(listaBanco);

                    // Distribuzione
                    for (int p = 0; p < 2; p++) {
                        mano_player.aggiungiCarta(mazzo.pesca());
                        mano_banco.aggiungiCarta(mazzo.pesca());
                    }

                    // Invio carte Player
                    for (Carta c : mano_player.getCarte()) {
                        pw.println("CARTA:" + c.getNomeFile());
                    }

                    // Invio carte Banco
                    ArrayList<Carta> cartadelBanco = mano_banco.getCarte();
                    for (int b = 0; b < cartadelBanco.size(); b++) {
                        if (b == 1) pw.println("BANCO:retro");
                        else pw.println("BANCO:" + cartadelBanco.get(b).getNomeFile());
                    }

                    // Controllo SPLIT Preliminare
                    if (mano_player.getCarte().get(0).getValore() == mano_player.getCarte().get(1).getValore()) {
                        pw.println("split");
                    }

                    
                    
                 // DEBUG: Vediamo cosa pensa Java
                    Carta c1 = mano_player.getCarte().get(0);
                    Carta c2 = mano_player.getCarte().get(1);
                    System.out.println("DEBUG: Carta 1 (" + c1.getNomeFile() + ") vale: " + c1.getValore());
                    System.out.println("DEBUG: Carta 2 (" + c2.getNomeFile() + ") vale: " + c2.getValore());

                    // CONTROLLO SPLIT (Deve essere FUORI dal while!)
                    if (c1.getValore() == c2.getValore()) {
                        pw.println("split");
                        System.out.println("DEBUG: --- INVIO COMANDO SPLIT ---");
                    } else {
                        System.out.println("DEBUG: --- NO SPLIT ---");
                    }

                    System.out.println("DEBUG: Entro nel while e aspetto comandi...");
                    
                    boolean voglioUscire = false;
                    
                    // LOOP PARTITA
                    while (true) {
                        String comando = in.readLine();

                        if (comando == null || comando.equals("ESCI") || comando.equals("QUIT")) {
                            voglioUscire = true;
                            break; // Esce dal Loop 3
                        }
                        
                        if (comando == null) break;
                        if (comando.equals("RESET")) break;
                        //if (comando.equals("ESCI")) break;

                        int punteggioP = mano_player.punteggio();

                        // --- GESTIONE SPLIT ---
                        if (comando.equals("SPLIT")) {
                        	int puntataCorrenteD = puntataCorrente;
                        	int puntataCorrenteS = puntataCorrente;
                        	saldo -= puntataCorrenteS;
                        	pw.println("SALDO:" + saldo + ":" + (puntataCorrenteD + puntataCorrenteS)); // Aggiorna grafica
                        	
                            ArrayList<Carta> carteOriginali = mano_player.getCarte();

                            // 1. Setup Mano Destra
                            ArrayList<Carta> listaD = new ArrayList<>();
                            Mano mano_destra = new Mano(listaD); // Passo la lista vuota
                            mano_destra.aggiungiCarta(carteOriginali.get(1)); // Sposto la carta
                            mano_destra.aggiungiCarta(mazzo.pesca()); // Rifornisco

                            // 2. Setup Mano Sinistra
                            ArrayList<Carta> listaS = new ArrayList<>();
                            Mano mano_sinistra = new Mano(listaS); // Passo la lista vuota
                            mano_sinistra.aggiungiCarta(carteOriginali.get(0)); // Sposto la carta
                            mano_sinistra.aggiungiCarta(mazzo.pesca()); // Rifornisco

                            // Aggiorna Grafica Python
                            for (Carta c : mano_destra.getCarte()) pw.println("CARTA_DX:" + c.getNomeFile());
                            for (Carta c : mano_sinistra.getCarte()) pw.println("CARTA_SX:" + c.getNomeFile());

                            // Gioca Destra                           
                            while (true) {
                                // IL FRENO: Java si DEVE fermare qui ad aspettare te!
                                String cmnd = in.readLine(); 
                                
                                // Protezione
                                if (cmnd == null || cmnd.equals("RESET")) break;
                                
                                // Se premi STO, smettiamo di dare carte a questa mano
                                if (cmnd.equals("STO")) break; 
                                
                                // Se premi CARTA, ne diamo UNA SOLA e poi torniamo ad aspettare
                                if (cmnd.equals("CARTA")) {
                                    Carta pesc = mazzo.pesca();
                                    mano_destra.aggiungiCarta(pesc);
                                    pw.println("CARTA_DX:" + pesc.getNomeFile());
                                    
                                    // Controllo sballato
                                    if (mano_destra.punteggio() > 21) {                                       
                                        try { Thread.sleep(1000); } catch (Exception e) {}
                                        puntataCorrenteD = 0;
                                        break; // Esce dal ciclo perché hai perso questa mano
                                    }
                                }
                            }

                            // Gioca Sinistra                     
                            while (true) {
                                // IL FRENO: Aspettiamo di nuovo te!
                                String cmns = in.readLine();
                                
                                if (cmns == null || cmns.equals("RESET")) break;
                                
                                if (cmns.equals("STO")) break;
                                
                                if (cmns.equals("CARTA")) {
                                    Carta pesc = mazzo.pesca();
                                    mano_sinistra.aggiungiCarta(pesc);
                                    pw.println("CARTA_SX:" + pesc.getNomeFile());
                                    
                                    if (mano_sinistra.punteggio() > 21) {                                      
                                        try { Thread.sleep(1000); } catch (Exception e) {}
                                        puntataCorrenteS = 0;
                                        break;
                                    }
                                }
                            }

                            // Banco Gioca
                            pw.println("BANCO:" + cartadelBanco.get(1).getNomeFile());
                            while (mano_banco.punteggio() < 17) {
                                try { Thread.sleep(1000); } catch (Exception e) {}
                                Carta pesc = mazzo.pesca();
                                mano_banco.aggiungiCarta(pesc);
                                pw.println("BANCO:" + pesc.getNomeFile());
                            }

                            // Verdetto Split
                            int pB = mano_banco.punteggio();
                            int pD = mano_destra.punteggio();
                            int pS = mano_sinistra.punteggio();
                            
                            String esito = "MSG:BANCO(" + pB + ") | ";
                            if (pD > 21) esito += "DX:Sballato ";
                            else if (pB > 21 || pD > pB) {
                            	esito += "DX:Ha Vinto ";
                            	saldo += puntataCorrenteD * 2;
                            	puntataCorrenteD = 0;
                            	puntataCorrente = 0;
                            } else if (pD == pB) {
                                esito += "DX:Pareggio ";
                                saldo += puntataCorrenteD; // Ti ridò i soldi
                                puntataCorrenteD = 0;
                            	puntataCorrente = 0;
                            }
                            else {
                            	esito += "DX:Perso ";
                            	puntataCorrenteD = 0;
                             	puntataCorrente = 0;
                            }
                            
                            if (pS > 21) esito += "SX:Sballato";
                            else if (pB > 21 || pS > pB) {
                            	esito += "SX:Ha Vinto";
                            	saldo += puntataCorrenteS * 2;
                            	puntataCorrenteS = 0;
                            	puntataCorrente = 0;
                            } else if (pS == pB) { 
                                esito += "SX:Pareggio ";
                                saldo += puntataCorrenteS; // Ti ridò i soldi
                                puntataCorrenteS = 0;
                            	puntataCorrente = 0;
                            }
                            else {
                            	esito += "SX:Perso ";
                            	puntataCorrenteS = 0;
                             	puntataCorrente = 0;
                            }

                            pw.println(esito);
                            pw.println("SALDO:" + saldo + ":0");
                            break; // Fine Split                                               
                        }

                        // --- GESTIONE STO NORMALE ---
                        if (comando.equals("STO")) {
                            pw.println("BANCO:" + cartadelBanco.get(1).getNomeFile());
                            try { Thread.sleep(1000); } catch (Exception e) {}

                            while (mano_banco.punteggio() < 17) {
                                try { Thread.sleep(1000); } catch (Exception e) {}
                                Carta pesc = mazzo.pesca();
                                mano_banco.aggiungiCarta(pesc);
                                pw.println("BANCO:" + pesc.getNomeFile());
                            }

                            int pB = mano_banco.punteggio();
                            if (pB > 21) {
                            	saldo += puntataCorrente * 2;
                            	puntataCorrente = 0;
                            	pw.println("MSG:BANCO SBALLATO! VINCI TU!");
                            }
                            else if (punteggioP < pB) {
                            	puntataCorrente = 0;
                            	pw.println("MSG:Ha Vinto IL BANCO!");
                            }
                            else if (punteggioP > pB) {
                            	saldo += puntataCorrente * 2;
                            	puntataCorrente = 0;
                            	pw.println("MSG:HAI VINTO!");
                            }
                            else {
                            	saldo += puntataCorrente;
                            	puntataCorrente = 0;
                            	pw.println("MSG:PAREGGIO!");
                            }
                            pw.println("SALDO:" + saldo + ":0");
                            break;
                        }

                        // --- GESTIONE CARTA NORMALE ---
                        if (comando.equals("CARTA")) {
                            Carta pesc = mazzo.pesca();
                            mano_player.aggiungiCarta(pesc);
                            pw.println("CARTA:" + pesc.getNomeFile());
                            
                            punteggioP = mano_player.punteggio(); // Aggiorno punteggio
                            if (punteggioP > 21) {
                                puntataCorrente = 0;
                            	pw.println("MSG:HAI SBALLATO! Ha Vinto il BANCO");
                            	pw.println("SALDO:" + saldo + ":0");
                                break;
                            }
                        }
                    } // Fine Loop Partita

                    if (voglioUscire) {
                        System.out.println("Uscita rapida: Salto il Limbo.");
                        break; // ROMPE IL LOOP 2 (Sessione) -> Va a chiudere il socket
                    }
                    
                    // LIMBO
                    String scelta = null;
                    
                    // CICLO DI FILTRAGGIO:
                    // Continua a leggere finché non trova un comando valido.
                    // Ignora righe vuote o comandi vecchi rimasti nel buffer.
                    while (true) {
                        scelta = in.readLine();
                        
                        // Se il client è morto/chiuso
                        if (scelta == null) break; 
                        
                        System.out.println("Ho letto nel limbo: " + scelta); // DEBUG
                        
                        // Se è il comando giusto, usciamo da questo piccolo while di attesa
                        if (scelta.equals("RESET") || scelta.equals("ESCI") || scelta.equals("QUIT")) {
                            break; 
                        }
                        
                        System.out.println("Comando ignorato, aspetto ancora...");
                    }
                    
                    if (scelta != null && scelta.equals("RESET")) {
                        continue;
                    } else {
                        break;
                    }

                } // Fine Loop Sessione
                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}