package org.example;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private static ServerGUI miaGUI;

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




    public static class RichiesteHandler extends org.example.ServerGrpc.ServerImplBase
    {
        @Override
        public void prenotazioneAppello(PrenotazioneRequest request, StreamObserver<PrenotazioneResponse> responseObserver)
        {
            boolean prenotato = false;

            String matricola = request.getMatricola();
            String cf = request.getCF();
            String appello = request.getAppello();

            Appello app = new Appello();
            Utente utente = new Utente();

            String domande = "";
            String risposte = "";
            for(Appello a:appelli)
            {   if(a.getNomeEsame().equals(appello))
                {   domande = a.getDomande();
                    risposte = a.getRisposte();
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
                utente.setAppello(app);
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
