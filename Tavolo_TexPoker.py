import tkinter as tk
from PIL import Image, ImageTk
import socket
import threading
import random

carta_coperta_label = None
"""
client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect(("127.0.0.1", 7777))

risposta = client.recv(1024).decode()
print(risposta)

client.send("CONNESSO\n".encode())
"""
def carica_carta(percorso_file, larghezza, altezza):
    """
    Funzione che carica un'immagine, la ridimensiona e
    restituisce l'oggetto pronto per Tkinter.
    """
    img_originale = Image.open(percorso_file)
    # 2. Ridimensiona l'immagine (metodo LANCZOS per alta qualità)
    img_ridimensionata = img_originale.resize((larghezza, altezza), Image.Resampling.LANCZOS)
    
    # 3. Converte per Tkinter
    foto_finale = ImageTk.PhotoImage(img_ridimensionata)
    return foto_finale

#Creare la finestra principale
root = tk.Tk()
root.title("Poker Texano")
root.geometry("1000x1000")

# Imposta il vero schermo intero (senza barra del titolo)
root.attributes('-fullscreen', True)

# Scorciatoia comoda per uscire premendo ESC
root.bind("<Escape>", lambda event: root.attributes('-fullscreen', False))

# 2. Creare un Frame
# bg="darkgreen" serve per vederlo chiaramente, bd=5 è il bordo
mio_frame = tk.Frame(root, bg="darkgreen", bd=5, relief="sunken")

# 3. Posizionare il Frame nella finestra
mio_frame.pack(fill="both", expand=True, padx=20, pady=20)

# 2. Rettangolo centrale (l'area delle carte del banco)
# Ho messo un colore leggermente diverso (#005500) per farlo risaltare sul darkgreen
frame_banco = tk.Frame(mio_frame, bg="#005500", bd=5, relief="ridge", width=600, height=250)

# expand=True e anchor=tk.CENTER lo terranno perfettamente al centro in verticale e orizzontale.
frame_banco.pack(expand=True, anchor=tk.CENTER)

# 3. Blocchiamo le dimensioni!
# Senza questo, appena aggiungerai la prima carta, il rettangolo si rimpicciolirà attorno ad essa.
frame_banco.pack_propagate(False)


# Creo i 4 frame per i 4 possibili giocatori
frame_sotto = tk.Frame(mio_frame, bg="#005500", bd=5, relief="ridge", width=400, height=250)
frame_sotto.place(relx=0.5, rely=1.0, anchor=tk.S)
frame_sotto.pack_propagate(False)


frame_sopra = tk.Frame(mio_frame, bg="#005500", bd=5, relief="ridge", width=400, height=250)
frame_sopra.place(relx=0.5, rely=0.0, anchor=tk.N)
frame_sopra.pack_propagate(False)


frame_sinistro = tk.Frame(mio_frame, bg="#005500", bd=5, relief="ridge", width=250, height=400)
frame_sinistro.place(relx=0.0, rely=0.5, anchor=tk.W)
frame_sinistro.pack_propagate(False)


frame_destro = tk.Frame(mio_frame, bg="#005500", bd=5, relief="ridge", width=250, height=400)
frame_destro.place(relx=1.0, rely=0.5, anchor=tk.E, )
frame_destro.pack_propagate(False)


# Creo il bottone per la puntata
bottone_puntata = tk.Button(mio_frame, text="BET", width=15, font=("Arial", 12, "bold"), bg="orange")
bottone_puntata.place(relx=0.99, rely=0.99, anchor=tk.SE)
bottone_puntata.config(state="disabled")

# Bottone Fold
bottone_fold = tk.Button(mio_frame, text="FOLD", width=15, font=("Arial", 12, "bold"), bg="orange")
bottone_fold.place(relx=0.88, rely=0.99, anchor=tk.SE)
bottone_fold.config(state="disabled")


# Bottone Call
bottone_call = tk.Button(mio_frame, text="CALL", width=15, font=("Arial", 12, "bold"), bg="orange")
bottone_call.place(relx=0.88, rely=0.94, anchor=tk.SE)
bottone_call.config(state="disabled")


