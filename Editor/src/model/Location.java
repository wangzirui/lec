package model;

public class Location {
	public int pos;
	public String temp, type;
	
	public Location(int p, String t) {
		pos = p;
		temp = t;
	}
	
	public Location(int p, String t, String y) {
		pos = p;
		temp = t;
		type = y;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof Location))
			return false;
		Location that = (Location)o;
		return pos == that.pos && temp.equals(that.temp);
	}
}
