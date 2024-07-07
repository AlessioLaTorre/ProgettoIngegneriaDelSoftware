package org.example;

import java.util.ArrayList;

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


/*
    public Appello deserializzaAppello(String appelloSerializzato)
    {   return ;
    }

 */
}