# Bottone All-in
bottone_allin = tk.Button(mio_frame, text="ALL-IN", width=15, font=("Arial", 12, "bold"), bg="orange")
bottone_allin.place(relx=0.99, rely=0.94, anchor=tk.SE)


# Bottone per uscire dal gioco
bottone_exit = tk.Button(mio_frame, text="ESCI", width=15, font=("Arial", 12, "bold"), bg="orange")
bottone_exit.place(relx=0.99, rely=0.01, anchor=tk.NE)

# Funzione di dominio della puntata
def controlla_puntata(testo):
    if testo == "":
        bottone_puntata.config(state="disabled")
        return True
    elif testo.isdigit():
        bottone_puntata.config(state="normal")
        return True
    else:
        return False

# Tkinter ha bisogno che questa funzione venga "registrata" per poterla usare dentro un widget
validazione_puntata = root.register(controlla_puntata)


# Creo l'area di testo dove scrivere la puntata
testo_bet = tk.Entry(mio_frame, width=10, font=("Arial", 14), justify="center", validate="key", validatecommand=(validazione_puntata, '%P'))
testo_bet.place(relx=0.65, rely=0.965, anchor=tk.SW)

"""
def ascolta_server():
    global carta_coperta_label
    
    # Funzione interna per disegnare in sicurezza
    def disegna_carta_sicura(frame_destinazione, percorso_img, salva_in_variabile_globale=False):
        try:
            img = carica_carta(percorso_img, 100, 150)
            lbl = tk.Label(frame_destinazione, image=img, bg="forestgreen")
            lbl.pack(side=tk.LEFT, padx=5)
            lbl.image = img # Mantiene il riferimento
            
            if salva_in_variabile_globale:
                global carta_coperta_label
                carta_coperta_label = lbl
        except Exception as e:
            print(f"Errore grafico: {e}")

    # Funzione interna per distruggere carta coperta
    def rimuovi_carta_coperta():
        global carta_coperta_label
        if carta_coperta_label is not None:
            carta_coperta_label.destroy()
            carta_coperta_label = None

    while True:
        try:
            # Controllo vita finestra
            try:
                if not root.winfo_exists(): break
            except: break

            dati = client.recv(4096).decode()
            if not dati: break
            
            for msg in dati.split("\n"):
                msg = msg.strip()
                if not msg: continue
                
                print(f"MSG: {msg}") # Debug
                
                # --- GESTIONE CARTA GIOCATORE ---
                if "CARTA:" in msg:
                    # Passa alla grafica di gioco (in sicurezza)
                    root.after(0, mostra_fase_gioco)
                    
                    frame_dest = frame_sotto
                    nome = ""
                    
                    nome = msg.replace("CARTA_DX:", "")
                    
                    path = r"PNG/" + nome + ".png"
                    # Ordina a Tkinter di disegnare (nel thread principale!)
                    root.after(0, lambda f=frame_dest, p=path: disegna_carta_sicura(f, p))

                # --- GESTIONE BANCO ---
                elif "BANCO:" in msg:
                    if "retro" in msg:
                        path = r"PNG/retro" + ".png"
                        # Disegna e salva come carta coperta
                        root.after(0, lambda f=carte_banco, p=path: disegna_carta_sicura(f, p, True))
                    else:
                        # Rimuovi la vecchia carta coperta
                        root.after(0, rimuovi_carta_coperta)
                        
                        nome = msg.replace("BANCO:", "")
                        path = r"PNG/" + nome + ".png"
                        # Disegna la carta svelata/nuova
                        root.after(0, lambda f=carte_banco, p=path: disegna_carta_sicura(f, p))

                # --- ALTRI COMANDI ---
                elif "SALDO:" in msg:
                    parti = msg.replace("SALDO:", "").split(":")
                    soldi, puntata = parti[0], parti[1]
                    root.after(0, lambda: root.title(f"BLACKJACK - Saldo: {soldi}€ | Puntata: {puntata}€"))
                
                elif "MSG:" in msg:
                    txt = msg.replace("MSG:", "")
                    root.after(0, lambda: label_info.config(text=txt, fg="yellow"))
                    keywords = ["VINTO", "VINCE", "PERSO", "PERDE", "PAREGGIO", "PARI", "SBALLATO", "SBALLA"]
                    
                    # TRUCCO: Convertiamo il messaggio in maiuscolo (.upper()) prima di controllare
                    if any(parola in txt.upper() for parola in keywords):
                        root.after(0, lambda: btn_carta.config(state="disabled"))
                        root.after(0, lambda: btn_stai.config(state="disabled"))
                        root.after(0, lambda: btn_split.config(state="disabled"))
                        root.after(0, lambda: btn_rigioca.config(state="normal"))
                        

        except Exception as e:
            print("Errore ascolto:", e)
            break

        except Exception as e:
            print("Errore ricezione:", e)
            break

def chiudi_gioco():
    try:
        client.send("ESCI\n".encode())
    except Exception as e:
        print("ERRORE NELL'INVIO", e)
    root.destroy()
    
# Collega la funzione al click del mouse
bottone_exit.config(command=chiudi_gioco)


def sto():
    try:
        client.send("STO\n".encode())
    except Exception as e1:
        print("ERRORE NELL'INVIO", e1)
        
btn_stai.config(command=sto)


def carta():
    try:
        client.send("CARTA\n".encode())
    except Exception as e2:
        print("ERRORE NELL'INVIO", e2)
        
btn_carta.config(command=carta)

def pulisci_tavolo():
    try:
        # Pulisce le carte
        for widget in carte_playerD.winfo_children(): widget.destroy()
        for widget in carte_banco.winfo_children(): widget.destroy()
        
        global carta_coperta_label
        carta_coperta_label = None
        
        label_info.config(text="Nuova partita...", fg="white")
        
        # CAMBIO SCENA (Senza parentesi!)
        root.after(0, mostra_fase_scommessa)
        
        # Reset Pulsanti
        btn_carta.config(state="normal")
        btn_stai.config(state="normal")
        btn_split.config(state="disabled")
        btn_rigioca.config(state="disabled")
        
    except Exception as e:
        print(f"Errore durante pulizia tavolo: {e}")

def rigioca():
    try:
        pulisci_tavolo()
        
        client.send("RESET\n".encode())
    except Exception as e3:
        print("ERRORE NELL'INVIO", e3)
        
btn_rigioca.config(command=rigioca)


def mostra_fase_scommessa():
    # Nascondi i comandi di gioco
    frame_comandi.pack_forget() 
    # Mostra le scommesse
    frame_scommesse.pack()
    # Aggiorna testo
    label_info.config(text="Fai la tua puntata...", fg="white")

def mostra_fase_gioco():
    # Nascondi le scommesse
    frame_scommesse.pack_forget()
    # Mostra i comandi di gioco
    frame_comandi.pack()
    # Aggiorna testo
    label_info.config(text="Partita in corso", fg="yellow")
    
def manda_bet(valore):
    try:
        messaggio = f"BET:{valore}\n" # <--- IL \n È FONDAMENTALE!
        print(f"PYTHON PROVA A INVIARE: '{messaggio.strip()}'")
        client.send(messaggio.encode())
        print("PYTHON: Invio completato.")
    except Exception as e:
        print(f"PYTHON ERRORE INVIO: {e}")

def manda_deal():
    try:
        print("PYTHON PROVA A INVIARE: 'DEAL'")
        client.send("DEAL\n".encode()) # <--- ANCHE QUI IL \n
    except Exception as e:
        print(f"PYTHON ERRORE DEAL: {e}")
        
btn_deal.config(command=manda_deal)
# ... tutto il tuo codice grafico ...

# AVVIO DEL THREAD DI ASCOLTO
t = threading.Thread(target=ascolta_server)
t.daemon = True 
t.start()

# --- PRIMA IL LIFT (ALZARE LA FINESTRA) ---
root.lift()
root.attributes('-topmost',True)
root.after_idle(root.attributes,'-topmost',False)

# Intercetta la chiusura della finestra
root.protocol("WM_DELETE_WINDOW", chiudi_gioco)
# --- POI IL MAINLOOP (FINE DEL CODICE) ---
root.after(100, mostra_fase_scommessa)

# %%
"""
root.mainloop()
