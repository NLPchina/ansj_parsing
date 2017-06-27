package org.nlpcn.parsing.domain;

import org.nlpcn.commons.lang.util.StringUtil;

import java.util.List;

/**
 * Created by Ansj on 19/05/2017.
 */
public class RuleGroup {

	private List<RuleGroup> subs = null;

	private String name;

	private String nature;

	private double weight = 0;

	private int step;

	public RuleGroup(String name, String nature) {
		this.name = name;
		this.nature = nature;
		this.step = 1;
	}

	public RuleGroup(String nature, double weight) {
		this.nature = nature;
		this.weight = weight;
	}

	public List<RuleGroup> getSubs() {
		return subs;
	}

	public void setSubs(List<RuleGroup> subs) {
		for (RuleGroup sub : subs) {
			step += sub.step;
		}
		this.subs = subs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getStep() {
		return step;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(nature + " ");
		if (name != null) {
			sb.append(name);
		}

		if (subs != null) {
			sb.append(StringUtil.joiner(subs, " "));
		}

		sb.append(")");

		return sb.toString();
	}
}
