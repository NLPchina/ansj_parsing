package org.nlpcn.parsing.domain;

import org.nlpcn.commons.lang.util.tuples.KeyValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ansj on 28/05/2017.
 */
public class Feature implements Serializable {
	private String name;

	private List<KeyValue<Integer, Integer>> xList = new ArrayList<>();

	public Feature(String temp) {
		String[] split = temp.split(":");
		this.name = split[0];
		String[] xArr = split[1].split("/");
		for (int i = 0; i < xArr.length; i++) {
			String substring = xArr[i].substring(3, xArr[i].length() - 1);
			String[] ints = substring.split(",");
			xList.add(KeyValue.with(Integer.parseInt(ints[0]), Integer.parseInt(ints[1])));
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<KeyValue<Integer, Integer>> getxList() {
		return xList;
	}

	public void setxList(List<KeyValue<Integer, Integer>> xList) {
		this.xList = xList;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(":");
		for (KeyValue<Integer, Integer> x : xList) {
			sb.append("%[");
			sb.append(x.getKey());
			sb.append(",");
			sb.append(x.getValue());
			sb.append("]/");
		}
		String s = sb.toString();
		return s.substring(0, s.length() - 1);
	}
}
