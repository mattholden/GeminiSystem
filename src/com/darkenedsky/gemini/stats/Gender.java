package com.darkenedsky.gemini.stats;

public interface Gender {
	public static final int GENDER_MALE = 1, GENDER_FEMALE = -1, GENDER_UNDISCLOSED = 0;
	public static final String GENDER_FIELD = "gender";
	
	public int getGender(); 
	
	public void setGender(int gend);
	
}