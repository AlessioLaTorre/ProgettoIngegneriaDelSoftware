package testing;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.stub.StreamObserver;
import org.example.Appello;
import org.example.Cliente;
import org.example.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class TestClass {

    ManagedChannel channel;

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private Cliente client;
    private io.grpc.Server server;
    private static final String SERVER_NAME = InProcessServerBuilder.generateName();

    @Before
    public void setUp() throws Exception {

        // avvia il server in-process
        server = InProcessServerBuilder.forName(SERVER_NAME)
                .addService(new Server.RichiesteHandler())
                .directExecutor()
                .build()
                .start();

        // Setup del client in-process
        channel = InProcessChannelBuilder.forName(SERVER_NAME).directExecutor().build();
        client = new Cliente(channel);
    }

    @After
    public void tearDown() throws Exception {
        if(channel != null) {
            channel.shutdown();
        }
        if(server != null) {
           server.shutdown();
        }
    }

    @Test
    public void testRegistrazione() {
        String matricola = "123456";
        String cf = "ABCDEF123456";

        //VERIFICA REGISTRAZIONE
        boolean result = client.registrami(matricola, cf);
        assertTrue(result);

        //SECONDO ACCESSO CON STESSI DATI NON POSSIBILE
        boolean fallimento = client.registrami(matricola, cf);
        assertFalse(fallimento);
    }

    @Test
    public void testPrenotazioneAppello() {
        String matricola = "123456";
        String cf = "ABCDEF123456";
        String nomeAppello = "Esame di Test";
        String data = "2024-06-14";
        String oraInizio = "09:00";
        String oraFine = "09:50";

        // Registra l'utente
        boolean registrazione = client.registrami(matricola, cf);
        assertTrue(registrazione);

        // Aggiungi un appello al server
        Appello appello = new Appello(nomeAppello, data, oraInizio, oraFine);
        Server.aggiungiAppello(appello);

        // Verifica che l'appello sia stato aggiunto
        assertTrue(Server.contieneAppello(nomeAppello));

        // Effettua la prenotazione dell'appello
        boolean prenotazione = client.prenotazioneAppello(nomeAppello);
        assertTrue(prenotazione);

        //verifica che un client non possa effettuare due prenotazioni in orari coincidenti
        Appello app = new Appello("esame", data, "09:01","09:51");
        Server.aggiungiAppello(app);
        boolean secondaPrenotazione = client.prenotazioneAppello(app.getNomeEsame());
        assertFalse(secondaPrenotazione);

    }
}
