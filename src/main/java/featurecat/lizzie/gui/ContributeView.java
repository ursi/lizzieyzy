package featurecat.lizzie.gui;

import featurecat.lizzie.Config;
import featurecat.lizzie.Lizzie;
import featurecat.lizzie.util.DocType;
import featurecat.lizzie.util.Utils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.json.JSONObject;

public class ContributeView extends JFrame {
  private JTextField txtGameIndex;
  private JTextField txtAutoPlayInterval;
  private JLabel lblGameInfos;
  private String result = "";
  private JLabel lblGameResult;
  private JLabel lblGameType;
  private JLabel lblKomi;
  private JLabel lblTip;
  private JIMSendTextPane console;
  private JTextPane txtRules;
  private int finishedGames = 0;
  private int playingGames = 0;
  private int watchingGameIndex = 0;
  private JTextField txtMoveNumber;
  private ArrayDeque<DocType> docQueue;
  private ScheduledExecutorService executor;
  private JButton btnPauseResume;
  private JButton btnSlowShutdown;
  private JButton btnForceShutdown;
  private JButton btnCloseView;
  private JCheckBox chkIgnoreNone19;
  private boolean exitedAfterSignal = false;
  private int max_length = 15000;

  public ContributeView() {
    exitedAfterSignal = false;
    setTitle(Lizzie.resourceBundle.getString("ContributeView.title")); // ("KataGo跑谱贡献");
    setResizable(false);
    try {
      setIconImage(ImageIO.read(getClass().getResourceAsStream("/assets/logo.png")));
    } catch (IOException e) {
      e.printStackTrace();
    }
    JPanel mainPanel = new JPanel();
    mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    getContentPane().add(mainPanel, BorderLayout.CENTER);
    GridBagLayout gbl_mainPanel = new GridBagLayout();
    gbl_mainPanel.rowWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0};
    gbl_mainPanel.columnWeights = new double[] {1.0};
    mainPanel.setLayout(gbl_mainPanel);

    JPanel labelPanel = new JPanel();
    labelPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
    GridBagConstraints gbc_panel_1 = new GridBagConstraints();
    gbc_panel_1.insets = new Insets(0, 0, 0, 0);
    gbc_panel_1.fill = GridBagConstraints.BOTH;
    gbc_panel_1.gridx = 0;
    gbc_panel_1.gridy = 0;
    mainPanel.add(labelPanel, gbc_panel_1);

    lblGameInfos = new JFontLabel();
    labelPanel.add(lblGameInfos);

