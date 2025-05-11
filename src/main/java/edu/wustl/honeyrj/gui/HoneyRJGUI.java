package edu.wustl.honeyrj.gui;

import edu.wustl.honeyrj.honeyrj.*;
import edu.wustl.honeyrj.lowinteraction.*;
import edu.wustl.honeyrj.analysis.LogFileAnalyzer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class HoneyRJGUI extends JFrame implements ActionListener {

	private JButton startButton;
	private JButton stopButton;
	private JButton pauseResumeButton;
	private JButton dashboardButton;
	private static final long serialVersionUID = -2936644671086250830L;
	private LIModuleContainer _moduleContainer;
	private HoneyRJ _honeyRJ;

	public HoneyRJGUI(HoneyRJ rj) {
		super("HoneyRJ");
		_honeyRJ = rj;
		createTopPane();
		_moduleContainer = new LIModuleContainer();
		JScrollPane moduleScroll = new JScrollPane(_moduleContainer);
		getContentPane().add(moduleScroll, BorderLayout.CENTER);
		pack();
		setVisible(true);
	}

	public void AddModule(LIModule toAdd) {
		_honeyRJ.RegisterService(toAdd);
		_moduleContainer.AddModule(new LIModuleGUI(toAdd));
		pack();
	}

	private void createTopPane() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1000, 600);
		setPreferredSize(new Dimension(1100, 600));

		// 1. État du service
		JPanel legendPanel = new JPanel(new GridLayout(2, 2, 5, 5));
		legendPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"État du service",
				TitledBorder.CENTER,
				TitledBorder.TOP
		));
		legendPanel.add(createStatusLabel("Started", Color.GREEN));
		legendPanel.add(createStatusLabel("Stopped", new Color(255, 165, 0)));
		legendPanel.add(createStatusLabel("Paused", Color.YELLOW));
		legendPanel.add(createStatusLabel("Error", Color.RED));

		// 2. Contrôle
		JPanel controlPanel = new JPanel(new GridLayout(3, 1, 5, 5));
		controlPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Contrôle",
				TitledBorder.CENTER,
				TitledBorder.TOP
		));

		startButton = new JButton("Démarrer tout");
		stopButton = new JButton("Arrêter tout");
		pauseResumeButton = new JButton("Pause");

		startButton.setActionCommand("startall");
		stopButton.setActionCommand("stopall");
		pauseResumeButton.setActionCommand("pauseall");

		for (JButton btn : new JButton[]{startButton, stopButton, pauseResumeButton}) {
			btn.addActionListener(this);
			controlPanel.add(btn);
		}

		stopButton.setEnabled(false);
		pauseResumeButton.setEnabled(false);

		// 3. Analyse des journaux
		JPanel analysisPanel = new JPanel(new GridLayout(3, 1, 5, 5));
		analysisPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Analyse des journaux",
				TitledBorder.CENTER,
				TitledBorder.TOP
		));

		JButton analyzeButton = new JButton("Analyser un dossier");
		analyzeButton.setActionCommand("analyze");

		JButton analyzeAllButton = new JButton("Analyser toutes les sessions");
		analyzeAllButton.setActionCommand("analyze_all");

		dashboardButton = new JButton("Afficher les statistiques");
		dashboardButton.setActionCommand("showdashboard");

		for (JButton btn : new JButton[]{dashboardButton, analyzeAllButton, analyzeButton }) {
			btn.addActionListener(this);
			analysisPanel.add(btn);
		}

		// تجميع جميع اللوحات في الأعلى
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
		topPanel.add(legendPanel);
		topPanel.add(controlPanel);
		topPanel.add(analysisPanel);

		getContentPane().add(topPanel, BorderLayout.PAGE_START);
	}

	private JLabel createStatusLabel(String text, Color color) {
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setOpaque(true);
		label.setBackground(color);
		label.setForeground(Color.DARK_GRAY);
		label.setFont(new Font("Arial", Font.BOLD, 14));
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		label.setPreferredSize(new Dimension(80, 40));
		return label;
	}

	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "startall":
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				pauseResumeButton.setEnabled(true);
				_moduleContainer.startAll();
				break;

			case "stopall":
				startButton.setEnabled(true);
				stopButton.setEnabled(false);
				pauseResumeButton.setEnabled(false);
				_moduleContainer.stopAll();
				break;

			case "pauseall":
				pauseResumeButton.setText("Reprendre");
				pauseResumeButton.setActionCommand("resumeall");
				pauseResumeButton.setToolTipText("Reprendre tous les services");
				_moduleContainer.pauseAll();
				break;

			case "resumeall":
				pauseResumeButton.setText("Pause");
				pauseResumeButton.setActionCommand("pauseall");
				pauseResumeButton.setToolTipText("Mettre en pause tous les services");
				_moduleContainer.resumeAll();
				break;

			case "analyze":
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File(System.getProperty("user.home") + File.separator + "HoneyRJLogs"));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File selectedDir = chooser.getSelectedFile();
					try {
						String summary = LogFileAnalyzer.analyze(selectedDir.getAbsolutePath());
						JOptionPane.showMessageDialog(this, summary, "Résultat de l’analyse", JOptionPane.INFORMATION_MESSAGE);
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(this, "Échec de l’analyse : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
					}
				}
				break;

			case "analyze_all":
				File baseDir = new File(System.getProperty("user.home") + File.separator + "HoneyRJLogs");
				try {
					String summary = LogFileAnalyzer.analyzeAll(baseDir);
					JOptionPane.showMessageDialog(this, summary, "Analyse complète", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this, "Échec de l’analyse complète : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
				}
				break;

			case "showdashboard":
				new HoneyRJDashboard();
				break;
		}
	}
}
