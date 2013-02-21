/**
 * 
 */
package com.darkenedsky.gemini.tools;
import java.util.*;


/**
 * Contains a handful of static methods for complex string manipulation.
 * 
 * @author Jaeden
 *
 */
public abstract class StringTools {
	

	/** Capitalize the first letter of a word 
     *  @param word word to capitalize
     *  @return word with first letter capitalized and the rest not */
     public static String toSentenceCase(String word)
     {
         return new String(Character.toUpperCase(word.charAt(0)) + word.toLowerCase().substring(1));            
     }

    /** Words never to title case */
 	private static ArrayList<String> noTitleCase = new ArrayList<String>();
 	
 	/** Words never to title case unless they are the first word in the string */
 	private static ArrayList<String> noTitleCaseUnlessFirst = new ArrayList<String>();
 	
 	/**Load the exception words for title-casing  */
 	private static void loadNoTitleCase() { 
 		noTitleCaseUnlessFirst.add("of");
 		
 	//	noTitleCase.add("a");
 		noTitleCaseUnlessFirst.add("the");
 		noTitleCaseUnlessFirst.add("an");
 		noTitleCaseUnlessFirst.add("in");
 		noTitleCase.add("and");
 		noTitleCase.add("or");
 		noTitleCase.add("de");
 		noTitleCase.add("di");
 		noTitleCase.add("le");
 		noTitleCase.add("la");
 		noTitleCase.add("du");
 		noTitleCase.add("el");
 	}
 	
 	/** Cull out everything but the digits in a string, for numeric parsing
 	 * 
 	 * @param number the original string
 	 * @return a string containing only the digits from the original string, in order
 	 */
 	public static String toDigits(String number) { 
 		if (number == null) 
 			return "";
 		
 		String digits = new String();
 		for (int i = 0; i < number.length(); i++)
 			if (Character.isDigit(number.charAt(i)))
 				digits = digits + number.charAt(i);
 		
 		return digits;
 	}
 	
 	/** Convert a string into title case, respecting "ignored words" like articles.
 	 * 
 	 * @param original the original string to modify
 	 * @return the title-cased string
 	 */
 	public static String toTitleCase(String original) { 
 		
 		if (noTitleCase.size() == 0)
 			loadNoTitleCase();
 		
 		String title = new String();
 		String[] tokens = original.split("[ \\&/\\?$\\+\\(\\);:\\.-]");
 		
 		int position = 0;
 		
 		for (String token : tokens) { 
 			
 			// add the token
 			if (position != 0 && position < original.length())
 				title += original.charAt(position - 1);
 			 
 			title += titleToken(token, (position == 0));
 			position += token.length() + 1;
 			
 		}
 		
 		if (position - 1< original.length() && tokens.length > 1) {
 			title += original.substring(position -1);
 		}
 		return title;
 	}
 	
 	/** Title-case a token
 	 * 
 	 * @param token a token to title-case
 	 * @param isFirst an indicator for whether this is the first token in the string
 	 * @return a title-cased token
 	 */
 	private static String titleToken(String token, boolean isFirst) { 
 		
 		if (token == null || token.length() == 0)
 			return "";
 		
 		// look for articles not to capitalize
 		if (noTitleCase.contains(token.toLowerCase()))
 				return token.toLowerCase();
// 		 look for articles not to capitalize
 		if (noTitleCaseUnlessFirst.contains(token.toLowerCase()) && isFirst ==false)
 				return token.toLowerCase();
 		
 		// title case
 		return new String(Character.toTitleCase(token.charAt(0)) + token.substring(1).toLowerCase());
 		
 	}
 	
	/**
	 * Replaces spaces with underscores for purposes of XML-izing strings.
	 * 
	 * @param strSource string with spaces
	 * @return string with underscores
	 */
	public static String replaceSpaces(String strSource) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < strSource.length(); i++) {
			if (strSource.charAt(i) == ' ') {
				buff.append('_');
			} else {
				buff.append(strSource.charAt(i));
			}
		}
		return buff.toString();
	}

	/**
	 * Replaces underscores with spaces for purposes of de-XML-izing strings.
	 * 
	 * @param strSource string with underscores
	 * @return string with spaces
	 */
	public static String replaceUnderscores(String strSource) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < strSource.length(); i++) {
			if (strSource.charAt(i) == '_') {
				buff.append(' ');
			} else {
				buff.append(strSource.charAt(i));
			}
		}
		return buff.toString();
	}

	
	/**
	 * Don't show a constructor for this class
	 */
	private StringTools() {
		//
	}
	
	
	 /** Do a replace, since replaceAll in a string needs a regex 
     *  @param inScript script to search
     *  @param replace what to replace
     *  @param replaceWith what to replace it with 
     *  @return the new string */
    public static String stringReplace(String inScript, String replace, String replaceWith) { 
        
         if (inScript !=null)
        {
        final int len = replace.length();
        StringBuffer sb = new StringBuffer();
        int found = -1;
        int start = 0;

        while( (found = inScript.indexOf(replace, start) ) != -1) {
            sb.append(inScript.substring(start, found));
            sb.append(replaceWith);
            
            start = found + len;
        }

        sb.append(inScript.substring(start));
        return sb.toString();
        }
         return inScript;
    }
    
  
}
