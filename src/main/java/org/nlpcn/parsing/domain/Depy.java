package org.nlpcn.parsing.domain;

/**
 * Created by Ansj on 14/06/2017.
 */
public class Depy {

	public static final String ROOT_TAG = "-1_ROOT" ;

	private String tag ;

	private double score ;

	private  int index ;

	private int toIndex ;

	public Depy(String tag ,int index , int toIndex) {
		this.tag = tag;
		this.index = index ;
		this.toIndex = toIndex ;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public int getToIndex() {
		return toIndex;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "Depy{" +
				"tag='" + tag + '\'' +
				"toIndex='" + toIndex + '\'' +
				", score=" + score +
				'}';
	}
}
