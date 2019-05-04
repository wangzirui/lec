package model;

import java.util.ArrayList;
import java.util.List;

public class LSC {
	public String mode;
	public List<Instance> instances;
	public List<Subchart> subcharts;
	public List<Condition> conditions;
	
	public LSC() {
		instances = new ArrayList<Instance>();
		subcharts = new ArrayList<Subchart>();
		conditions = new ArrayList<Condition>();
	}
	
	public Instance addAndGetInstance(Instance e) {
		if (!instances.contains(e))
			instances.add(e);
		else e = instances.get(instances.indexOf(e));
		return e;
	}
}
