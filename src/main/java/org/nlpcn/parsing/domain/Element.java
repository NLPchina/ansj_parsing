package org.nlpcn.parsing.domain;

import org.ansj.domain.Term;
import org.nlpcn.parsing.util.StaticValue;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Ansj on 29/03/2017.
 */
public class Element implements Serializable {

	private static final String[] NATURE_STR = new String[]{
			"ad", "as", "ba", "cc", "cd", "cs", "dec", "deg", "der", "dev", "dt", "etc", "fw", "ij", "jj", "lb", "lc", "m", "msp", "nn", "nr", "nt", "od", "on", "p", "pn", "pu", "sb", "sp", "va", "vc", "ve", "vv"
	};

	private Term term;

	private Nature[] natures;

	private String nature;

	private Depy[] depys;

	private Integer depyIndex;

	private String depyName;

	public Element(Term term) {
		this.term = term;
		natures = StaticValue.getWordNatureCount(term.getName());
		if (natures == null) {
			String nature = StaticValue.getNatureMapping(term.getNatureStr());
			if (nature == null) {
				natures = Arrays.stream(NATURE_STR).map(str -> new Nature(str, 100)).toArray(Nature[]::new);
			} else {
				natures = new Nature[]{new Nature(nature, term.natrue().allFrequency)};
			}
		}
	}


	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public Nature[] getNatures() {
		return natures;
	}

	public void setNatures(Nature[] natures) {
		this.natures = natures;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public Integer getDepyIndex() {
		return depyIndex;
	}

	public void setDepyIndex(Integer depyIndex) {
		this.depyIndex = depyIndex;
	}

	public String getDepyName() {
		return depyName;
	}

	public void setDepyName(String depyName) {
		this.depyName = depyName;
	}

	@Override
	public String toString() {
		return term.getName() + "/" + nature + "/" + depyIndex + "/" + depyName;
	}
}
