package org.example.GestioneAppello;

import java.util.ArrayList;

public class Domanda {
    private final String testo;
    private final String rispostaCorretta;
    private final ArrayList<String> risposteSbagliate;
    private static final int numRisposte = 4;


    public Domanda(String testo, String rispostaCorretta, ArrayList<String> risposteSbagliate) {
        this.testo = testo;
        this.rispostaCorretta = rispostaCorretta;
        this.risposteSbagliate = risposteSbagliate;
    }

    public String getTesto() {
        return testo;
    }

    public String getRispostaCorretta() {
        return rispostaCorretta;
    }

    public ArrayList<String> getRisposteSbagliate() {
        return new ArrayList<>(risposteSbagliate);
    }

    public ArrayList<String> getRisposte()
    {
        ArrayList<String> risposte = new ArrayList<>();
        risposte.add(rispostaCorretta);
        risposte.addAll(risposteSbagliate);

        return risposte;
    }

    public int getNumRisposte() {
        return numRisposte;
    }
}
