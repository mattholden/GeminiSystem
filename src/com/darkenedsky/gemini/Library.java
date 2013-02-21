package com.darkenedsky.gemini;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.darkenedsky.gemini.exception.InvalidLibrarySectionException;

public class Library implements MessageSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7355704229580479818L;

	private ConcurrentHashMap<String, LibrarySection> sections = 
		new ConcurrentHashMap<String, LibrarySection>();
	
	public LibrarySection getSection(String name) { 
		LibrarySection sec = sections.get(name);
		if (sec == null)
			throw new InvalidLibrarySectionException(name);
		return sec;
	}

	public LibrarySection addSection(String name) {
		LibrarySection ls = new LibrarySection();
		sections.put(name, ls);
		return ls;
	}
	
	public LibrarySection addSection(String name, GameObject[] stuff) {		
		return sections.put(name, new LibrarySection(stuff));
	}
	
	public void merge(Library other) { 
		for (Map.Entry<String, LibrarySection> e : other.sections.entrySet()) {
			
			LibrarySection mine = sections.get(e.getKey());
			if (mine == null) { 
				sections.put(e.getKey(), e.getValue());
			}
			else { 
				mine.merge(e.getValue());
			}
		}
	}

	@Override
	public Message serialize(Player player) {
		Message m = new Message();
		for (String sect : sections.keySet()) { 
			m.addList(sect);
			LibrarySection section = getSection(sect);
			for (GameObject go : section.getAll()) { 
				m.addToList(sect, go, player);
			}
		}
		return m;
	}
}


