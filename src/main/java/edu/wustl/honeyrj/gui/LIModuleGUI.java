package edu.wustl.honeyrj.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.*;

import edu.wustl.honeyrj.lowinteraction.LIModule;

public class LIModuleGUI extends JPanel implements ActionListener {

	private static final long serialVersionUID = -6195602253907757865L;
	private LIModule _liModule;
	private JLabel _numConnections;
	private JButton _start;
	private JButton _stop;
	private JButton _pauseResume;

	public LIModuleGUI(LIModule liModule) {

		this.setBackground(new Color(255, 165, 0));
		_liModule = liModule;
		_liModule.registerWithGUI(this);

		setPreferredSize(new Dimension(900, 75));
		setMaximumSize(new Dimension(900, 75));
		setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
		setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

		// Informations sur le module
		JLabel _mainInfo = new JLabel("Module : " + _liModule.toString() + " (Port " + _liModule.getPort() + ")");
		_mainInfo.setFont(new Font("Arial", Font.BOLD, 14));
		_mainInfo.setPreferredSize(new Dimension(300, 25));
		_mainInfo.setHorizontalAlignment(SwingConstants.LEFT);
		_mainInfo.setToolTipText("Module : " + _liModule.toString() + " (Port " + _liModule.getPort() + ")");



		// Nombre de connexions
		_numConnections = new JLabel("Connexions suspectes : 0");
		_numConnections.setFont(new Font("Arial", Font.PLAIN, 13));
		_numConnections.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		_numConnections.setPreferredSize(new Dimension(180, 25));
		_numConnections.setHorizontalAlignment(SwingConstants.CENTER);

		// Boutons
		_start = new JButton("Démarrer");
		_start.setActionCommand("start");
		_start.setToolTipText("Démarrer ce module");
		_start.addActionListener(this);

		_stop = new JButton("Arrêter");
		_stop.setActionCommand("stop");
		_stop.setToolTipText("Arrêter ce module");
		_stop.setEnabled(false);
		_stop.addActionListener(this);

		_pauseResume = new JButton("Pause");
		_pauseResume.setActionCommand("pause");
		_pauseResume.setToolTipText("Mettre en pause l’écoute");
		_pauseResume.setEnabled(false);
		_pauseResume.addActionListener(this);

		// Ajouter tous les composants
		add(_start);
		add(_stop);
		add(_mainInfo);
		add(_numConnections);
		add(_pauseResume);
	}

	public int getPort() {
		return _liModule.getPort();
	}

	public void startInteractionModule() {
		try {
			_liModule.startInteractionModule();
			setBackground(Color.GREEN);
		} catch (IOException e) {
			// error
			setBackground(Color.RED);
		}
		_stop.setEnabled(true);
		_start.setEnabled(false);
		_pauseResume.setEnabled(true);
	}

	public void pauseListeningForConnections() {
		_liModule.pauseListeningForConnections();
		setBackground(Color.YELLOW);
		_pauseResume.setText("Reprendre");
		_pauseResume.setActionCommand("resume");
		_pauseResume.setToolTipText("Reprendre l’écoute des connexions");
	}

	public void resumeListeningForConnections() {
		if (_liModule.resumeListeningForConnections()) {
			setBackground(Color.GREEN);
		} else {
			// error
			setBackground(Color.RED);
		}
		_pauseResume.setText("Pause");
		_pauseResume.setActionCommand("pause");
		_pauseResume.setToolTipText("Mettre en pause l’écoute des connexions");
	}

	public void stopInteractionModule() {
		_liModule.stopInteractionModule();
		setBackground( new Color(255, 165, 0));
		_stop.setEnabled(false);
		_start.setEnabled(true);
		_pauseResume.setEnabled(false);
	}

	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "start":
				startInteractionModule();
				break;
			case "stop":
				stopInteractionModule();
				break;
			case "pause":
				pauseListeningForConnections();
				break;
			case "resume":
				resumeListeningForConnections();
				break;
		}
	}

	public void setNumberConnections(int newNum) {
		_numConnections.setText("Connexions suspectes : " + newNum);
	}
}
