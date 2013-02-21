/**
 * 
 */
package com.darkenedsky.gemini.tools;

/**
 * Convenience methods and tools for dealing with classes, reflection, and metaprogramming.
 * 
 * @author Jaeden
 *
 */
public class ReflectionTools {

	  /**
	 * Convenience method because Matt can never remember which order the classes
	 * in java.lang.Class.isAssignableFrom go :)
	 * 
	 * @param classBase class we want to check for superclass-ness
	 * @param classChild class we think might be a subclass of classBase
	 * @return true if classChild is a subclass/subinterface of classBase
	 */
	public static boolean isSubclass(Class<?> classBase, Class<?> classChild) {
		return classBase.isAssignableFrom(classChild);
	}

	
}
