package com.darkenedsky.gemini.service;
import java.io.IOException;
import java.util.Vector;
import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.GameCharacter;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.service.GeminiService;
import com.darkenedsky.gemini.tools.XMLTools;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;

public abstract class GeminiServlet<TChar extends GameCharacter, TPlay extends Player, TGame extends Game<TChar, TPlay>> extends HttpServlet implements ActionList {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5582135845513272471L;
	
	protected GeminiService<TChar, TPlay, TGame> service;
	
	@Override
	public abstract void init();
	
	@Override
	public void destroy() {
		try { 
			System.out.println("Shutting down Gemini Servlet...");
			service.shutdown();
		}
		catch (Exception x) { 
			x.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("GET request received.");
		
		//if (service.getSettings().getBoolean("allow-get-requests")) {
			doPost(req, resp);
		//}
		//else { 
		//	throw new ServletException("HTTP GET requests are not accepted by this server.");
		//}
		
	}
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
		try {
			
			System.out.println("POST request received");
			
			//if (service.getSettings().getBoolean("require-https") && !req.isSecure())
			//	throw new ServletException("Insecure requests are not accepted by this server.");
			
			String m = req.getParameter("xml");
			String j = req.getParameter("json");
			if (j != null) { 
				System.out.println("===================================================");
				System.out.println("RECEIVED JSON:");
				System.out.println(j);
				Message msg = new Message(j);
				msg.put(Message.SESSION_IPADDRESS, req.getRemoteAddr());
				Vector<Message> replies = (service.processMessage(msg));
				StringBuffer sb = new StringBuffer("[\n");
				for (int i = 0; i < replies.size(); i++) { 
					sb.append(replies.get(i).toJSONString());
					if (i != replies.size()-1) { 
						sb.append(",\n");
					}
				}
				sb.append("\n]");
				
				String retstr = sb.toString();
				System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\nRESPONDED:");			
				System.out.println(retstr);
				resp.setContentType("text/json");
				resp.getWriter().println(retstr);
			}
			
			else if (m != null) { 
			
				System.out.println("===================================================");
				System.out.println("RECEIVED XML:");
				System.out.println(m);
				Element e = XMLTools.stringToXML(m);
				e.addContent(XMLTools.xml(Message.SESSION_IPADDRESS, req.getRemoteAddr()));
				Message msg = new Message(e);				
				Vector<Message> replies = (service.processMessage(msg));
				Element reply = new Element("messages");
				for (Message mx : replies) { 
					reply.addContent(mx.toXML("message"));
				}
				String retstr = XMLTools.xmlToString(reply);
				System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\nRESPONDED:");			
				System.out.println(retstr);
				resp.setContentType("text/xml");
				resp.getWriter().println(retstr);
			}
			
		}
		catch (IOException x) { 
			throw x;
		}
		catch (ServletException x) { 
			throw x; 
		}
		catch (Exception x) { 
			throw new ServletException(x);
		}		
	}
}
