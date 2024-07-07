package org.example.GestioneAppello;

import java.util.ArrayList;

public class Esito
{
    private int punteggio;
    private ArrayList<String> risposteCorrette;
    private boolean superato;

    public Esito(int punteggio, ArrayList<String> risposteCorrette, boolean superato)
    {
        this.punteggio = punteggio;
        this.risposteCorrette = risposteCorrette;
        this.superato = superato;
    }

    public int getPunteggio() {
        return punteggio;
    }

    public ArrayList<String> getRisposteCorrette() {
        return risposteCorrette;
    }

    public boolean isSuperato() {
        return superato;
    }
}
