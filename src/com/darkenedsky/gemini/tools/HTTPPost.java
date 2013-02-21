package com.darkenedsky.gemini.tools;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class HTTPPost {


	  final private String url;
	  private StringBuffer toPost = new StringBuffer("");

	  public HTTPPost(String url) {
	    this.url = url;	 
	  }

	  public void addValue(String key, String value) throws IOException {
		if (toPost.length() > 0)
			toPost.append("&");
		
	    toPost.append(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8"));
	  }

	  public byte[] post() throws MalformedURLException, IOException {
	   
	    URLConnection conn = new URL(url).openConnection();
	    conn.setReadTimeout(60000);
	    conn.setConnectTimeout(60000);
	    conn.setDoOutput(true);
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    wr.write(toPost.toString());
	    wr.flush();

	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String line;
	    StringBuffer in = new StringBuffer();
	    while ( (line = rd.readLine()) != null ) {
	      in.append(line);
	      in.append("\n");
	    }

	    wr.close();
	    rd.close();
	    return in.toString().getBytes();
	  }
	}

