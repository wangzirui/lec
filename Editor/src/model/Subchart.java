package model;

import java.util.HashSet;
import java.util.Set;

import control.Engine;

public class Subchart {
	final int defaultOffset = 50;
	public int pos, left, right, end, leftOff, rightOff;
	public Set<Instance> instances;
	
	public Subchart() {
		left = -1;
		right = -1;
		leftOff = defaultOffset;
		rightOff = defaultOffset;
		instances = new HashSet<Instance>();
		end = -1;
	}

	public void adjustWidth(int pos) {
		if (left == -1 || pos < left) {
			if (left >= 0)
				leftOff = Math.max(Engine.x(pos) - (Engine.x(left) - leftOff), defaultOffset);
			left = pos;
			
		}
		if (right == -1 || pos > right) {
			rightOff = Math.max(Engine.x(right) + rightOff - Engine.x(pos), defaultOffset);
			right = pos;
		}
	}
}
