/**
 * 
 */
package com.darkenedsky.gemini.tools;

import java.io.*;

/**
 * @author Jaeden
 *
 */
public abstract class FileTools {


	public static byte[] getFileBytes(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        	throw new IOException("File is greater than 4GB!");
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
	

	public static void fileCopy(String file, String newFile) throws IOException { 
		File in = new File (file);		
		File out = new File(newFile);
		InputStream inRead = new BufferedInputStream(new FileInputStream(in));
		OutputStream outWrite = new BufferedOutputStream(new FileOutputStream(out));
		byte[] buffer = new byte[(int)in.length()];	
		int read = inRead.read(buffer);
		if (read != in.length())
			throw new IOException("File not completely read.");
		
		inRead.close();		
		outWrite.write(buffer);		
		outWrite.close();
		
	}
	
	public static boolean fileMove(String file, String newFile) throws IOException { 
		fileCopy(file, newFile);
		
		// need to let the copy finish before we can delete
		return new File(file).delete();
		
	}
	
	public static void deleteDirectory(File dir) { 		
		for (File f : dir.listFiles())
			f.delete();
		dir.delete();
	}
	
}
