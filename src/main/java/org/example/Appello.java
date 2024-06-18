package org.example;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

public class Appello
{
    private String nomeEsame;
    private LocalDate data;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private final int durata = 50;
    private boolean giaIniziato = false;
    private boolean prenotato = false;

                //  DOMANDE           RISPOSTE
    private HashMap<String, ArrayList<String>> mappaDomande = new HashMap<>();

    /**
     *
     * @param nomeEsame
     * @param data Inesrire questo parametro in formato XX:XX
     * @param oraInizio Inserire parametro oraInizio in formato AAAA-MM-GG
     * @param oraFine Inserire parametro oraFine in formato AAAA-MM-GG
     */
    public Appello(String nomeEsame, String data, String oraInizio, String oraFine)
    {
        this.nomeEsame = nomeEsame;
        this.data = LocalDate.parse(data);
        this.oraInizio = LocalTime.parse(oraInizio);
        this.oraFine = LocalTime.parse(oraFine);
    }

    public Appello(){};


    public void setPrenotato() {
        this.prenotato = true;
    }

    public boolean isPrenotato() {
        return prenotato;
    }

    public void setGiaIniziato() {
        this.giaIniziato = true;
    }

    public boolean isGiaIniziato() {
        return giaIniziato;
    }

    /**
     * Devono avere lo stesso numero di elementi
     * ad ogni domanda è associato un blocco di risposte
     * separate da "-"
     * @param domande
     * @param risposte
     */
    public void setMappaDomande(ArrayList<String> domande, ArrayList<String> risposte)
    {   for(int i = 0; i < domande.size(); i++)
        {
            String[] formatRisp = risposte.get(i).split("-");
            ArrayList<String> risposteDomanda = new ArrayList<>();
            for (int j = 0; j < formatRisp.length; j++)
            {   risposteDomanda.add(formatRisp[j]);
            }
            mappaDomande.put(domande.get(i), risposteDomanda);
        }
    }

    public HashMap<String, ArrayList<String>> getMappaDomande() {
        return mappaDomande;
    }

    public String getDomande()
    {   StringBuilder sb = new StringBuilder();
        int size = mappaDomande.size();
        int count = 0;

        for (String domanda : mappaDomande.keySet()) {
            sb.append(domanda);
            count++;
            if (count < size) {
                sb.append("-");
            }
        }

        return sb.toString();
    }

    public String getRisposte()
    {   StringBuilder sb = new StringBuilder();
        int size = mappaDomande.size();
        int count = 0;
        for (String domanda : mappaDomande.keySet()) {
            int countRisp = 0;
            int lunghezza = mappaDomande.get(domanda).size();
            for(String risposte : mappaDomande.get(domanda))
            {   sb.append(risposte);
                countRisp++;
                if (countRisp < lunghezza)
                {   sb.append("-");
                }
            }
            count++;
            if (count < size) {
                sb.append("#");
            }
        }
        return sb.toString();
    }

    public int getDurata()
    {   return durata;
    }

    public String getNomeEsame()
    {   return nomeEsame;
    }

    public LocalTime getOraFine()
    {   return oraFine;
    }

    public LocalTime getOraInizio()
    {   return oraInizio;
    }

    public LocalDate getData()
    {   return data;
    }

}
