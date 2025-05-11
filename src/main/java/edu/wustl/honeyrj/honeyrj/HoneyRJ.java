package edu.wustl.honeyrj.honeyrj;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

import edu.wustl.honeyrj.logging.LogFile;
import edu.wustl.honeyrj.lowinteraction.LIModule;

/**
 * HoneyRJ coordinates a number of LIModules, which provide faux services to hackers.
 * HoneyRJ is the final destination for all log files created by the LIModules.
 */
public class HoneyRJ {
	private File _logging_dir;

	public File getLoggingDirectory() {
		return _logging_dir;
	}

	private HashMap<Integer, LIModule> _services;
	private HashMap<Integer, TreeMap<Date, LogFile>> _logs;

	public static final int DEFAULT_TIME_OUT_MS = 120000;
	public static final boolean LOG_TO_CONSOLE = false;
	public static final int TIME_WAIT_CONNECTION = 5000;

	public HoneyRJ() throws HoneyRJException {
		this(getDefaultLogBasePath());
	}

	public HoneyRJ(String logBasePath) throws HoneyRJException {
		_services = new HashMap<>();
		_logs = new HashMap<>();

		// المسار الآمن لإنشاء مجلد السجلات
		_logging_dir = new File(logBasePath + "rj_" + new Date().getTime() + "_log");

		if (!_logging_dir.exists()) {
			if (!_logging_dir.mkdirs()) {
				throw new HoneyRJException("Failed to create log directory: " + _logging_dir.getAbsolutePath());
			}
		}
	}

	// المسار الافتراضي الآمن للسجلات داخل user.home
	private static String getDefaultLogBasePath() {
		return System.getProperty("user.home") + File.separator + "HoneyRJLogs" + File.separator;
	}

	public boolean startPort(int portToStart) {
		if (!_services.containsKey(portToStart)) return false;
		try {
			_services.get(portToStart).startInteractionModule();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean startModule(LIModule moduleToStart) {
		return startPort(moduleToStart.getProtocol().getPort());
	}

	public boolean RegisterService(LIModule moduleToBeConnected) {
		if (_services.containsKey(moduleToBeConnected.getPort())) {
			return false;
		} else {
			_services.put(moduleToBeConnected.getPort(), moduleToBeConnected);
			_logs.put(moduleToBeConnected.getPort(), new TreeMap<>());
			moduleToBeConnected.registerParent(this);
			return true;
		}
	}

	public boolean DeRegisterService(LIModule moduleToBeDisconnected) {
		int port = moduleToBeDisconnected.getPort();
		if (_services.containsKey(port) && moduleToBeDisconnected == _services.get(port)) {
			moduleToBeDisconnected.stopInteractionModule();
			_services.remove(port);
			return true;
		} else {
			return false;
		}
	}

	public boolean PauseNewConnections(LIModule moduleToPause) {
		if (_services.containsKey(moduleToPause.getPort()) && moduleToPause == _services.get(moduleToPause.getPort())) {
			moduleToPause.pauseListeningForConnections();
			return true;
		} else return false;
	}

	public boolean ResumeNewConnections(LIModule moduleToResume) {
		if (_services.containsKey(moduleToResume.getPort()) && moduleToResume == _services.get(moduleToResume.getPort())) {
			moduleToResume.resumeListeningForConnections();
			return true;
		} else return false;
	}

	public void storeLogFiles(LIModule from, LogFile file) {
		_logs.get(from.getPort()).put(file.getStartedDate(), file);
	}

	public void DebugServices() {
		for (Integer i : _services.keySet()) {
			System.out.println("Port " + i + ": " + _services.get(i).toString());
		}
	}
}