    JButton btnSaveGameRecords =
        new JFontButton(
            Lizzie.resourceBundle.getString("ContributeView.btnSaveGameRecords")); // ("保存所有棋谱");
    labelPanel.add(btnSaveGameRecords);
    btnSaveGameRecords.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (Lizzie.frame.contributeEngine != null) Lizzie.frame.contributeEngine.saveAllGames();
          }
        });
    updateLblGameInfos();

    JPanel gameControlPanel = new JPanel();
    gameControlPanel.setLayout(new FlowLayout(1, 4, 2));
    gameControlPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
    GridBagConstraints gbc_gameControlPanel = new GridBagConstraints();
    gbc_gameControlPanel.fill = GridBagConstraints.BOTH;
    gbc_gameControlPanel.insets = new Insets(0, 0, 0, 0);
    gbc_gameControlPanel.gridx = 0;
    gbc_gameControlPanel.gridy = 1;
    mainPanel.add(gameControlPanel, gbc_gameControlPanel);

    JButton btnFirstGame = new JButton("|<");
    btnFirstGame.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (Lizzie.frame.contributeEngine != null)
              Lizzie.frame.contributeEngine.setWatchGame(0);
          }
        });
    gameControlPanel.add(btnFirstGame);

    JButton btnPrevious =
        new JFontButton(Lizzie.resourceBundle.getString("ContributeView.btnPrevious")); // ("上一局");
    btnPrevious.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (Lizzie.frame.contributeEngine != null)
              Lizzie.frame.contributeEngine.setWatchGame(
                  Lizzie.frame.contributeEngine.watchingGameIndex - 1);
          }
        });
    gameControlPanel.add(btnPrevious);

    JButton btnNext =
        new JFontButton(Lizzie.resourceBundle.getString("ContributeView.btnNext")); // ("下一局");
    btnNext.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (Lizzie.frame.contributeEngine != null)
              Lizzie.frame.contributeEngine.setWatchGame(
                  Lizzie.frame.contributeEngine.watchingGameIndex + 1);
          }
        });
    gameControlPanel.add(btnNext);

    JButton btnLastGame = new JButton(">|");
    btnLastGame.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (Lizzie.frame.contributeEngine != null)
              Lizzie.frame.contributeEngine.setWatchGame(
                  Lizzie.frame.contributeEngine.getGameMaxIndex());
          }
        });
    gameControlPanel.add(btnLastGame);

    JLabel lblGoto =
        new JFontLabel(Lizzie.resourceBundle.getString("ContributeView.lblGoto")); // ("跳转");
    gameControlPanel.add(lblGoto);

    txtGameIndex = new JFontTextField();
    gameControlPanel.add(txtGameIndex);
    txtGameIndex.setColumns(3);
    txtGameIndex.setDocument(new IntDocument());

    JButton btnGotoGame =
        new JFontButton(Lizzie.resourceBundle.getString("ContributeView.btnGotoGame")); // ("确定");
    btnGotoGame.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            try {
              int index = Integer.parseInt(txtGameIndex.getText()) - 1;
              if (Lizzie.frame.contributeEngine != null)
                Lizzie.frame.contributeEngine.setWatchGame(index);
              txtGameIndex.setText("");
            } catch (NumberFormatException ex) {
              ex.printStackTrace();
            }
          }
        });
    gameControlPanel.add(btnGotoGame);

    JPanel playPanel = new JPanel();
    playPanel.setLayout(new FlowLayout(1, 4, 4));
    playPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
    GridBagConstraints gbc_playPanel = new GridBagConstraints();
    gbc_playPanel.insets = new Insets(0, 0, 0, 0);
    gbc_playPanel.fill = GridBagConstraints.BOTH;
    gbc_playPanel.gridx = 0;
    gbc_playPanel.gridy = 2;
    mainPanel.add(playPanel, gbc_playPanel);

    JButton btnFirst = new JFontButton("|<");
    btnFirst.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.frame.firstMove();
          }
        });
    playPanel.add(btnFirst);

    JButton btnPrevious10 = new JFontButton("<<");
    btnPrevious10.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (Lizzie.frame.commentEditPane.isVisible()) Lizzie.frame.setCommentEditable(false);
            for (int i = 0; i < 10; i++) Lizzie.board.previousMove(false);
            Lizzie.board.clearAfterMove();
            Lizzie.frame.refresh();
          }
        });
    playPanel.add(btnPrevious10);

    JButton btnPrevious1 = new JFontButton("<");
    btnPrevious1.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (Lizzie.frame.commentEditPane.isVisible()) Lizzie.frame.setCommentEditable(false);
            Lizzie.board.previousMove(true);
          }
        });
    playPanel.add(btnPrevious1);

    JButton btnNext1 = new JFontButton(">");
    btnNext1.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (Lizzie.frame.commentEditPane.isVisible()) Lizzie.frame.setCommentEditable(false);
            Lizzie.board.nextMove(true);
          }
        });
    playPanel.add(btnNext1);

    JButton btnNext10 = new JFontButton(">>");
    btnNext10.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (Lizzie.frame.commentEditPane.isVisible()) Lizzie.frame.setCommentEditable(false);
            for (int i = 0; i < 10; i++) Lizzie.board.nextMove(false);
          }
        });
    playPanel.add(btnNext10);

    JButton btnLast = new JFontButton(">|");
    btnLast.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.frame.lastMove();
          }
        });
    playPanel.add(btnLast);

    btnFirst.setMargin(new Insets(2, 5, 2, 5));
    btnPrevious10.setMargin(new Insets(2, 5, 2, 5));
    btnPrevious1.setMargin(new Insets(2, 8, 2, 8));
    btnNext1.setMargin(new Insets(2, 8, 2, 8));
    btnNext10.setMargin(new Insets(2, 5, 2, 5));
    btnLast.setMargin(new Insets(2, 5, 2, 5));

    txtMoveNumber = new JFontTextField();
    playPanel.add(txtMoveNumber);
    txtMoveNumber.setColumns(3);
    txtMoveNumber.setDocument(new IntDocument());

    JButton btnGotoMove =
        new JFontButton(Lizzie.resourceBundle.getString("ContributeView.btnGotoMove")); // ("跳转");
    btnGotoMove.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            try {
              Lizzie.board.goToMoveNumberBeyondBranch(Integer.parseInt(txtMoveNumber.getText()));
              txtMoveNumber.setText("");
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
    playPanel.add(btnGotoMove);
    btnGotoMove.setMargin(new Insets(2, 5, 2, 5));

    JCheckBox chkAlwaysLastMove =
        new JFontCheckBox(
            Lizzie.resourceBundle.getString("ContributeView.chkAlwaysLastMove")); // ("自动跳转最新一手");
    playPanel.add(chkAlwaysLastMove);
    chkAlwaysLastMove.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.config.contributeWatchAlwaysLastMove = chkAlwaysLastMove.isSelected();
            Lizzie.config.uiConfig.put(
                "contribute-watch-always-last-move", Lizzie.config.contributeWatchAlwaysLastMove);
            if (Lizzie.config.contributeWatchAlwaysLastMove) Lizzie.frame.lastMove();
          }
        });
    chkAlwaysLastMove.setSelected(Lizzie.config.contributeWatchAlwaysLastMove);

    JPanel autoPlayPanel = new JPanel();
    autoPlayPanel.setLayout(new FlowLayout(1, 2, 2));
    autoPlayPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
    GridBagConstraints gbc_autoPlayPanel = new GridBagConstraints();
    gbc_autoPlayPanel.insets = new Insets(0, 0, 0, 0);
    gbc_autoPlayPanel.fill = GridBagConstraints.BOTH;
    gbc_autoPlayPanel.gridx = 0;
    gbc_autoPlayPanel.gridy = 3;
    mainPanel.add(autoPlayPanel, gbc_autoPlayPanel);

    JCheckBox chkAutoPlay =
        new JFontCheckBox(
            Lizzie.resourceBundle.getString("ContributeView.chkAutoPlay")); // ("自动播放");
    autoPlayPanel.add(chkAutoPlay);
    chkAutoPlay.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.config.contributeWatchAutoPlay = chkAutoPlay.isSelected();
            Lizzie.config.uiConfig.put(
                "contribute-watch-auto-play", Lizzie.config.contributeWatchAutoPlay);
          }
        });
    chkAutoPlay.setSelected(Lizzie.config.contributeWatchAutoPlay);

    JLabel lblAutoPlayInterval =
        new JFontLabel(
            Lizzie.resourceBundle.getString("ContributeView.lblAutoPlayInterval")); // ("每手时间(秒)");
    autoPlayPanel.add(lblAutoPlayInterval);

    txtAutoPlayInterval = new JFontTextField();
    autoPlayPanel.add(txtAutoPlayInterval);
    txtAutoPlayInterval.setColumns(3);
    txtAutoPlayInterval.setDocument(new DoubleDocument());
    Document dtTxtAutoPlayInterval = txtAutoPlayInterval.getDocument();
    dtTxtAutoPlayInterval.addDocumentListener(
        new DocumentListener() {
          public void insertUpdate(DocumentEvent e) {
            dtTxtAutoPlayIntervalUpdate();
          }

          public void removeUpdate(DocumentEvent e) {
            dtTxtAutoPlayIntervalUpdate();
          }

          public void changedUpdate(DocumentEvent e) {
            dtTxtAutoPlayIntervalUpdate();
          }

          private void dtTxtAutoPlayIntervalUpdate() {
            double time = Utils.parseTextToDouble(txtAutoPlayInterval, -1.0);
            if (time > 0) {
              Lizzie.config.contributeWatchAutoPlayInterval = time;
              Lizzie.config.uiConfig.put(
                  "contribute-watch-auto-play-interval",
                  Lizzie.config.contributeWatchAutoPlayInterval);
            }
          }
        });
    txtAutoPlayInterval.setText(String.valueOf(Lizzie.config.contributeWatchAutoPlayInterval));

    JCheckBox chkAutoPlayNextGame =
        new JFontCheckBox(
            Lizzie.resourceBundle.getString("ContributeView.chkAutoPlayNextGame")); // ("自动播放下一局");
    autoPlayPanel.add(chkAutoPlayNextGame);
    chkAutoPlayNextGame.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.config.contributeWatchAutoPlayNextGame = chkAutoPlayNextGame.isSelected();
            chkIgnoreNone19.setEnabled(chkAutoPlayNextGame.isSelected());
            Lizzie.config.uiConfig.put(
                "contribute-watch-auto-play-next-game",
                Lizzie.config.contributeWatchAutoPlayNextGame);
          }
        });
    chkAutoPlayNextGame.setSelected(Lizzie.config.contributeWatchAutoPlayNextGame);

    chkIgnoreNone19 =
        new JFontCheckBox(
            Lizzie.resourceBundle.getString("ContributeView.chkIgnoreNone19")); // ("跳过非19x19");
    autoPlayPanel.add(chkIgnoreNone19);
    chkIgnoreNone19.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.config.contributeWatchSkipNone19 = chkIgnoreNone19.isSelected();
            Lizzie.config.uiConfig.put(
                "contribute-watch-skip-none-19", Lizzie.config.contributeWatchSkipNone19);
          }
        });
    chkIgnoreNone19.setSelected(Lizzie.config.contributeWatchSkipNone19);
    chkIgnoreNone19.setEnabled(chkAutoPlayNextGame.isSelected());

    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout(1, 10, 2));
    panel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
    GridBagConstraints gbc_panel = new GridBagConstraints();
    gbc_panel.fill = GridBagConstraints.BOTH;
    gbc_panel.gridx = 0;
    gbc_panel.gridy = 4;
    mainPanel.add(panel, gbc_panel);

    lblGameType =
        new JFontLabel(
            Lizzie.resourceBundle.getString("ContributeView.lblGameType")); // ("本局类型:  ");
    panel.add(lblGameType);

    lblKomi =
        new JFontLabel(Lizzie.resourceBundle.getString("ContributeView.lblKomi")); // ("贴目: ");
    panel.add(lblKomi);

    lblGameResult =
        new JFontLabel(
            Lizzie.resourceBundle.getString("ContributeView.lblGameResult")); // ("结果: ");
    panel.add(lblGameResult);

    JButton btnHideShowResult = new JFontButton();
    btnHideShowResult.setText(
        Lizzie.config.contributeHideResult
            ? Lizzie.resourceBundle.getString("ContributeView.show")
            : Lizzie.resourceBundle.getString("ContributeView.hide"));
    setResult(result);
    btnHideShowResult.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.config.contributeHideResult = !Lizzie.config.contributeHideResult;
            btnHideShowResult.setText(
                Lizzie.config.contributeHideResult
                    ? Lizzie.resourceBundle.getString("ContributeView.show")
                    : Lizzie.resourceBundle.getString("ContributeView.hide"));
            setResult(result);
          }
        });
    panel.add(btnHideShowResult);

    btnHideShowResult.setMargin(new Insets(1, 7, 1, 7));

    txtRules = new JTextPane();
    txtRules.setEditable(false);
    JPanel ruleAndButtonPanel = new JPanel();
    getContentPane().add(ruleAndButtonPanel, BorderLayout.SOUTH);
    txtRules.setText(
        Lizzie.resourceBundle.getString("ContributeView.rules.scoring")
            + "\r\n"
            + Lizzie.resourceBundle.getString("ContributeView.rules.ko")
            + "\r\n"
            + Lizzie.resourceBundle.getString("ContributeView.rules.suicide")
            + "\r\n"
            + Lizzie.resourceBundle.getString("ContributeView.rules.tax")
            + "\r\n"
            + Lizzie.resourceBundle.getString("ContributeView.rules.whiteHandicapBonus")
            + "\r\n"
            + Lizzie.resourceBundle.getString("ContributeView.rules.button"));

    ruleAndButtonPanel.setLayout(new BorderLayout());
    JPanel rulePanel = new JPanel();
    rulePanel.setLayout(new BorderLayout(0, 0));
    rulePanel.add(txtRules);
    ruleAndButtonPanel.add(rulePanel, BorderLayout.CENTER);

    JButton btnShowHideRules = new JFontButton();
    btnShowHideRules.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.config.contributeHideRules = !Lizzie.config.contributeHideRules;
            Lizzie.config.uiConfig.put("contribute-hide-rules", Lizzie.config.contributeHideRules);
            if (Lizzie.config.contributeHideRules) txtRules.setVisible(false);
            else txtRules.setVisible(true);
            btnShowHideRules.setText(
                Lizzie.config.contributeHideRules
                    ? Lizzie.resourceBundle.getString("ContributeView.showRules")
                    : Lizzie.resourceBundle.getString("ContributeView.hideRules"));
            pack();
          }
        });
    btnShowHideRules.setText(
        Lizzie.config.contributeHideRules
            ? Lizzie.resourceBundle.getString("ContributeView.showRules")
            : Lizzie.resourceBundle.getString("ContributeView.hideRules"));
    if (Lizzie.config.contributeHideRules) txtRules.setVisible(false);
    else txtRules.setVisible(true);

    rulePanel.add(btnShowHideRules, BorderLayout.SOUTH);
    JPanel buttonPanel = new JPanel();
    ruleAndButtonPanel.add(buttonPanel, BorderLayout.SOUTH);

    btnSlowShutdown =
        new JFontButton(Lizzie.resourceBundle.getString("ContributeView.btnSlowShutdown"));
    btnSlowShutdown.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (Lizzie.frame.contributeEngine != null) {
              if (Lizzie.frame.contributeEngine.paused) Lizzie.frame.contributeEngine.togglePause();
              Lizzie.frame.contributeEngine.slowQuit();
            }
          }
        });

    btnSlowShutdown.setToolTipText(
        Lizzie.resourceBundle.getString("ContributeView.btnSlowShutdownTip"));

    btnForceShutdown =
        new JFontButton(
            Lizzie.resourceBundle.getString(
                "ContributeView.btnForceShutdown")); // "结束跑谱贡献");btnForceShutdown
    btnForceShutdown.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (Lizzie.frame.contributeEngine != null) Lizzie.frame.contributeEngine.normalQuit();
            setVisible(false);
          }
        });
    btnForceShutdown.setToolTipText(
        Lizzie.resourceBundle.getString("ContributeView.btnForceShutdownTip"));
    //    if (Lizzie.frame.contributeEngine != null)
    //      setSlowShutdownButton(Lizzie.frame.contributeEngine.canSlowClose());

    btnPauseResume = new JFontButton(Lizzie.resourceBundle.getString("ContributeView.btnPause"));
    btnPauseResume.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (Lizzie.frame.contributeEngine != null)
              Lizzie.frame.contributeEngine.pauseAndResume();
          }
        });

    buttonPanel.add(btnPauseResume);
    buttonPanel.add(btnSlowShutdown);
    buttonPanel.add(btnForceShutdown);

    btnCloseView = new JFontButton(Lizzie.resourceBundle.getString("ContributeView.btnCloseView"));
    btnCloseView.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            setVisible(false);
          }
        });
    buttonPanel.add(btnCloseView);
    btnCloseView.setVisible(false);

    JPanel consolePanel = new JPanel();
    consolePanel.setLayout(new BorderLayout());
    getContentPane().add(consolePanel, BorderLayout.NORTH);

    docQueue = new ArrayDeque<>();
    console = new JIMSendTextPane(false);
    console.setFont(new Font(Config.sysDefaultFontName, Font.PLAIN, Config.frameFontSize));
    console.setEditable(false);
    console.setBackground(Color.BLACK);
    console.setForeground(Color.LIGHT_GRAY);

    Font gtpFont;
    try {
      gtpFont =
          Font.createFont(
              Font.TRUETYPE_FONT,
              Thread.currentThread()
                  .getContextClassLoader()
                  .getResourceAsStream("fonts/SourceCodePro-Regular.ttf"));

      gtpFont = gtpFont.deriveFont(Font.PLAIN, Config.frameFontSize);
    } catch (IOException | FontFormatException e) {
      e.printStackTrace();
      gtpFont = new Font(Font.MONOSPACED, Font.PLAIN, Config.frameFontSize);
    }
    console.setFont(gtpFont);

    JScrollPane scrollConsole = new JScrollPane(console);
    JPanel consoleTextPane = new JPanel();
    consoleTextPane.setLayout(new BorderLayout());
    lblTip =
        new JFontLabel(Lizzie.resourceBundle.getString("ContributeView.lblTip")); // ("正在初始化...");
    lblTip.setBorder(new EmptyBorder(0, 4, 2, 0));
    consoleTextPane.add(lblTip, BorderLayout.SOUTH);
    consoleTextPane.add(scrollConsole, BorderLayout.CENTER);
    consolePanel.add(consoleTextPane, BorderLayout.CENTER);
    scrollConsole.setPreferredSize(
        new Dimension((int) consoleTextPane.getPreferredSize().getWidth(), Config.menuHeight * 7));

    JPanel consoleButtonPane = new JPanel();
    consoleButtonPane.setLayout(new BorderLayout(0, 0));
    JButton btnHideShowConsole = new JFontButton();
    consoleButtonPane.add(btnHideShowConsole);

    btnHideShowConsole.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.config.contributeHideConsole = !Lizzie.config.contributeHideConsole;
            if (Lizzie.config.contributeHideConsole) scrollConsole.setVisible(false);
            else scrollConsole.setVisible(true);
            btnHideShowConsole.setText(
                Lizzie.config.contributeHideConsole
                    ? Lizzie.resourceBundle.getString("ContributeView.showConsole")
                    : Lizzie.resourceBundle.getString("ContributeView.hideConsole"));
            pack();
            Lizzie.config.uiConfig.put(
                "contribute-hide-console", Lizzie.config.contributeHideConsole);
          }
        });

    btnHideShowConsole.setText(
        Lizzie.config.contributeHideConsole
            ? Lizzie.resourceBundle.getString("ContributeView.showConsole")
            : Lizzie.resourceBundle.getString("ContributeView.hideConsole"));
    if (Lizzie.config.contributeHideConsole) scrollConsole.setVisible(false);
    else scrollConsole.setVisible(true);
    consolePanel.add(consoleButtonPane, BorderLayout.SOUTH);

    JPanel panel_1 = new JPanel();
    panel_1.setLayout(new FlowLayout(0, 0, 0));
    consoleButtonPane.add(panel_1, BorderLayout.EAST);

    JButton btnFullConsole =
        new JFontButton(
            Lizzie.resourceBundle.getString("ContributeView.btnFullConsole")); // "完整控制台");
    panel_1.add(btnFullConsole);

    JCheckBox chkAlwaysTop =
        new JCheckBox(Lizzie.resourceBundle.getString("ContributeView.chkAlwaysTop"));
    panel_1.add(chkAlwaysTop);
    chkAlwaysTop.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.config.contributeViewAlwaysTop = chkAlwaysTop.isSelected();
            Lizzie.config.uiConfig.put(
                "contribute-always-top", Lizzie.config.contributeViewAlwaysTop);
            setAlwaysOnTop(Lizzie.config.contributeViewAlwaysTop);
          }
        });
    btnFullConsole.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.frame.toggleGtpConsole();
          }
        });

    executor = Executors.newSingleThreadScheduledExecutor();
    executor.execute(this::read);

    this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            if (exitedAfterSignal) setVisible(false);
            else
              Utils.showMsg(
                  Lizzie.resourceBundle.getString(
                      "ContributeView.closeTip")); // ("请使用结束跑普贡献关闭此窗口");
          }
        });

    pack();
    int width = this.getWidth();
    int height = this.getHeight();
    int frameX = Lizzie.frame.getX();
    int frameY = Lizzie.frame.getY();
    Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
    int screenWidth = screensize.width;
    int screenHeight = screensize.height;
    int boardX = Lizzie.frame.boardX;
    int boardY =
        Lizzie.frame.boardY
            + Utils.zoomIn(Lizzie.frame.mainPanel.getY())
            + Config.menuHeight
            + Lizzie.frame.topPanel.getHeight();
    int boardLenght = Lizzie.frame.maxSize;
    if (Lizzie.config.isFloatBoardMode() && Lizzie.frame.independentMainBoard != null) {
      frameX = Lizzie.frame.independentMainBoard.getX();
      frameY = Lizzie.frame.independentMainBoard.getY();
      boardX = 0;
      boardY = 0;
      boardLenght = Lizzie.frame.independentMainBoard.getWidth();
    }
    if (frameX + boardX + boardLenght + width + 5 <= screenWidth)
      this.setLocation(
          frameX + boardX + boardLenght + 5,
          Math.min(frameY + boardY + boardLenght / 2 - height * 2 / 3, screenHeight - height));
    else if (frameX + boardX - width >= 0)
      this.setLocation(
          frameX + boardX - width,
          Math.min(frameY + boardY + boardLenght / 2 - height * 2 / 3, screenHeight - height));
    else if (frameY + boardY - height > 0)
      this.setLocation(frameX + boardX + boardLenght / 2 - width / 2, frameY + boardY - height);
    else if (frameY + boardY + boardLenght + height <= screenHeight)
      this.setLocation(
          frameX + boardX + boardLenght / 2 - width / 2, frameY + boardY + boardLenght);
    else
      setLocation(
          screenWidth - width,
          Math.min(frameY + boardY + boardLenght / 2 - height * 2 / 3, screenHeight - height));
    setVisible(true);
  }

  //  public void setSlowShutdownButton(boolean set) {
  //    // TODO Auto-generated method stub
  //    if (set) {
  //      btnSlowShutdown.setVisible(true);
  //
  // btnForceShutdown.setText(Lizzie.resourceBundle.getString("ContributeView.btnForceShutdown"));
  //      btnForceShutdown.setToolTipText(
  //          Lizzie.resourceBundle.getString("ContributeView.btnForceShutdownTip"));
  //    } else {
  //      btnSlowShutdown.setVisible(false);
  //      btnForceShutdown.setText(Lizzie.resourceBundle.getString("ContributeView.btnShutdown"));
  //    }
  //  }

  public void setType(String text) {
    lblGameType.setText(Lizzie.resourceBundle.getString("ContributeView.lblGameType") + text);
  }

  public void setKomi(double komi) {
    lblKomi.setText(
        Lizzie.resourceBundle.getString("ContributeView.lblKomi")
            + String.format(Locale.ENGLISH, "%.1f", komi));
  }

  public void setResult(String text) {
    result = text;
    lblGameResult.setText(
        Lizzie.resourceBundle.getString("ContributeView.lblGameResult")
            + (Lizzie.config.contributeHideResult
                ? "---"
                : text.length() > 0
                    ? text
                    : Lizzie.resourceBundle.getString("ContributeView.lblGameResult.none")));
  }

  public void setTip(String text) {
    lblTip.setText(text);
  }

  public void setRules(JSONObject jsonRules) {
    //	  txtRules.setText(
    //	    		"胜负判断:数子\r\n"
    //	            + "打劫:严格禁全同\r\n"
    //	            + "允许棋块自杀:是\r\n"
    //	            + "还棋头:否\r\n"
    //	            + "让子贴还(让N子): 贴还N目\r\n"
    //	            + "收后贴还0.5目: 否");
    txtRules.setText(Utils.getRuleString(jsonRules));
    pack();
  }

  public void setGames(int finishedGames, int playingGames) {
    this.finishedGames = finishedGames;
    this.playingGames = playingGames;
    updateLblGameInfos();
  }

  public void setWathGameIndex(int watchingGameIndex) {
    this.watchingGameIndex = watchingGameIndex;
    updateLblGameInfos();
  }

  private void updateLblGameInfos() {
    lblGameInfos.setText(
        "<html>"
            + Lizzie.resourceBundle.getString("ContributeView.lblGameInfos.watching")
            + "<a color=\"blue\">"
            + watchingGameIndex
            + "/"
            + (finishedGames + playingGames)
            + "</a>"
            + ","
            + Lizzie.resourceBundle.getString("ContributeView.lblGameInfos.complete")
            + finishedGames
            + Lizzie.resourceBundle.getString("ContributeView.lblGameInfos.games")
            + "</html>");
    //            + "正在观看第"
    //            + "<a color=\"blue\">"
    //            + watchingGameIndex
    //            + "/"
    //            + (finishedGames + playingGames)
    //            + "</a>"
    //            + "局"+","+"已完成"
    //            + finishedGames
    //            + "局"
    //            + "</html>");
  }

  public void setBtnPauseResume(boolean paused) {
    btnPauseResume.setText(
        paused
            ? Lizzie.resourceBundle.getString("ContributeView.btnResume")
            : Lizzie.resourceBundle.getString("ContributeView.btnPause"));
  }

  private void checkConsole() {
    Document doc = console.getDocument();
    int length = doc.getLength();
    try {
      if (length > max_length) {
        doc.remove(0, length - max_length / 2);
      }
    } catch (BadLocationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void addDocs(DocType doc) {
    SimpleAttributeSet attrSet = new SimpleAttributeSet();
    StyleConstants.setForeground(attrSet, doc.contentColor);
    if (doc.isCommand) {
      StyleConstants.setFontFamily(attrSet, Lizzie.config.uiFontName);
    }
    StyleConstants.setFontSize(attrSet, doc.fontSize);
    String insertContent = doc.content;
    if (insertContent.length() > max_length / 10) {
      insertContent = insertContent.substring(0, max_length / 10 - 5) + "(...)\n";
    }
    insert(insertContent, attrSet);
    console.setCaretPosition(console.getDocument().getLength());
  }

  private void insert(String str, AttributeSet attrSet) {
    Document doc = console.getDocument();
    try {
      doc.insertString(doc.getLength(), str, attrSet);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  private void setDocs(String str, Color col, boolean isCommand, int fontSize) {
    DocType doc = new DocType();
    doc.content = str;
    doc.contentColor = col;
    doc.isCommand = isCommand;
    doc.fontSize = fontSize;
    docQueue.addLast(doc);
  }

  public void addErrorLine(String line) {
    setDocs(line, new Color(255, 0, 0), false, Config.frameFontSize);
  }

  public void addLine(String line) {
    if (line == null || line.trim().length() == 0) {
      return;
    }
    setDocs(" " + line, Color.GREEN, false, Config.frameFontSize);
  }

  private void read() {
    while (true) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      synchronized (docQueue) {
        while (!docQueue.isEmpty()) {
          try {
            DocType doc = docQueue.removeFirst();
            addDocs(doc);
          } catch (NoSuchElementException e) {
            e.printStackTrace();
            docQueue = new ArrayDeque<>();
            docQueue.clear();
            break;
          }
          checkConsole();
        }
      }
    }
  }

  public void exitedAfterSignal() {
    // TODO Auto-generated method stub
    btnSlowShutdown.setVisible(false);
    btnForceShutdown.setVisible(false);
    btnCloseView.setVisible(true);
    exitedAfterSignal = true;
  }
}
