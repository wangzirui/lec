package model;

import java.util.ArrayList;
import java.util.List;

public class Instance {
	public String name;
	public List<Location> locations;
	
	public Instance(String name) {
		this.name = name;
		locations = new ArrayList<Location>();
		locations.add(new Location(-1, "hot"));
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof Instance))
			return false;
		Instance obj = (Instance)o;
		return obj.name.equals(name);
	}
}
