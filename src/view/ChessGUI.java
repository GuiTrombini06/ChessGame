// ========================= src/view/ChessGUI.java =========================
package view;

import controller.Game;
import ai.IANivel3;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import model.board.Position;
import model.pieces.Pawn;
import model.pieces.Piece;
import model.board.Move;

public class ChessGUI extends JFrame {
    private static final long serialVersionUID = 1L; // evita warning de serialização

    // --- Config de cores/styles ---
    // 🎨 Cores do tabuleiro e destaques
    private static final Color LIGHT_SQ = new Color(173, 216, 230);  // azul claro
    private static final Color DARK_SQ  = new Color(70, 130, 180);   // azul escuro

    private static final Color HILITE_SELECTED = Color.RED;          // peça selecionada em vermelho
    private static final Color HILITE_LEGAL    = Color.BLACK;        // movimentos legais em preto
    private static final Color HILITE_LASTMOVE = new Color(100, 149, 237); // azul médio (CornflowerBlue)

    // 🎨 Cores da interface em volta do tabuleiro
    private static final Color BG_PANEL = new Color(220, 230, 240);      // Fundo em azul muito claro
    private static final Color TEXT_COLOR = new Color(40, 40, 40);       // Texto em cinza escuro
    private static final Color BORDER_COLOR = new Color(180, 200, 210);  // Borda mais clara
    
    private static final Border BORDER_SELECTED = new MatteBorder(2,2,2,2, HILITE_SELECTED);
    private static final Border BORDER_LEGAL    = new MatteBorder(2,2,2,2, HILITE_LEGAL);
    private static final Border BORDER_LASTMOVE = new MatteBorder(2,2,2,2, HILITE_LASTMOVE);

    private final Game game;

    private final JPanel boardPanel;
    private final JButton[][] squares = new JButton[8][8];

    private final JLabel status;
    private final JTextArea history;
    private final JScrollPane historyScroll;

    // Menu / controles
    private JCheckBoxMenuItem pcAsBlack;
    private JSpinner depthSpinner;
    private JMenuItem newGameItem, quitItem;

    // Seleção atual e movimentos legais
    private Position selected = null;
    private List<Position> legalForSelected = new ArrayList<>();

    // Realce do último lance
    private Position lastFrom = null, lastTo = null;

    // IA
    private boolean aiThinking = false;

    public ChessGUI() {
        super("ChessGame");

        // Look&Feel Nimbus
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ignored) {}

        this.game = new Game();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Menu
        setJMenuBar(buildMenuBar());

        // Painel do tabuleiro (8x8)
        boardPanel = new JPanel(new GridLayout(8, 8, 0, 0));
        boardPanel.setBackground(Color.DARK_GRAY);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

        // Cria botões das casas
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final int rr = r;
                final int cc = c;
                JButton b = new JButton();
                b.setMargin(new Insets(0, 0, 0, 0));
                b.setFocusPainted(false);
                b.setOpaque(true);
                b.setBorderPainted(true);
                b.setContentAreaFilled(true);
                b.setFont(b.getFont().deriveFont(Font.BOLD, 24f)); // fallback com Unicode
                b.addActionListener(e -> handleClick(new Position(rr, cc)));
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        // Barra inferior de status
        status = new JLabel("Vez: Vermelhas");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        status.setBackground(BG_PANEL); // Usa a cor do painel de fundo
        status.setForeground(TEXT_COLOR); // Usa a cor do texto
        status.setOpaque(true);

