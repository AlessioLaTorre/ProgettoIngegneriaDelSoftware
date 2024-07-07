package org.example.GUI;

import org.example.GestioneAppello.Appello;
import org.example.GestioneAppello.Domanda;
import org.example.Cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientGUI extends JFrame {

    private final Cliente cliente;

    private JTextField nomeField;
    private JTextField cognomeField;

    private HomeFrame homeFrame;


    public ClientGUI(Cliente cliente) {
        this.cliente = cliente;
        this.homeFrame = new HomeFrame(cliente);
        setTitle("Esami-OnLine");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initializeGUI();
    }

    //METODO CHE SERVE PER RAGGIUNGERE LA GUI DAL CLIENT
    public void loadAppello(Appello appello)
    {   homeFrame.loadAppello(appello);
    }

    public void domandeRisposteAppello(ArrayList<Domanda> domande, String appello)
    {   homeFrame.domandeRisposteAppello(domande,appello);
    }

    private void initializeGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel registrazionePanel = new JPanel(new GridLayout(3, 2));
        registrazionePanel.setBorder(BorderFactory.createTitledBorder("Registrazione Studente"));
        nomeField = new JTextField();
        cognomeField = new JTextField();
        JButton registrazioneButton = new JButton("Registrati");

        registrazionePanel.add(new JLabel("Matricola:"));
        registrazionePanel.add(nomeField);
        registrazionePanel.add(new JLabel("Codice Fiscale:"));
        registrazionePanel.add(cognomeField);
        registrazionePanel.add(new JLabel(""));
        registrazionePanel.add(registrazioneButton);


        registrazioneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String matricola = nomeField.getText();
                String CF = cognomeField.getText();
                if(matricola.isEmpty() || CF.isEmpty()) {
                    JOptionPane.showMessageDialog(ClientGUI.this, "Inserisci tutti i campi!");
                }
                else {
                    new RegistrazioneWorker(matricola,CF).execute();
                }
            }
        });
        mainPanel.add(registrazionePanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    //#######################################################
    //SWING WORKER REGISTRAZIONE

    private class RegistrazioneWorker extends SwingWorker<Boolean, Void> {
        private final String matricola;
        private final String CF;

        public RegistrazioneWorker(String matricola, String CF) {
            this.matricola = matricola;
            this.CF = CF;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            // Questa operazione verrà eseguita in background
            return cliente.registrami(matricola, CF);
        }

        @Override
        protected void done() {
            try {
                boolean success = get();
                if (!success) {
                    JOptionPane.showMessageDialog(ClientGUI.this, "Studente già registrato");
                } else {
                    JOptionPane.showMessageDialog(ClientGUI.this, "Studente registrato: " + matricola + " " + CF);
                    openHomePage();
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(ClientGUI.this, "Errore durante la registrazione");
            }
        }
    }

    //FINE SWING WORKER REGISTRAZIONE
    //######################################################################





    private void openHomePage()
    {   //CHIUDO LA FINESTRA ATTUALE
        dispose();
        //APRO LA NUOVA
        homeFrame.setVisible(true);
    }

    static class HomeFrame extends JFrame {

        private Cliente cliente;


        private JList<String> appelliList;
        private DefaultListModel<String> appelliListModel;      //LISTA CHE SI VEDE ALL'INTERNO DELLA GUI CHE MOSTRA GLI APPELLI DISPONIBILI
        private CopyOnWriteArrayList<Appello> appelli = new CopyOnWriteArrayList<>();
        private static Map<String, Boolean> prenotazioni = new HashMap<>();     //MAPPA CHE MI TIENE CONTO DEGLI APPELLI CHE SONO STATI PRENOTATI

        public HomeFrame(Cliente cliente) {
            this.cliente = cliente;
            setTitle("Sistema di Gestione Appelli");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            initializeGUI();

            ControllaInizioEsami controllaInizioEsami = new ControllaInizioEsami();
            controllaInizioEsami.start();
        }

        class ControllaInizioEsami extends Thread {

            @Override
            public void run()
            {   try {
                while (true) {
                    sleep(3000);
                    if (!appelli.isEmpty()) {
                        for (Appello appello : appelli) {
                            if (appello.isPrenotato() && !appello.isGiaIniziato() && (LocalTime.now().isAfter(appello.getOraInizio())
                                    || LocalTime.now().equals(appello.getOraInizio())) && LocalDate.now().isEqual(appello.getData())) {
                                appello.setGiaIniziato();
                                SwingUtilities.invokeLater(() -> new EsameGUI(HomeFrame.this, appello).setVisible(true));
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            }
        }


        private void initializeGUI() {
            JPanel visualizzazionePanel = new JPanel(new BorderLayout());
            visualizzazionePanel.setBorder(BorderFactory.createTitledBorder("Prossimi Appelli"));
            appelliListModel = new DefaultListModel<>();
            appelliList = new JList<>(appelliListModel);
            visualizzazionePanel.add(new JScrollPane(appelliList), BorderLayout.CENTER);

            add(visualizzazionePanel);

            appelliList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int index = appelliList.locationToIndex(e.getPoint());
                        if (index >= 0) {
                            String[] appelloSelezionatoformat = appelliListModel.get(index).split(" ");
                            String appelloSelezionato = appelloSelezionatoformat[1];
                            if (prenotazioni.getOrDefault(appelloSelezionato, false)) {
                                JOptionPane.showMessageDialog(HomeFrame.this, "Sei già prenotato per questo appello.");
                            }
                            else if(appelloScaduto(appelloSelezionato))
                                JOptionPane.showMessageDialog(HomeFrame.this,"l'appello non è prenotabile causa orario di scadenza.");
                            else {
                                showPrenotazioneDialog(appelloSelezionato);
                            }
                        }
                    }
                }
            });

        }

        public boolean appelloScaduto(String appello)
        {   boolean eScaduto = false;
            for(Appello a: appelli)
            {   if(a.getNomeEsame().equals(appello))
                    if(LocalDate.now().isAfter(a.getData()))
                        eScaduto = true;
                    else if (LocalDate.now().isEqual(a.getData()) && LocalTime.now().isAfter(a.getOraInizio()))
                    {   eScaduto = true;
                    }
            }
            return eScaduto;
        }

        private void domandeRisposteAppello(ArrayList<Domanda> domande,String appello)
        {   for(Appello app: appelli)
            {   if(app.getNomeEsame().equals(appello))
                {   app.setDomande(domande);
                    app.setPrenotato();
                }
            }

        }

        private void loadAppello(Appello a) {
            new LoadAppelloWorker(a).execute();
        }

        //########################################################
        //SWING WORKER PER CARICARE GLI APPELLI
        private class LoadAppelloWorker extends SwingWorker<Void, Void> {
            private final Appello appello;

            public LoadAppelloWorker(Appello appello) {
                this.appello = appello;
            }

            @Override
            protected Void doInBackground() {
                //Non devo fare nulla
                return null;
            }

            @Override
            protected void done() {
                // Questo codice viene eseguito sull'Event Dispatch Thread (EDT)
                try {
                    get(); // Verifichiamo se ci sono eccezioni
                    String formatAppelloString = "Appello " + appello.getNomeEsame() + " : Data " + appello.getData() + " ora Inizio: " + appello.getOraInizio() + " ora Fine: " + appello.getOraFine();
                    appelli.add(appello);
                    appelliListModel.addElement(formatAppelloString);
                    prenotazioni.put(formatAppelloString, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(HomeFrame.this, "Errore durante il caricamento dell'appello");
                }
            }
        }

        //FINE SWING WORKER APPELLI
        //#########################################################################

        private void showPrenotazioneDialog(String appello) {
            int result = JOptionPane.showConfirmDialog(HomeFrame.this, "Sei sicuro di voler procedere con la prenotazione all'appello?", "Conferma", JOptionPane.YES_NO_OPTION);

            if(result == JOptionPane.YES_OPTION) {
                boolean possoPrenotarmi = cliente.prenotazioneAppello(appello);
                if (possoPrenotarmi) {
                    JOptionPane.showMessageDialog(HomeFrame.this, "Prenotazione per studente con matricola: " + cliente.getMatricola() + " confermata.");
                    prenotazioni.put(appello, true);
                } else if (!possoPrenotarmi) {
                    JOptionPane.showMessageDialog(HomeFrame.this, "Hai già un'appello previsto nello stesso orario");
                } else {
                    // Codice da eseguire quando l'utente annulla l'operazione
                    JOptionPane.showMessageDialog(HomeFrame.this, "Operazione annullata.");
                }
            }
            }
        }
    }
