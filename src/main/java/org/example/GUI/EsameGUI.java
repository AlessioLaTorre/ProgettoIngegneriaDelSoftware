package org.example.GUI;

import org.example.GestioneAppello.Appello;
import org.example.GestioneAppello.Domanda;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class EsameGUI extends JDialog
{
    Appello appello;
    private int risultato = 0;

    private ArrayList<String> risultati = new ArrayList<>();
    private ArrayList<Domanda> domandeVisualizzate = new ArrayList<>();

    private int currentIteration = 0;
    private static final int TOTAL_ITERATIONS = 2;//DA MODIFICARE
    private Timer timer;

    private Timer countdown;
    private int tempoCountdown = 300;
    private final int tempoDefault = 300;

    private boolean bottonePremuto = false;

    private JTextArea textArea;
    private JPanel buttonPanel;

    public EsameGUI(JFrame parent,Appello appello)
    {   super(parent,"Appello: "+appello.getNomeEsame(),true);
        this.appello = appello;

        setSize(400, 300);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        initializeGUI();

    }

    private void initializeGUI()
    {
        textArea = new JTextArea();
        add(textArea, BorderLayout.NORTH);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2));
        add(buttonPanel, BorderLayout.SOUTH);

        caricaNuovaDomanda();
    }

    private void aggiornaCountown()
    {   if (tempoCountdown > 0) {
            tempoCountdown--;
            String domandaDaVisualizzare = textArea.getText().split("\n")[0];
            textArea.setText(domandaDaVisualizzare + "\nTempo rimanente: " + tempoCountdown + " secondi");
        } else {
            onTimeUp();
        }
    }

    private void caricaNuovaDomanda() {

        countdown = new Timer(1000,e -> aggiornaCountown());
        countdown.setRepeats(true);
        countdown.start();

        Domanda domandaDaVisualizzare = null;
        int count = 0;
        for (Domanda domanda : appello.getDomande()) {
            if (count == currentIteration) {
                domandaDaVisualizzare = domanda;
                domandeVisualizzate.add(domanda);
                break;
            }
            count++;
        }

        textArea.setText("Domanda: " + domandaDaVisualizzare.getTesto() + "\nHai 5 minuti per rispondere.");

        ArrayList<String> risposte = domandaDaVisualizzare.getRisposte();

        buttonPanel.removeAll();
        for (int j = 0; j < 4; j++) {
            JButton button = new JButton(risposte.get(j));
            button.addActionListener(new ButtonClickListener(j, risposte.get(j), domandaDaVisualizzare.getTesto()));
            buttonPanel.add(button);
        }

        buttonPanel.revalidate(); // Aggiorna il layout dei bottoni
        buttonPanel.repaint(); // Ridisegna il pannello dei bottoni

        bottonePremuto = false; // Reset per la nuova domanda
    }

    private void onTimeUp() {
        countdown.stop();
        tempoCountdown = tempoDefault;
        if (!bottonePremuto) {
            risultati.add("Nessuna risposta");
            risultato -= 1; //PUNTEGGIO ASSEGNATO PER OGNI RISPOSTA NON DATA

        }
        if (currentIteration < TOTAL_ITERATIONS - 1) {
            currentIteration++;
            caricaNuovaDomanda();
        } else {
            //showResults();
            dispose();
        }
    }

/*
    private void showResults() {
        StringBuilder resultMessage = new StringBuilder("Risultati dell'appello:\n");
        for (int i = 0; i < risultati.size(); i++) {
            resultMessage.append("La tua risposta ").append(i + 1).append(": ").append(risultati.get(i)).append("\n");
            resultMessage.append("La risposta corretta era: ").append(appello.getMappaDomande().get(domandeVisualizzate.get(i)).getFirst()).append("\n").append("\n");
        }
        resultMessage.append("hai totalizzato ").append(risultato).append(" punti");

        JOptionPane.showMessageDialog(this, resultMessage.toString(), "Risultati", JOptionPane.INFORMATION_MESSAGE);
    }
 */


    private class ButtonClickListener implements ActionListener {
        private final int buttonIndex;
        private final String risposta;
        private final String domanda;

        public ButtonClickListener(int buttonIndex, String risposta, String domanda) {
            this.buttonIndex = buttonIndex;
            this.risposta = risposta;
            this.domanda = domanda;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            bottonePremuto = true;

            // Memorizza la risposta selezionata
            risultati.add(risposta);

            // Controlla se la risposta è corretta e aggiorna il punteggio
            //if (isCorrectAnswer(domanda, risposta)) {
            //    risultato += 3; //OGNI RISPOSTA CORRETTA VALE 3 PUNTI
            //}

            onTimeUp();
        }
/*
        private boolean isCorrectAnswer(String domanda, String risposta) {
            return appello.getMappaDomande().get(domanda).getFirst().equals(risposta);
        }
    */
    }
}
