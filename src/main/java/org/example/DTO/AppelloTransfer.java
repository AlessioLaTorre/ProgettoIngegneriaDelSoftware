package org.example.DTO;

import org.example.GestioneAppello.Appello;
import org.example.GestioneAppello.Domanda;

import java.util.ArrayList;
import java.util.Arrays;

public class AppelloTransfer
{
    public AppelloTransfer(){};

    public String serializzaDomande(Appello appello)
    {   StringBuilder sb = new StringBuilder();
        int count = 0;
        ArrayList<Domanda> domande = appello.getDomande();

        for(Domanda d : domande)
        {
            String domanda = d.getTesto();
            sb.append(domanda);
            count++;
            if(count < domande.size())
                sb.append("-");
        }
        return sb.toString();
    }

    public String serializzaRisposte(Appello appello)
    {
        StringBuilder sb = new StringBuilder();
        int count = 0;

        ArrayList<Domanda> domande = appello.getDomande();

        for(Domanda d : domande)
        {
            int countRisposta = 1;
            int numDomande = d.getNumRisposte();

            sb.append(d.getRispostaCorretta()).append("-");
            for(String rispostaSbagliata : d.getRisposteSbagliate())
            {
                sb.append(rispostaSbagliata);
                countRisposta++;
                if(countRisposta< numDomande)
                    sb.append("-");
            }
            count++;
            if(count < domande.size())
                sb.append("#");
        }
        return sb.toString();
    }



    public ArrayList<Domanda> deserializzaPrenotazione(String domande, String risposte)
    {   ArrayList<Domanda> domandeAppello = new ArrayList<>();

        String[] testiDomande = domande.split("-");
        ArrayList<String> testi = new ArrayList<>(Arrays.asList(testiDomande));
        String[] blocchiRisposte = risposte.split("#");
        ArrayList<String> risposteAlleDomande = new ArrayList<>(Arrays.asList(blocchiRisposte));

        for(int i = 0; i < testi.size(); i++)
        {
            String[] tutteRisposte = blocchiRisposte[i].split("-");

            String testoDomanda = testi.get(i);
            String rispostaCorretta = tutteRisposte[0];

            System.out.println(testoDomanda);
            System.out.println(rispostaCorretta);

            ArrayList<String> risposteSbagliate = new ArrayList<>();
            for(int j = 1; j < tutteRisposte.length; j++)
            {   risposteSbagliate.add(tutteRisposte[j]);
                System.out.println(tutteRisposte[i]);
            }
            Domanda d = new Domanda(testoDomanda,rispostaCorretta,risposteSbagliate);
            domandeAppello.add(d);
        }

        return domandeAppello;
    }


    public String serializzaRisposteDateOCorrette(ArrayList<String> risposte)
    {
        int count = 0;
        StringBuilder sb = new StringBuilder();
        for(String risposta : risposte)
        {   sb.append(risposta);
            count++;
            if(count < risposte.size())
                sb.append("-");
        }
        return sb.toString();
    }


    public ArrayList<String> ottieniRisposteDate(String risposteDate)
    {   String[] formattate = risposteDate.split("-");
        ArrayList<String> ret = new ArrayList<>(Arrays.asList(formattate));
        return ret;
    }
}
