package com.darkenedsky.gemini.test;

import java.util.Vector;

public abstract class TestCase {
	
	private String description;
	
	public TestCase(String desc) { 
		description = desc;
	}
	
	protected TestSession findSession(long playerid, Vector<TestSession> ts) {
		for (TestSession s : ts)
			if (s.getPlayerID() == playerid) return s;
		
		return null;		
	}
	
	public TestResult runTest(Vector<TestSession> sessions, Long gameID) { 
		TestResult result = new TestResult(description);
		execute(result, sessions, gameID);
		result.endProfile();
		return result;
	}
	
	protected abstract void execute(TestResult result, Vector<TestSession> sessions, Long gameID);
	
}
