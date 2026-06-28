package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import analysis.MatchAnalysisContext;
import config.AtlasConfig;
import core.AtlasCore;
import core.AtlasCoreApi;
import model.MatchPerformance;
import model.SignalColor;
import model.StatSignal;
import service.LastGameAnalysis;

/**
 * Riot Cell — finestra sperimentale (Swing).
 *
 * Non sa nulla di Riot API, JSON o parser: parla solo con la porta del nucleo
 * ({@link AtlasCoreApi}). Due gesti separati, perché non sono la stessa cosa:
 *
 *   "Mostra ultimo noto" → VIEW    → core.getLastRiotAnalysis    (nessuna rete)
 *   "Aggiorna da Riot"    → REFRESH → core.refreshLastRiotAnalysis (rete se serve)
 *
 * Il click su Aggiorna non significa "allenati", significa
 * "controlla se c'è una nuova osservazione".
 */
public class RiotCellApp {

    private final AtlasCoreApi core = new AtlasCore();

    private JTextField riotIdField;
    private JButton viewButton;
    private JButton refreshButton;
    private JLabel statusLabel;
    private JLabel headerLabel;
    private JLabel detailLabel;
    private JPanel signalsPanel;

    /** matchId attualmente mostrato: serve a distinguere "nuova" da "invariata". */
    private String shownMatchId;

