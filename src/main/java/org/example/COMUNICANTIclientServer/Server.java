package org.example.COMUNICANTIclientServer;

import org.example.*;
import org.example.DTO.AppelloTransfer;
import org.example.GUI.ServerGUI;
import org.example.GestioneAppello.Appello;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.GestioneAppello.Domanda;
import org.example.GestioneAppello.Utente;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private static ServerGUI miaGUI;

    private static final AppelloTransfer appelloTransfer = new AppelloTransfer();

    private static ArrayList<Utente> registrati = new ArrayList<>();
    private static CopyOnWriteArrayList<Appello> appelli = new CopyOnWriteArrayList<>();

    public static void inserisciPrenotato(String nomeEsame,String matricola)
    {
        miaGUI.aggiornaMappa(nomeEsame,matricola);
    }

    public static boolean contieneAppello(String nomeAppello)
    {   if(appelli.isEmpty())
            return false;
        for(Appello appello : appelli)
        {   if(appello.getNomeEsame().equals(nomeAppello))
                return true;
        }
        return false;
    }

    public static void aggiungiAppello(Appello appello) {
        appelli.add(appello);
    }

    public Server()
    {   miaGUI = new ServerGUI(this);
        miaGUI.setVisible(true);
    }





    public static class RichiesteHandler extends ServerGrpc.ServerImplBase
    {
        @Override
        public void inizioEsami(InizioEsameRequest request, StreamObserver<InizioEsameResponse> responseObserver) {
            GestoreInizioEsami gestoreInizioEsami = new GestoreInizioEsami(responseObserver,request.getMatricola());
            gestoreInizioEsami.start();
            System.out.println("mi sono rotto i coglioni porcaccia la miseria ladra");
        }

        class GestoreInizioEsami extends Thread
        {
            private String matricola;
            private StreamObserver<InizioEsameResponse> responseObserver;
            public GestoreInizioEsami(StreamObserver<InizioEsameResponse> responseObserver,String matricola)
            {
                this.responseObserver = responseObserver;
                this.matricola = matricola;
            }

            @Override
            public void run() {

                while (true) {
                    try {
                        sleep(1000);
                        for (Appello appello : appelli) {
                            System.out.println(appello.isGiaIniziato());
                            if (!appello.isGiaIniziato()) {
                                if ((LocalTime.now().equals(appello.getOraInizio()) || LocalTime.now().isAfter(appello.getOraInizio()))
                                        && LocalDate.now().isEqual(appello.getData())) {
                                    for (Utente utente : registrati) {
                                        if (utente.getMatricola().equals(matricola) && utente.isPrenotato(appello) && !utente.hasSostenuto(appello)) {
                                            utente.setAppelloSostenuto(appello);
                                            InizioEsameResponse response = InizioEsameResponse.newBuilder()
                                                    .setNomeEsame(appello.getNomeEsame())
                                                    .build();
                                            responseObserver.onNext(response);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }}


        @Override
        public void prenotazioneAppello(PrenotazioneRequest request, StreamObserver<PrenotazioneResponse> responseObserver)
        {
            boolean prenotato = false;

            String matricola = request.getMatricola();
            String cf = request.getCF();
            String appello = request.getAppello();

            Appello app = null;
            Utente utente = new Utente();

            String domande = "";
            String risposte = "";
            for(Appello a:appelli)
            {   if(a.getNomeEsame().equals(appello))
                {   domande = appelloTransfer.serializzaDomande(a);
                    risposte = appelloTransfer.serializzaRisposte(a);
                    app = a;
                }
            }

            for(Utente a:registrati)
            {   if(a.getMatricola().equals(matricola) && a.getCF().equals(cf))
                {   utente = a;
                }
            }

            if(utente.isOrarioAccettabile(app))
            {   prenotato = true;
                utente.setAppelloPrenotato(app);
                inserisciPrenotato(appello,matricola);
            }


            PrenotazioneResponse response = PrenotazioneResponse.newBuilder()
                    .setDomandeAppello(domande)
                    .setRisposteAppello(risposte)
                    .setPrenotato(prenotato)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        }

        @Override
        public void fineAppello(FineAppelloRequest request, StreamObserver<FineAppelloResponse> responseObserver) {

            String matricola = request.getMatricola();
            String cf = request.getCf();
            String nomeAppello = request.getNomeAppello();

            Utente utente = new Utente();

            ArrayList<String> risposteDate = appelloTransfer.ottieniRisposteDate(request.getRisposteDate());

            for(Utente a:registrati)
            {   if(a.getMatricola().equals(matricola) && a.getCF().equals(cf))
                    utente = a;
            }

            int punteggio = 0;
            ArrayList<String> risposteCorrette = new ArrayList<>();

            for(Appello a:appelli)
            {   if(a.getNomeEsame().equals(nomeAppello))
                {   ArrayList<Domanda> domandeAppello = a.getDomande();
                    for(int i = 0; i < domandeAppello.size(); i++)
                    {   String rispostaCorretta = domandeAppello.get(i).getRispostaCorretta();
                        if(risposteDate.get(i).equals("Nessuna Risposta"))
                        {   punteggio -= 1;
                        } else if (risposteDate.get(i).equals(rispostaCorretta))
                        {   punteggio += 3;
                        }
                        risposteCorrette.add(rispostaCorretta);
                    }
                }
            }

            String rispCorrette = appelloTransfer.serializzaRisposteDateOCorrette(risposteCorrette);

            boolean esamePassato = false;
            if(punteggio >= 3)
                esamePassato = true;

            FineAppelloResponse fineAppelloResponse = FineAppelloResponse.newBuilder()
                    .setPunteggioEffettuato(punteggio)
                    .setRisposteCorrette(rispCorrette)
                    .setEsamePassato(esamePassato)
                    .build();

            responseObserver.onNext(fineAppelloResponse);
            responseObserver.onCompleted();
        }

        @Override
        public void registrazione(RegistrazioneRequest request, StreamObserver<RegistrazioneResponse> responseObserver)
        {
            boolean effettuata = true;
            boolean giaRegistrato = false;
            String matricola = request.getMatricola();
            String CF = request.getCF();
            Utente nuovo = new Utente(matricola,CF);
            for(Utente u : registrati)
            {   if (u.equals(nuovo))
                    giaRegistrato = true;
            }
            if(!giaRegistrato || registrati.isEmpty())
                registrati.add(nuovo);
            RegistrazioneResponse risposta = RegistrazioneResponse.newBuilder()
                    .setRegistrato(!giaRegistrato)
                    .build();

            responseObserver.onNext(risposta);
            responseObserver.onCompleted();
        }

        @Override
        public void notificami(NotificaRequest request, StreamObserver<NotificaResponse> responseObserver)
        {   GestoreNotifiche gestore = new GestoreNotifiche(responseObserver);
            gestore.start();
        }


        class GestoreNotifiche extends Thread
        {
            private ArrayList<Appello> appelliGiaNotificati;
            private StreamObserver<NotificaResponse> responseObserver;

            public GestoreNotifiche(StreamObserver<NotificaResponse> responseObserver)
            {   this.appelliGiaNotificati = new ArrayList<>();
                this.responseObserver = responseObserver;
            }

            @Override
            public void run()
            {
                while(true)
                    {   try{
                            sleep(100);
                        }
                        catch (Exception e)
                            {   responseObserver.onError(e);}
                        for (Appello appello : appelli)
                        {   if (!appelliGiaNotificati.contains(appello))
                            {   appelliGiaNotificati.add(appello);
                                NotificaResponse notifica = NotificaResponse.newBuilder()
                                        .setNomeEsame(appello.getNomeEsame())
                                        .setData(appello.getData().toString())
                                        .setOraInizio(appello.getOraInizio().toString())
                                        .setOraFine(appello.getOraFine().toString())
                                        .setZonaFusoOrario(ZoneId.systemDefault().toString())
                                        .build();
                                responseObserver.onNext(notifica);
                            }
                        }
                    }
            }
        }
    }


    public static void main(String[] args) throws IOException,InterruptedException {

        Server s = new Server();

        int port = 10000;
        io.grpc.Server server = ServerBuilder.forPort(port)
                .addService(new RichiesteHandler())
                .build()
                .start();

        System.out.println("Server started");

        server.awaitTermination();
    }
}