        // Histórico
        history = new JTextArea(14, 22);
        history.setEditable(false);
        history.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        history.setBackground(new Color(230, 240, 250)); // Fundo do histórico em azul bem claro
        history.setForeground(TEXT_COLOR);
        historyScroll = new JScrollPane(history);
        historyScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2)); // Usa a cor da borda
        historyScroll.getViewport().setBackground(new Color(230, 240, 250)); // Garante que a área visível do histórico tenha a cor correta

        // Layout principal: tabuleiro à esquerda, histórico à direita
        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        rightPanel.setBackground(BG_PANEL); // Usa a cor do painel de fundo
        rightPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        JLabel histLabel = new JLabel("Histórico de lances:");
        histLabel.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
        histLabel.setForeground(TEXT_COLOR); // Usa a cor do texto
        rightPanel.add(histLabel, BorderLayout.NORTH);
        rightPanel.add(historyScroll, BorderLayout.CENTER);
        rightPanel.add(buildSideControls(), BorderLayout.SOUTH);

        add(boardPanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // Atualiza ícones conforme a janela/painel muda de tamanho
        boardPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh(); // recarrega ícones ajustando o tamanho
            }
        });

        setMinimumSize(new Dimension(920, 680));
        setLocationRelativeTo(null);

        // Atalhos: Ctrl+N, Ctrl+Q
        setupAccelerators();

        setVisible(true);
        refresh();
        maybeTriggerAI(); // caso o PC jogue primeiro
    }

    // ----------------- Menus e controles -----------------

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(BG_PANEL); // Usa a cor do painel de fundo

        JMenu gameMenu = new JMenu("Jogo");
        gameMenu.setForeground(TEXT_COLOR); // Usa a cor do texto

        newGameItem = new JMenuItem("Novo Jogo");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newGameItem.addActionListener(e -> doNewGame());

        pcAsBlack = new JCheckBoxMenuItem("PC joga com as Amarelas");
        pcAsBlack.setSelected(false);
        pcAsBlack.setBackground(BG_PANEL); // Usa a cor do painel de fundo
        pcAsBlack.setForeground(TEXT_COLOR); // Usa a cor do texto

        JMenu depthMenu = new JMenu("Profundidade IA");
        depthMenu.setForeground(TEXT_COLOR); // Usa a cor do texto
        depthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
        depthSpinner.setToolTipText("Profundidade efetiva da IA (heurística não-minimax)");
        depthSpinner.setBackground(BG_PANEL); // Usa a cor do painel de fundo
        depthSpinner.setForeground(TEXT_COLOR); // Usa a cor do texto
        depthMenu.add(depthSpinner);

        quitItem = new JMenuItem("Sair");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        quitItem.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(pcAsBlack);
        gameMenu.add(depthMenu);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);

        mb.add(gameMenu);
        return mb;
    }

    private JPanel buildSideControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setBackground(BG_PANEL); // Usa a cor do painel de fundo
        JButton btnNew = new JButton("Novo Jogo");
        btnNew.addActionListener(e -> doNewGame());
        btnNew.setBackground(BG_PANEL); // Usa a cor do painel de fundo
        btnNew.setForeground(TEXT_COLOR); // Usa a cor do texto
        btnNew.setFocusPainted(false);
        btnNew.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true)); // Usa a cor da borda
        panel.add(btnNew);

        JCheckBox cb = new JCheckBox("PC (Amarelas)");
        cb.setBackground(BG_PANEL); // Usa a cor do painel de fundo
        cb.setForeground(TEXT_COLOR); // Usa a cor do texto
        cb.setSelected(pcAsBlack.isSelected());
        cb.addActionListener(e -> pcAsBlack.setSelected(cb.isSelected()));
        panel.add(cb);

        JLabel profLabel = new JLabel("Prof. IA:");
        profLabel.setForeground(TEXT_COLOR); // Usa a cor do texto
        panel.add(profLabel);
        
        int curDepth = ((Integer) depthSpinner.getValue()).intValue();
        JSpinner sp = new JSpinner(new SpinnerNumberModel(curDepth, 1, 3, 1));
        sp.addChangeListener(e -> depthSpinner.setValue(sp.getValue()));
        sp.setBackground(BG_PANEL); // Usa a cor do painel de fundo
        sp.setForeground(TEXT_COLOR); // Usa a cor do texto
        sp.getEditor().getComponent(0).setBackground(BG_PANEL); // Garante que o editor também tenha a cor correta
        sp.getEditor().getComponent(0).setForeground(TEXT_COLOR);
        panel.add(sp);

        return panel;
    }

    private void setupAccelerators() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "newGame");
        getRootPane().getActionMap().put("newGame", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { doNewGame(); }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "quit");
        getRootPane().getActionMap().put("quit", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                dispatchEvent(new WindowEvent(ChessGUI.this, WindowEvent.WINDOW_CLOSING));
            }
        });
    }

    private void doNewGame() {
        selected = null;
        legalForSelected.clear();
        lastFrom = lastTo = null;
        aiThinking = false;
        game.newGame();
        refresh();
        maybeTriggerAI();
    }

    // ----------------- Interação de tabuleiro -----------------

    private void handleClick(Position clicked) {
        if (game.isGameOver() || aiThinking) return;

        // Se for vez do PC (pretas) e modo PC ativado, ignore cliques
        if (pcAsBlack.isSelected() && !game.whiteToMove()) return;

        Piece p = game.board().get(clicked);

        if (selected == null) {
            // Nada selecionado ainda: só seleciona se for peça da vez
            if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            }
        } else {
            // Já havia uma seleção
            List<Position> legals = game.legalMovesFrom(selected); // recalc por segurança
            if (legals.contains(clicked)) {
                Character promo = null;
                Piece moving = game.board().get(selected);
                if (moving instanceof Pawn && game.isPromotion(selected, clicked)) {
                    promo = askPromotion();
                }
                lastFrom = selected;
                lastTo   = clicked;

                game.move(selected, clicked, promo);

                selected = null;
                legalForSelected.clear();

                refresh();
                maybeAnnounceEnd();
                maybeTriggerAI();
                return;
            } else if (p != null && p.isWhite() == game.whiteToMove()) {
                // Troca a seleção para outra peça da vez
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            } else {
                // Clique inválido: limpa seleção
                selected = null;
                legalForSelected.clear();
            }
        }
        refresh();
    }

    private Character askPromotion() {
        String[] opts = {"Rainha", "Torre", "Bispo", "Cavalo"};
        int ch = JOptionPane.showOptionDialog(
                this,
                "Escolha a peça para promoção:",
                "Promocao",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opts,
                opts[0]
        );
        return switch (ch) {
            case 1 -> 'R';
            case 2 -> 'B';
            case 3 -> 'N';
            default -> 'Q';
        };
    }

    // ----------------- IA (não bloqueante) -----------------

    private void maybeTriggerAI() {
        if (game.isGameOver()) return;
        if (!pcAsBlack.isSelected()) return;
        if (game.whiteToMove()) return; // PC joga de pretas

        aiThinking = true;
        status.setText("Vez: Amarelas — PC pensando...");
        final int depth = (Integer) depthSpinner.getValue();

        new SwingWorker<Void, Void>() {
            Move aiMove = null;
            @Override
            protected Void doInBackground() {
                IANivel3 ai = new IANivel3(depth);
                aiMove = ai.makeMove(game);
                return null;
            }

            @Override
            protected void done() {
                try { get(); } catch (Exception ignored) {}

                if (aiMove != null && !game.isGameOver() && !game.whiteToMove()) {
                    lastFrom = aiMove.getFrom();
                    lastTo   = aiMove.getTo();
                    Character promo = aiMove.getPromotion();
                    game.move(lastFrom, lastTo, promo);
                }
                aiThinking = false;
                refresh();
                maybeAnnounceEnd();
            }
        }.execute();
    }

    // ----------------- Código original (não mexido) -----------------
    private String toUnicode(String sym, boolean white) {
        return switch (sym) {
            case "K" -> white ? "\u2654" : "\u265A";
            case "Q" -> white ? "\u2655" : "\u265B";
            case "R" -> white ? "\u2656" : "\u265C";
            case "B" -> white ? "\u2657" : "\u265D";
            case "N" -> white ? "\u2658" : "\u265E";
            case "P" -> white ? "\u2659" : "\u265F";
            default -> "";
        };
    }

    private void maybeAnnounceEnd() {
        if (!game.isGameOver()) return;
        String msg;
        if (game.inCheck(game.whiteToMove())) {
            msg = "Xeque-mate! " + (game.whiteToMove() ? "Vermelhas" : "Amarelas") + " estão em mate.";
        } else {
            msg = "Empate por afogamento (stalemate).";
        }
        JOptionPane.showMessageDialog(this, msg, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private int computeSquareIconSize() {
        JButton b = squares[0][0];
        int w = Math.max(1, b.getWidth());
        int h = Math.max(1, b.getHeight());
        int side = Math.min(w, h);
        if (side <= 1) return 64;
        return Math.max(24, side - 8);
    }
    
    private void refresh() {
        // 1) Cores base e limpa bordas
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                Color base = light ? LIGHT_SQ : DARK_SQ;
                JButton b = squares[r][c];
                b.setBackground(base);
                b.setBorder(null);
                b.setToolTipText(null);
            }
        }

        // 2) Realce último lance
        if (lastFrom != null) squares[lastFrom.getRow()][lastFrom.getColumn()].setBorder(BORDER_LASTMOVE);
        if (lastTo   != null) squares[lastTo.getRow()][lastTo.getColumn()].setBorder(BORDER_LASTMOVE);

        // 3) Realce seleção e movimentos legais
        if (selected != null) {
            squares[selected.getRow()][selected.getColumn()].setBorder(BORDER_SELECTED);
            for (Position d : legalForSelected) {
                squares[d.getRow()][d.getColumn()].setBorder(BORDER_LEGAL);
            }
        }

        // 4) Ícones das peças (ou Unicode como fallback)
        int iconSize = computeSquareIconSize();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.board().get(new Position(r, c));
                JButton b = squares[r][c];

                if (p == null) {
                    b.setIcon(null);
                    b.setText("");
                    continue;
                }

                char sym = p.getSymbol().charAt(0);
                ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), sym, iconSize);
                if (icon != null) {
                    b.setIcon(icon);
                    b.setText("");
                } else {
                    b.setIcon(null);
                    b.setText(toUnicode(p.getSymbol(), p.isWhite()));
                }
            }
        }

        // 5) Status e histórico
        String side = game.whiteToMove() ? "Vermelhas" : "Amarelas";
        String chk = game.inCheck(game.whiteToMove()) ? " — Xeque!" : "";
        if (aiThinking) chk = " — PC pensando...";
        status.setText("Vez: " + side + chk);

        StringBuilder sb = new StringBuilder();
        var hist = game.history();
        for (int i = 0; i < hist.size(); i++) {
            if (i % 2 == 0) sb.append((i / 2) + 1).append('.').append(' ');
            sb.append(hist.get(i)).append(' ');
            if (i % 2 == 1) sb.append('\n');
        }
        history.setText(sb.toString());
        history.setCaretPosition(history.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}