    private void buildAndShow() {
        JFrame frame = new JFrame("Riot Cell");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel title = new JLabel("Riot Cell");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        riotIdField = new JTextField(AtlasConfig.RIOT_ID, 16);

        viewButton = new JButton("Mostra ultimo noto");
        viewButton.addActionListener(e -> onView());

        refreshButton = new JButton("Aggiorna da Riot");
        refreshButton.addActionListener(e -> onRefresh());

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        inputRow.add(new JLabel("Riot ID:"));
        inputRow.add(riotIdField);
        inputRow.add(viewButton);
        inputRow.add(refreshButton);

        statusLabel = new JLabel("Pronto.");
        headerLabel = new JLabel(" ");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));

        detailLabel = new JLabel(" ");
        detailLabel.setFont(detailLabel.getFont().deriveFont(Font.PLAIN, 11f));
        detailLabel.setForeground(Color.GRAY);

        signalsPanel = new JPanel();
        signalsPanel.setLayout(new BoxLayout(signalsPanel, BoxLayout.Y_AXIS));

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        for (JComponent c : new JComponent[] {
                title, inputRow, statusLabel, headerLabel, detailLabel, signalsPanel }) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        root.add(title);
        root.add(Box.createVerticalStrut(10));
        root.add(inputRow);
        root.add(Box.createVerticalStrut(8));
        root.add(statusLabel);
        root.add(Box.createVerticalStrut(8));
        root.add(new JSeparator());
        root.add(Box.createVerticalStrut(8));
        root.add(headerLabel);
        root.add(Box.createVerticalStrut(2));
        root.add(detailLabel);
        root.add(Box.createVerticalStrut(8));
        root.add(signalsPanel);

        frame.setContentPane(root);
        frame.setMinimumSize(new Dimension(620, 440));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // All'apertura: osservare senza dimenticare. Mostra subito ciò che già sa.
        onView();
    }

    /** VIEW: legge la cache locale, nessuna chiamata a Riot. */
    private void onView() {
        String riotId = riotIdField.getText().trim();
        clearResult();
        if (riotId.isEmpty()) {
            statusLabel.setText("Inserisci un Riot ID (es. daxs#EUW).");
            return;
        }
        LastGameAnalysis known = core.getLastRiotAnalysis(riotId);
        if (known == null) {
            statusLabel.setText("Nessuna osservazione nota. Premi «Aggiorna da Riot».");
            return;
        }
        showResult(known);
        statusLabel.setText("Ultima partita conosciuta (nessuna chiamata a Riot).");
    }

    /** REFRESH: chiede al nucleo di controllare Riot. La rete va fuori dall'EDT. */
    private void onRefresh() {
        final String riotId = riotIdField.getText().trim();
        if (riotId.isEmpty()) {
            statusLabel.setText("Inserisci un Riot ID (es. daxs#EUW).");
            return;
        }

        final String before = shownMatchId;
        setBusy(true);
        statusLabel.setText("Controllo Riot...");

        new SwingWorker<LastGameAnalysis, Void>() {
            @Override
            protected LastGameAnalysis doInBackground() throws Exception {
                return core.refreshLastRiotAnalysis(riotId);
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    LastGameAnalysis analysis = get();
                    showResult(analysis);
                    String matchId = analysis.getCurrent().getMatchId();
                    statusLabel.setText(matchId.equals(before)
                        ? "Nessuna partita nuova."
                        : "Nuova partita trovata.");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setText("Errore: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void showResult(LastGameAnalysis analysis) {
        clearResult();
        MatchPerformance m = analysis.getCurrent();
        MatchAnalysisContext ctx = analysis.getContext();
        headerLabel.setText(String.format(
            "%s — %s — %s   |   Score %.2f%s",
            m.getChampion(),
            m.getRole(),
            m.isWin() ? "WIN" : "LOSS",
            analysis.getScore(),
            ctx.isReturningAfterBreak()
                ? "   |   dopo pausa +" + ctx.getHoursSinceLastGame() + "h"
                : ""));
        detailLabel.setText("match " + m.getMatchId());

        for (StatSignal s : analysis.getSignals()) {
            signalsPanel.add(signalRow(s));
            signalsPanel.add(Box.createVerticalStrut(4));
        }
        signalsPanel.add(Box.createVerticalStrut(8));
        signalsPanel.add(observationRow(analysis));
        refreshSignals();

        shownMatchId = m.getMatchId();
    }

    private JPanel signalRow(StatSignal s) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel swatch = new JPanel();
        swatch.setBackground(toColor(s.getColor()));
        swatch.setPreferredSize(new Dimension(14, 14));
        swatch.setMaximumSize(new Dimension(14, 14));

        JLabel name = new JLabel(String.format("%-11s", s.getName()));
        name.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JLabel value = new JLabel(String.format("%6.2f", s.getValue()));
        value.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JLabel reason = new JLabel("— " + s.getReason());
        reason.setForeground(Color.GRAY);

        row.add(swatch);
        row.add(name);
        row.add(value);
        row.add(reason);
        return row;
    }

    /** Osservazione sintetica derivata dai segnali: una lente, non un giudizio. */
    private JComponent observationRow(LastGameAnalysis analysis) {
        long red   = analysis.getSignals().stream().filter(s -> s.getColor() == SignalColor.RED).count();
        long green = analysis.getSignals().stream().filter(s -> s.getColor() == SignalColor.GREEN).count();

        String text;
        if (red == 0 && green > 0) {
            text = "Partita pulita: nessun segnale rosso.";
        } else if (red > 0 && green > 0) {
            text = "Luci e ombre: " + green + " verde/i, " + red + " rosso/i.";
        } else if (red > 0) {
            text = red + " segnale/i rosso/i: qui c'è qualcosa da guardare.";
        } else {
            text = "Segnali nella media.";
        }

        JLabel obs = new JLabel(text);
        obs.setAlignmentX(Component.LEFT_ALIGNMENT);
        obs.setFont(obs.getFont().deriveFont(Font.ITALIC));
        return obs;
    }

    private void clearResult() {
        signalsPanel.removeAll();
        headerLabel.setText(" ");
        detailLabel.setText(" ");
        refreshSignals();
    }

    private void setBusy(boolean busy) {
        viewButton.setEnabled(!busy);
        refreshButton.setEnabled(!busy);
    }

    private void refreshSignals() {
        signalsPanel.revalidate();
        signalsPanel.repaint();
    }

    private static Color toColor(SignalColor c) {
        switch (c) {
            case GREEN:  return new Color(0x2E, 0x7D, 0x32);
            case YELLOW: return new Color(0xF9, 0xA8, 0x25);
            case RED:    return new Color(0xC6, 0x28, 0x28);
            default:     return Color.DARK_GRAY;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RiotCellApp().buildAndShow());
    }
}
