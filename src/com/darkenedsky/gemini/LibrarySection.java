package com.darkenedsky.gemini;

import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.darkenedsky.gemini.exception.DuplicateDefinitionIDException;
import com.darkenedsky.gemini.exception.InvalidObjectException;

public class LibrarySection  {
	
	private ConcurrentHashMap<Integer, GameObject> objects = new ConcurrentHashMap<Integer, GameObject>();
	
	public Vector<GameObject> getAll() { 
		Vector<GameObject> list = new Vector<GameObject>();
		for (GameObject thing : objects.values()) { 
			if (thing != null)
				list.add(thing);
		}
		Collections.sort(list);
		return list;
	}

	public void add(GameObject[] objs) { 
		for (GameObject thing : objs)
			objects.put(thing.getDefinitionID(), thing);
	}
	
	public LibrarySection() { /* deliberately blank */ }
	
	public LibrarySection(GameObject[] stuff) { 
		for (GameObject thing : stuff) { 
			if (thing != null)
				objects.put(thing.getDefinitionID(), thing);
		}
	}
	
	public GameObject get(int id) { 
		GameObject go = objects.get(id);
		if (go == null) 
			throw new InvalidObjectException(id);
		return go;
	}
	
	public void merge(LibrarySection other) { 
		objects.putAll(other.objects);
	}
	
	public void add(GameObject o) { 
		if (objects.get(o.getDefinitionID()) != null) { 
			throw new DuplicateDefinitionIDException(o.toString(), objects.get(o.getDefinitionID()).toString());
		}
		objects.put(o.getDefinitionID(), o);
	}
	
}
