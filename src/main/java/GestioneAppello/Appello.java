package GestioneAppello;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Appello
{
    private String nomeEsame;
    private LocalDate data;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private static final int durata = 50;
    private boolean giaIniziato = false;
    private boolean prenotato = false;

    private List<Domanda> domande;

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

    public ArrayList<Domanda> getDomande()
    {   ArrayList<Domanda> ret = new ArrayList<>();
        for(Domanda d : domande)
            ret.add(d);
        return ret;
    }

    public void setDomande(ArrayList<Domanda> domande) {
        this.domande = domande;
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



    private Appello(Builder builder)
    {   this.nomeEsame = builder.nomeEsame;
        this.data = builder.data;
        this.oraInizio = builder.oraInizio;
        this.oraFine = builder.oraFine;
        this.domande = builder.domande;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder
    {
        private String nomeEsame;
        private LocalDate data;
        private LocalTime oraInizio;
        private LocalTime oraFine;
        private List<Domanda> domande;

        private Builder() {}

        public Builder withNomeEsame(String nomeEsame) {
            this.nomeEsame = nomeEsame;
            return this;
        }

        /**
         *
         * @param data Inesrire questo parametro in formato XX:XX
         * @return
         */
        public Builder withData(String data) {
            this.data = LocalDate.parse(data);
            return this;
        }


        /**
         *
         * @param oraInizio Inserire parametro oraInizio in formato AAAA-MM-GG
         * @return
         */
        public Builder withOraInizio(String oraInizio) {
            this.oraInizio = LocalTime.parse(oraInizio);
            return this;
        }


        /**
         *
         * @param oraFine Inserire parametro oraFine in formato AAAA-MM-GG
         * @return
         */
        public Builder withOraFine(String oraFine) {
            this.oraFine = LocalTime.parse(oraFine);
            return this;
        }

        public Builder withDomande(List<Domanda> domande) {
            this.domande = domande;
            return this;
        }

        public Appello build() {
            return new Appello(this);
        }
    }

}
