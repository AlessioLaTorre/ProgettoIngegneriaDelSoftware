package testing;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.example.GestioneAppello.Appello;
import org.example.COMUNICANTIclientServer.Cliente;
import org.example.COMUNICANTIclientServer.Server;
import org.example.GestioneAppello.Domanda;
import org.junit.*;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@Ignore
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
        String matricola = "1234567";
        String cf = "ABCDEF1234567";
        String nomeAppello = "Esame di Test";
        String data = "2024-06-14";
        String oraInizio = "09:00";
        String oraFine = "09:50";

        // Registra l'utente
        boolean registrazione = client.registrami(matricola, cf);
        assertTrue(registrazione);

        // Aggiungi un appello al server
        Appello appello = Appello.newBuilder().withNomeEsame(nomeAppello)
                .withData(data)
                .withOraInizio(oraInizio)
                .withOraFine(oraFine)
                .build();

        Server.aggiungiAppello(appello);

        // Verifica che l'appello sia stato aggiunto
        assertTrue(Server.contieneAppello(nomeAppello));

        ArrayList<Domanda> domande = new ArrayList<>();
        ArrayList<String> risposteSbagliate = new ArrayList<>();
        risposteSbagliate.add("1");
        risposteSbagliate.add("2");
        risposteSbagliate.add("3");
        for(int i = 0; i < 2; i++)
        {     Domanda d = new Domanda("domanda"+i,"corretta",risposteSbagliate);
              domande.add(d);
        }
        appello.setDomande(domande);

        // Effettua la prenotazione dell'appello
        boolean prenotazione = client.prenotazioneAppello(nomeAppello);
        assertTrue(prenotazione);

        //verifica che un client non possa effettuare due prenotazioni in orari coincidenti
        Appello app = Appello.newBuilder().withNomeEsame("esame")
                .withData(data)
                .withOraInizio(oraInizio)
                .withOraFine(oraFine)
                .build();


        Server.aggiungiAppello(app);
        app.setDomande(domande);

        boolean secondaPrenotazione = client.prenotazioneAppello(app.getNomeEsame());
        assertFalse(secondaPrenotazione);

    }
}
