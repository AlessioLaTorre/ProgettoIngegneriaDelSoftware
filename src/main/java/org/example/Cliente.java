package org.example;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.example.RegistrazioneRequest;
import org.example.RegistrazioneResponse;

import javax.swing.*;

public class Cliente implements ClientIF{

    private String matricola;
    private String codiceFiscale;

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
        if(ret)
            notificami();
        return ret;
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
                Appello daAggiungere = new Appello(nomeEsame,data,oraInizio,oraFine);
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
        String domande = risposta.getDomandeAppello();
        String risposte = risposta.getRisposteAppello();
        miaGUI.domandeRisposteAppello(domande,risposte,appello);
        return risposta.getPrenotato();
    }



    public static void main(String[] args) {
        new Cliente("localhost",10000);
    }
}
