package com.darkenedsky.gemini.exception;

public class DuplicateDefinitionIDException extends GeminiException implements ExceptionCodes { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3306624724069971438L;

	public DuplicateDefinitionIDException(String nu, String existing) { 
		super(DUPLICATE_DEFINITION_ID, "Two object definitions were found with the same Defintion ID: " + nu + " AND " + existing);
		details.put("existing", existing);
		details.put("new", nu);
	}

}
