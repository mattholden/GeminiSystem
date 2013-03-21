package com.darkenedsky.gemini.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class TestRunner {

	private String logFile;
	private boolean failFast = false;
	private Long gameID;
	private Vector<TestCase> cases = new Vector<TestCase>();
	private Vector<TestSession> sessions = new Vector<TestSession>();
	private String url;
	
	public TestRunner(String log, String url, boolean fail) { 
		logFile = log;
		failFast = fail;
		this.url = url;
	}
	
	public void addCase(TestCase tCase) { cases.add(tCase); }
	
	public void addSession(String user, String pw) throws Exception { 
		sessions.add(new TestSession(user, pw, url));		
	}
	
	public void execute() throws IOException { 
		
		int pass = 0, fail = 0;
		long start = System.currentTimeMillis();
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(logFile), false));
			writer.write("<html><head><title>Gemini System Test Case Results</title>\n");
			
			// TODO: add some auto-refresh JS so people can see the results live in a browser as they come in?
			
			writer.write("</head>\n<body style='font-face: Arial, Helvetica, Verdana, sans-serif; font-size: 12px;'>\n");
			writer.flush();
			writer.close();
		}
		catch (IOException x) { 
			x.printStackTrace();
			return;
		}
		
		for (TestCase tc : cases) {
			TestResult result = tc.runTest(sessions, gameID);
			try { 
				result.writeLog(logFile);
			}
			catch (IOException x) { x.printStackTrace(); }
			if (result.getGameID() != null && gameID == null)
				gameID = result.getGameID();
			
			if (result.isFailure()) { 
				fail++;
				if (failFast) break;
			}
			else pass++;
		}
		
		long end = System.currentTimeMillis();
		long duration = end - start;
		
	
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(logFile), false));
			writer.write("\n\n<br><br><hr><b>Final Results:<br>");
			writer.write("<font color='#00FF00'>PASSED: " + pass + " of " + (pass+fail) + " (" + ((pass*1.0)/(pass+fail)) + "%)<br></font>\n");
			writer.write("<font color='#FF0000'>FAILED: " + fail + " of " + (pass+fail) + " (" + ((fail*1.0)/(pass+fail)) + "%)<br></font>\n");
			writer.write("<font color='#0000FF'>SKIPPED: " + (cases.size()-(pass+fail)) + " of " + cases.size() + " (" + (((pass+fail)*1.0)/cases.size()) + "%)<br></font>\n");
			writer.write("Test run completed in " + (duration / 1000.0) + " seconds.<br><br>\n");
			writer.write("</b></body></html>");
			writer.flush();
			writer.close();
		}
		catch (IOException x) { 
			x.printStackTrace();
			return;
		}
	
	}
	
}
