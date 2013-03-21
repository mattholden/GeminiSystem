package com.darkenedsky.gemini.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import com.darkenedsky.gemini.Message;

public class TestResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4871242950000016955L; 
	
	
	private long startMillis, endMillis, durationMillis;
	private String stepDescription;
	private StringBuffer log = new StringBuffer();
	private boolean failure = false;
	private Long gameID = null;
	
	public TestResult(String stepDesc) { 
		stepDescription = stepDesc;
		startMillis = System.currentTimeMillis();
		log.append("Test case [" + stepDescription + "] begun at " + startMillis + "\n<ol>");
	}
	
	public void endProfile() { 
		endMillis = System.currentTimeMillis();
		durationMillis = endMillis - startMillis;
		log.append("</ol>Test case ended at " + endMillis + " [Duration: " + durationMillis + " milliseconds]<br><br>");
	}
	
	public void addException(Throwable t, Boolean expected) { 
		log("Encountered exception: " + t.getMessage(), expected);
		if (expected != null && expected == false) 
			failure = true;
	}
		
	public void log(String text, Boolean expected) {
		String expect = (expected == null) ? "" : ((expected) ? "<font color='#00FF00'>&nbsp;[EXPECTED]</font>" :"<font color='#FF0000'>&nbsp;[UNEXPECTED]</font>"); 
		log.append("/t<li>" + text + expect + "</li>\n");
		if (expected != null && expected == false) 
			failure = true;
	}
	
	public void assertException(Message m, Integer value) { 
		String key = "exception";
		log.append("\t<li>Asserting exception code is equal to [" + value + "]... ");
		
		if (m == null) {
			log.append("<font color='#FF0000'>[UNEXPECTED: NULL MESSAGE]</FONT></LI>\n");
			failure = true;
		}
		else if (m.getString(key) == null && value != null) {
			log.append("<font color='#FF0000'>[UNEXPECTED: NULL]</FONT></LI>\n");
			failure = true;
		}		
		else if (!m.getString(key).equals(value)) {
			log.append("<font color='#FF0000'>[UNEXPECTED: '" + m.getString(key) + "' / EXPECTED: '" + value + "']</FONT></LI>\n");
			failure = true;
		}
		else
			log.append("<font color='00FF00'>[CONFIRMED: '" + value + "']</font></li>\n");			
	}
	
	public void assertString(Message m, String key, String value) { 
		log.append("\t<li>Asserting field [" + key + "] is equal to [" + value + "]... ");
		
		if (m == null) {
			log.append("<font color='#FF0000'>[UNEXPECTED: NULL MESSAGE]</FONT></LI>\n");
			failure = true;
		}
		else if (m.getString(key) == null && value != null) {
			log.append("<font color='#FF0000'>[UNEXPECTED: NULL]</FONT></LI>\n");
			failure = true;
		}		
		else if (!m.getString(key).equals(value)) {
			log.append("<font color='#FF0000'>[UNEXPECTED: '" + m.getString(key) + "' / EXPECTED: '" + value + "']</FONT></LI>\n");
			failure = true;
		}
		else
			log.append("<font color='00FF00'>[CONFIRMED: '" + value + "']</font></li>\n");		
	}
	public void assertBoolean(Message m, String key, Boolean value) { 
		assertString(m, key, (value == null) ? null : value.toString());
	}
	public void assertInteger(Message m, String key, Integer value) { 
		assertString(m, key, (value == null) ? null : value.toString());
	}
	public void assertLong(Message m, String key, Long value) { 
		assertString(m, key, (value == null) ? null : value.toString());
	}
	
	public boolean isFailure() { return failure; }
	public long getExecutionTimeMillis() { return durationMillis; }

	public void writeLog(String file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(file), true));
		writer.write("<hr width='80%' style='align: center;'>"+ log.toString());
		writer.flush();
		writer.close();
	}

	public Long getGameID() {
		return gameID;
	}

	public void setGameID(Long gameID) {
		this.gameID = gameID;
	}
	

}
