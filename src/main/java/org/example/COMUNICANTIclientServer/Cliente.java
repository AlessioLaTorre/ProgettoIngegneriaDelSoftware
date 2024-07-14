package org.example.COMUNICANTIclientServer;

import org.example.*;
import org.example.DTO.AppelloTransfer;
import org.example.GUI.ClientGUI;
import org.example.GestioneAppello.Appello;
import org.example.GestioneAppello.Domanda;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.example.GestioneAppello.Esito;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Cliente implements ClientIF {

    private String matricola;
    private String codiceFiscale;
    //private ArrayList<Appello> appelliSostenuti = new ArrayList<>();
    private AppelloTransfer appelloTransfer = new AppelloTransfer();


    private ClientGUI miaGUI;
    private final ServerGrpc.ServerBlockingStub blockingStub;
    private final ServerGrpc.ServerStub asyncStub;
    private final ManagedChannel canale;

    public Cliente(String host, int port)
    {   miaGUI = new ClientGUI(this);
        SwingUtilities.invokeLater(() -> miaGUI.setVisible(true));
        canale = ManagedChannelBuilder.forAddress(host,port)
                                      .usePlaintext()
                                      .build();

        blockingStub = ServerGrpc.newBlockingStub(canale);
        asyncStub = ServerGrpc.newStub(canale);
    }

    public Cliente(ManagedChannel canale)
    {
        this.canale = canale;
        blockingStub = ServerGrpc.newBlockingStub(this.canale);
        asyncStub = ServerGrpc.newStub(this.canale);
    }

    public String getMatricola() {
        return matricola;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }


    public boolean registrami(String matricola, String cf)
    {
        if(this.matricola == null && this.codiceFiscale == null)
        {
            this.matricola = matricola;
            this.codiceFiscale = cf;
        }
        RegistrazioneRequest richiesta = RegistrazioneRequest.newBuilder().setMatricola(matricola).setCF(cf).build();
        RegistrazioneResponse risposta = blockingStub.registrazione(richiesta);
        boolean ret = risposta.getRegistrato();
        if(ret) {
            notificami();

            InizioEsame inizioEsame = new InizioEsame(matricola);
            inizioEsame.start();
        }
        return ret;
    }

    class InizioEsame extends Thread {

        private String matricola;

        public InizioEsame(String matricola) {
            this.matricola = matricola;
        }

        public void run() {
            InizioEsameRequest richiesta = InizioEsameRequest.newBuilder()
                    .setMatricola(matricola)
                    .build();

            StreamObserver<InizioEsameResponse> responseObserver = new StreamObserver<InizioEsameResponse>() {

                @Override
                public void onNext(InizioEsameResponse inizioEsameResponse) {
                    String nomeEsame = inizioEsameResponse.getNomeEsame();

                    System.out.println("Sto inizioando l'esame e sono"+matricola);
                    miaGUI.inizioEsame(nomeEsame);
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onCompleted() {
                }

            };
            asyncStub.inizioEsami(richiesta, responseObserver);

        }

    }

    public void notificami()
    {   NotificaRequest richiesta = NotificaRequest.newBuilder().build();

        StreamObserver<NotificaResponse> responseObserver = new StreamObserver<NotificaResponse>() {

            @Override
            public void onNext(NotificaResponse notificaResponse) {
                String nomeEsame = notificaResponse.getNomeEsame();
                String data = notificaResponse.getData();
                String oraInizio = notificaResponse.getOraInizio();
                String oraFine = notificaResponse.getOraFine();
                String zonaFusoOrarioServer = notificaResponse.getZonaFusoOrario();
                ZoneId zonaOrarioAttale = ZoneId.systemDefault();

                // Convertire la stringa dell'orario in LocalTime
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime start = LocalTime.parse(oraInizio, timeFormatter);
                LocalTime end = LocalTime.parse(oraFine, timeFormatter);

                // Convertire la stringa del fuso orario in ZoneId
                ZoneId serverZoneId = ZoneId.of(zonaFusoOrarioServer);

                // Creare un ZonedDateTime usando la data corrente, l'orario e il fuso orario del server
                ZonedDateTime serverZonedDateTimeStart = ZonedDateTime.of(LocalDate.now(), start, serverZoneId);
                ZonedDateTime serverZonedDateTimeEnd = ZonedDateTime.of(LocalDate.now(), end, serverZoneId);

                ZonedDateTime clientZonedExamTimeStart = serverZonedDateTimeStart.withZoneSameInstant(zonaOrarioAttale);
                ZonedDateTime clientZonedExamTimeEnd = serverZonedDateTimeEnd.withZoneSameInstant(zonaOrarioAttale);

                // Estrarre LocalTime da ZonedDateTime
                LocalTime oraInizioLocale = clientZonedExamTimeStart.toLocalTime();
                LocalTime oraFineLocale = clientZonedExamTimeEnd.toLocalTime();

                LocalDate dataAppello = clientZonedExamTimeStart.toLocalDate();


                Appello.Builder builder = Appello.newBuilder();
                Appello daAggiungere = builder.withNomeEsame(nomeEsame)
                                              .withData(dataAppello)
                                              .withOraInizio(oraInizioLocale)
                                              .withOraFine(oraFineLocale)
                                              .build();

                miaGUI.loadAppello(daAggiungere);
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("Thanks for your collaboration");
            }

        };
        asyncStub.notificami(richiesta,responseObserver);

    }

    public boolean prenotazioneAppello(String appello)
    {
        PrenotazioneRequest prenotazione = PrenotazioneRequest.newBuilder()
                .setMatricola(this.matricola)
                .setCF(this.codiceFiscale)
                .setAppello(appello)
                .build();
        PrenotazioneResponse risposta = blockingStub.prenotazioneAppello(prenotazione);

        if(risposta.getPrenotato()) {
            String domande = risposta.getDomandeAppello();
            String risposte = risposta.getRisposteAppello();

            ArrayList<Domanda> domandeAppello = appelloTransfer.deserializzaPrenotazione(domande,risposte);

            miaGUI.domandeRisposteAppello(domandeAppello, appello);
        }
        return risposta.getPrenotato();
    }


    public Esito esitoEsame(ArrayList<String> risposteDate , String nomeAppello)
    {
        String risposte = appelloTransfer.serializzaRisposteDateOCorrette(risposteDate);


        FineAppelloRequest fineAppelloRequest = FineAppelloRequest.newBuilder()
                .setMatricola(matricola)
                .setCf(codiceFiscale)
                .setRisposteDate(risposte)
                .setNomeAppello(nomeAppello)
                .build();

        FineAppelloResponse fineAppelloResponse= blockingStub.fineAppello(fineAppelloRequest);

        int risultato = fineAppelloResponse.getPunteggioEffettuato();
        String[] risposteCorrette = fineAppelloResponse.getRisposteCorrette().split("-");
        boolean esito = fineAppelloResponse.getEsamePassato();

        Esito ret = new Esito(risultato,new ArrayList<>(List.of(risposteCorrette)),esito);
        return ret;
    }

    public static void main(String[] args) {
        new Cliente("localhost",10000);
    }
}

