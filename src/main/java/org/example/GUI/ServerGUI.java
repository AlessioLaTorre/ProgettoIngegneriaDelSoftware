package org.example.GUI;

import org.example.GestioneAppello.Appello;
import org.example.GestioneAppello.Domanda;
import org.example.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerGUI extends JFrame {

    private static final int[] mesiGiorni = {31,28,31,30,31,30,31,31,30,31,30,31};

    private Server server;
    private List<Domanda> doma = new ArrayList<>();
    private boolean aggiunta = false;


    private JList<String> appelliList;
    private DefaultListModel<String> appelliListModel;
    private static HashMap<String,ArrayList<String>> mappaAppelliConPrenotati = new HashMap<>();

    public static void aggiornaMappa(String nomeEsame,String matricola)
    {   mappaAppelliConPrenotati.get(nomeEsame).add(matricola);
    }

    public ServerGUI(Server server) {
        this.server = server;
        setTitle("Esami-On-Line");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        appelliListModel = new DefaultListModel<>();
        appelliList = new JList<>(appelliListModel);

        appelliList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = appelliList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String appelloSelezionato = appelliListModel.get(index);
                        ArrayList<String> prenotatiAppello = mappaAppelliConPrenotati.get(appelloSelezionato);
                        if(!prenotatiAppello.contains(appelloSelezionato)) {
                            StringBuilder sb = new StringBuilder();
                            for (String item : prenotatiAppello) {
                                sb.append("Studente matricola:").append(item).append("\n");
                            }
                            JOptionPane.showMessageDialog(null, sb.toString(),"Prenotati all'appello",JOptionPane.PLAIN_MESSAGE);
                        }

                    }
                }
            }
        });

        JButton AggiungiAppello = new JButton("Aggiungi Appello");
        AggiungiAppello.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAddAppelloDialog();
            }
        });

        add(AggiungiAppello);
        add(appelliList);
    }

    private void openAddAppelloDialog() {
        JDialog dialog = new JDialog(this, "Aggiungi Appello", true);
        dialog.setLayout(new GridLayout(6, 2,5,5));

        JLabel lblNomeEsame = new JLabel("Nome esame:");
        JTextField txtNomeEsame = new JTextField();

        JLabel lblNumeroCrediti = new JLabel("Numero crediti:");
        JSpinner spnNumeroCrediti = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

        JLabel lblData = new JLabel("Data: aaaa-mm-gg");
        JTextField txtData = new JTextField();

        JLabel lblOraInizio = new JLabel("Ora inizio: xx:xx");
        JTextField txtOraInizio = new JTextField();


        JButton domande = new JButton("Aggiungi domande");
        domande.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AggiungiDomandeDialog dialog = new AggiungiDomandeDialog(ServerGUI.this);
                dialog.setVisible(true);
                doma = dialog.getDomande();
                aggiunta = dialog.isAggiunta();
                if (aggiunta) {
                    domande.setEnabled(false);
                }
            }
        });


        JButton submit = new JButton("Submit");
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(!aggiunta || txtNomeEsame.getText().isEmpty() || txtData.getText().isEmpty() || txtOraInizio.getText().isEmpty())
                {
                    JOptionPane.showMessageDialog(ServerGUI.this, "Devi compilare tutti i campi");
                } else if (server.contieneAppello(txtNomeEsame.getText())) {
                    JOptionPane.showMessageDialog(ServerGUI.this, "Esame già esistente prova con un'altro nome");
                }
                else if(!dataFormattata(txtData.getText()) || !oraFormattata(txtOraInizio.getText()))
                {
                    JOptionPane.showMessageDialog(ServerGUI.this,"Non hai scritto correttamente data o ora");
                }
                else
                {   JOptionPane.showMessageDialog(ServerGUI.this, "L'appello è stato inserito correttamente");

                    mappaAppelliConPrenotati.put(txtNomeEsame.getText(), new ArrayList<>());
                    appelliListModel.addElement(txtNomeEsame.getText());

                    aggiunta = false;
                    String nomeEsame = txtNomeEsame.getText();
                    int numeroCrediti = (Integer) spnNumeroCrediti.getValue();
                    String data = txtData.getText();
                    String oraInizio = txtOraInizio.getText();
                    String oraFine = calcolaFineOra(oraInizio);
                    Appello.Builder builder = Appello.newBuilder();
                    Appello appello = builder.withNomeEsame(nomeEsame)
                                             .withData(data)
                                             .withOraInizio(oraInizio)
                                             .withOraFine(oraFine)
                                             .withDomande(new ArrayList<>(doma))
                                             .build();
                    doma.clear();
                    server.aggiungiAppello(appello);
                    dialog.dispose();
                }
            }
        });

        dialog.add(lblNomeEsame);
        dialog.add(txtNomeEsame);
        dialog.add(lblNumeroCrediti);
        dialog.add(spnNumeroCrediti);
        dialog.add(lblData);
        dialog.add(txtData);
        dialog.add(lblOraInizio);
        dialog.add(txtOraInizio);
        dialog.add(domande);
        dialog.add(new JLabel());
        dialog.add(submit);

        dialog.pack();
        dialog.setVisible(true);
    }

    public boolean dataFormattata(String data)
    {   boolean ret = true;
        String[] dataFormat = data.split("-");
        if(dataFormat[0].length() != 4)
            return false;
        for(int i = 1; i < dataFormat.length;i++)
        {   if(dataFormat[i].length() != 2)
                return false;
        }

        int mese = Integer.parseInt(dataFormat[1]);
        int giorno = Integer.parseInt(dataFormat[2]);
        if(mese <= 0 || mese > 12)
        {   return false;
        }

        if(giorno <= 0 || giorno > mesiGiorni[mese - 1])
        {   return false;
        }

        return ret;
    }

    public boolean oraFormattata(String ora)
    {   boolean ret = true;

        String[] oraFormat = ora.split(":");

        int ore = Integer.parseInt(oraFormat[0]);
        int minuti = Integer.parseInt(oraFormat[1]);
        if(ore < 0 || ore > 24 || minuti < 0 || minuti > 60)
            ret =  false;
        return ret;
    }

    public String calcolaFineOra(String oraInizio)
    {   String oraFine = "";
        String[] format = oraInizio.split(":");
        int ora = Integer.parseInt(format[0]);
        int minuto = Integer.parseInt(format[1]);
        if(minuto < 10)
        {   minuto +=50;
            if(ora < 10) {
                oraFine = "0" + ora + ":" + minuto;
            }
            else
                oraFine =ora + ":" + minuto;
        }
        else if(minuto >= 10 && minuto < 20)
        {   ora = (ora+1)%24;
            minuto = (minuto+50)%60;
            if(ora < 10) {
                oraFine = "0" + ora + ":0" + minuto;
            }
            else
                oraFine =ora + ":0" + minuto;
        }
        else
        {   ora = (ora+1)%24;
            minuto = (minuto+50)%60;
            if(ora < 10) {
                oraFine = "0" + ora + ":" + minuto;
            }
            else
                oraFine =ora + ":" + minuto;
        }
        return oraFine;
    }


    class AggiungiDomandeDialog extends JDialog
    {
        private static final int numDomande = 2;
        private List<Domanda> domande = new ArrayList<>();
        private JPanel domandePanel;
        private boolean aggiunte = false;



        //QUESTO METODO MI PERMETTE DI AGGIUGERE LA FINESTRA CON ALL'INTERNO TUTTE LE DOMANDRE DA INSERIRE
        //METODO COSTRUTTORE
        public AggiungiDomandeDialog(JFrame parent)
        {
            super(parent, "Aggiungi Domande", true);
            setLayout(new BorderLayout());

            domandePanel = new JPanel();
            domandePanel.setLayout(new BoxLayout(domandePanel, BoxLayout.Y_AXIS));
            JScrollPane scrollPane = new JScrollPane(domandePanel);
            add(scrollPane, BorderLayout.CENTER);


            for (int i = 1; i <= numDomande; i++)
            {
                aggiungiDomandaPanel(i);
            }

            JButton btnSubmit = new JButton("Submit");
            btnSubmit.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (raccoltaDomande())
                    {   aggiunte = true;
                        setVisible(false);
                    } else
                    {   JOptionPane.showMessageDialog(AggiungiDomandeDialog.this, "Devi inserire esattamente 10 domande e relative risposte.");
                    }
                }
            });

            add(btnSubmit, BorderLayout.SOUTH);

            setSize(400, 500);
            setLocationRelativeTo(parent);
        }

        //QUESTO METODO MI PERMETTE DI AGGIUNGERE OGNI PANEL DI OGNI SINGOLA DOMANDA
        //REF:metodo AggiungiDomandeDialog
        private void aggiungiDomandaPanel(int numero) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder("Domanda " + numero));

            JTextField txtDomanda = new JTextField();
            panel.add(new JLabel("Testo domanda:"), BorderLayout.NORTH);
            panel.add(txtDomanda, BorderLayout.CENTER);

            // Pannello per le risposte
            JPanel rispostaPanel = new JPanel(new GridLayout(4, 2));
            JTextField txtRispostaCorretta = new JTextField(20);
            JTextField txtRispostaSbagliata1 = new JTextField(20);
            JTextField txtRispostaSbagliata2 = new JTextField(20);
            JTextField txtRispostaSbagliata3 = new JTextField(20);

            // Aggiungi le etichette e i campi di testo al pannello delle risposte
            rispostaPanel.add(new JLabel("Risposta corretta:"));
            rispostaPanel.add(txtRispostaCorretta);
            rispostaPanel.add(new JLabel("Risposta sbagliata 1:"));
            rispostaPanel.add(txtRispostaSbagliata1);
            rispostaPanel.add(new JLabel("Risposta sbagliata 2:"));
            rispostaPanel.add(txtRispostaSbagliata2);
            rispostaPanel.add(new JLabel("Risposta sbagliata 3:"));
            rispostaPanel.add(txtRispostaSbagliata3);


            panel.add(rispostaPanel, BorderLayout.SOUTH);
            domandePanel.add(panel);
        }




        //CON QUESTO METODO CONTROLLO CHE LE DOMANDE SIANO STATE INSERITE TUTTE E 10 CORRETTAMENTE
        //REF:AggiungiDomandeDialog
        private boolean raccoltaDomande() {
            domande.clear();
            Component[] components = domandePanel.getComponents();
            for (Component component : components) {
                if (component instanceof JPanel) {
                    JPanel panel = (JPanel) component;
                    JTextField txtDomanda = (JTextField) ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                    JPanel rispostaPanel = (JPanel) ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);

                    JTextField txtRisposta = (JTextField) rispostaPanel.getComponent(1);
                    JTextField txtRispostaSbagliata1 = (JTextField) rispostaPanel.getComponent(3);
                    JTextField txtRispostaSbagliata2 = (JTextField) rispostaPanel.getComponent(5);
                    JTextField txtRispostaSbagliata3 = (JTextField) rispostaPanel.getComponent(7);


                    String testoDomanda = txtDomanda.getText();
                    String rispostaCorretta = txtRisposta.getText();
                    String rispostaSbagliata1 = txtRispostaSbagliata1.getText();
                    String rispostaSbagliata2 = txtRispostaSbagliata2.getText();
                    String rispostaSbagliata3 = txtRispostaSbagliata3.getText();


                    if (testoDomanda.isEmpty() || rispostaCorretta.isEmpty() || rispostaSbagliata1.isEmpty()
                            || rispostaSbagliata2.isEmpty() || rispostaSbagliata3.isEmpty()) {
                        return false;
                    }

                    ArrayList<String> risposteSbagliate = new ArrayList<>();
                    risposteSbagliate.add(rispostaSbagliata1);
                    risposteSbagliate.add(rispostaSbagliata2);
                    risposteSbagliate.add(rispostaSbagliata3);

                    domande.add(new Domanda(testoDomanda, rispostaCorretta,risposteSbagliate));
                }
            }
            return domande.size() == numDomande;
        }

        public List<Domanda> getDomande()
        {   return domande;
        }

        public boolean isAggiunta()
        {   return this.aggiunte;
        }

    }

}
