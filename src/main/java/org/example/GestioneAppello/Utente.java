package org.example.GestioneAppello;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class Utente {
    private String matricola,CF;
    private ArrayList<Appello> appelliPrenotati = new ArrayList<>();

    public Utente() {}

    public Utente(String matricola, String CF){
        this.matricola = matricola;
        this.CF = CF;
    }
    public String getMatricola() {
        return matricola;
    }
    public String getCF() {
        return CF;
    }

    public void setAppello(Appello appello) {
        this.appelliPrenotati.add(appello);
    }


    //PERMETTE DI CONTROLLARE SE VI è SOVRAPPOSIZIONE CON ALTRI APPELLI A CUI L'UTENTE è PRENOTATO
    public boolean isOrarioAccettabile(Appello appello){
        LocalTime inizioNuovoAppello = appello.getOraInizio();
        LocalTime fineNuovoAppello = appello.getOraFine();
        LocalDate dataAppello = appello.getData();
        for(Appello a: appelliPrenotati){
            if(a.getData().isEqual(dataAppello)) {
                LocalTime inizioAppelloEsistente = a.getOraInizio();
                LocalTime fineAppelloEsistente = a.getOraFine();
                if ((inizioNuovoAppello.isAfter(inizioAppelloEsistente) && inizioNuovoAppello.isBefore(fineAppelloEsistente)
                        || (fineNuovoAppello.isAfter(inizioAppelloEsistente) && fineNuovoAppello.isBefore(fineAppelloEsistente))
                        || inizioNuovoAppello.equals(inizioAppelloEsistente) || inizioNuovoAppello.equals(fineAppelloEsistente)
                        || fineNuovoAppello.equals(inizioAppelloEsistente) || fineNuovoAppello.equals(fineAppelloEsistente)))
                    return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utente utente)) return false;
        return this.matricola.equals(utente.matricola) && this.CF.equals(utente.CF);
    }

}
