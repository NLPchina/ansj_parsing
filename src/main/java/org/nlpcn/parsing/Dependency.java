package org.nlpcn.parsing;

import org.ansj.library.DicLibrary;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.util.MapCount;
import org.nlpcn.commons.lang.util.tuples.KeyValue;
import org.nlpcn.parsing.domain.Depy;
import org.nlpcn.parsing.domain.Element;
import org.nlpcn.parsing.domain.Feature;
import org.nlpcn.parsing.util.CrfTxtModel;
import org.nlpcn.parsing.util.StaticValue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ansj on 14/06/2017.
 */
public class Dependency {

	/**
	 * 加载CRF模型,这个必须在最下面
	 */
	private static final CrfTxtModel MODEL = init();

	private static CrfTxtModel init() {
		try (InputStream is = StaticValue.getInputStreamInJar("/dependency.model")) {
			CrfTxtModel model = new CrfTxtModel();
			CrfTxtModel.readAnsjModel(is, model);
			return model;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Element[] parse(String str, Forest... forests) {
		Element[] elements = POSTagging.parse(str, forests);
		compute(elements);
		return elements;
	}

	private static void compute(Element[] elements) {

		List<Depy>[] depys = new List[elements.length];

		int maxLen = Integer.MIN_VALUE;

		for (int i = 0; i < elements.length; i++) {
			MapCount<String> mc = new MapCount<>();
			List<Depy> list = new ArrayList<>();
			Depy depy = new Depy(Depy.ROOT_TAG, i, -1);
			setScore(elements, depy, i);
			list.add(depy);
			for (int j = i - 1; j >= 0; j--) {
				String nature = elements[j].getNature();
				mc.add(nature);
				Double count = mc.get().get(nature);
				depy = new Depy("-" + count.intValue() + "_" + nature, i, j);
				setScore(elements, depy, i);
				list.add(depy);
			}

			mc = new MapCount<>();
			for (int j = i + 1; j < elements.length; j++) {
				String nature = elements[j].getNature();
				mc.add(nature);
				Double count = mc.get().get(nature);
				depy = new Depy("+" + count.intValue() + "_" + nature, i, j);
				setScore(elements, depy, i);
				list.add(depy);
			}
			Collections.sort(list, (d1, d2) -> {
				if (d1.getScore() == d2.getScore()) {
					return 0;
				} else if (d1.getScore() > d2.getScore()) {
					return -1;
				} else {
					return 1;
				}
			});
			depys[i] = list;

			maxLen = Math.max(list.size(), maxLen);
		}

		int rootIndex = 0;

		for (int i = 0; i < maxLen; i++) { //找到root节点
			List<Depy> roots = new ArrayList<>();
			for (int j = 0; j < depys.length; j++) {
				if (i >= depys[j].size()) {
					continue;
				}
				Depy depy = depys[j].get(i);
				if (Depy.ROOT_TAG.equals(depy.getTag())) {
					roots.add(depy);
				}
			}
			if (roots.size() > 0) { //找到最大的root并设置
				Depy root = null;
				if (roots.size() == 1) {
					root = roots.get(0);
				} else {
					root = roots.stream().max((d1, d2) -> {
						if (d1.getScore() == d2.getScore()) {
							return 0;
						} else if (d1.getScore() > d2.getScore()) {
							return -1;
						} else {
							return 1;
						}
					}).get();
				}

				rootIndex = root.getIndex();
				elements[rootIndex].setDepyIndex(-1);
				elements[rootIndex].setDepyName("ROOT");
				break;
			}
		}


		for (int i = 0; i < depys.length; i++) {
			if (elements[i].getDepyIndex() != null) {
				continue;
			}

			Depy from = depys[i].get(0);

			if (from.getToIndex() == rootIndex) { //如果指向root不用验证
				elements[i].setDepyIndex(from.getToIndex());
				edge(elements, i);
				continue;
			}

			int minLen = 0;
			if (from.getToIndex() == -1) {
				minLen = depys[i].size();
			} else {
				minLen = Math.min(depys[i].size(), depys[from.getToIndex()].size());
			}


			int j = 0;
			for (; j < minLen; j++) {
				from = depys[i].get(j);
				if (from.getToIndex() == -1) {
					continue;
				}

				if (from.getToIndex() == rootIndex) { //如果指向root不用验证
					elements[i].setDepyIndex(from.getToIndex());
					edge(elements, i);
					break;
				}

				Depy to = null;
				if (depys[from.getToIndex()].size() <= j) {
					to = depys[from.getToIndex()].get(depys[from.getToIndex()].size() - 1);
				} else {
					to = depys[from.getToIndex()].get(j);
				}


				if (from.getIndex() == to.getToIndex()) { //发生冲突了
					if (from.getScore() > to.getScore()) { //如果本身大于则不用考虑直接覆盖
						elements[i].setDepyIndex(from.getToIndex());
						edge(elements, i);
						break;
					}
				} else {
					elements[i].setDepyIndex(from.getToIndex());
					edge(elements, i);
					break;
				}
			}

			if (elements[i].getDepyIndex() == null) {
				elements[i].setDepyIndex(rootIndex);
				edge(elements, i);
			}
		}
	}

	private static void edge(Element[] elements, int i) {
		Element from = elements[i];
		Element to = elements[elements[i].getDepyIndex()];
		KeyValue<String, Double> dependencyEdge = StaticValue.getDependencyEdge(from.getTerm().getName() + "@" + to.getNature());
		if (dependencyEdge != null) {
			from.setDepyName(dependencyEdge.getKey());
			return;
		}
		dependencyEdge = StaticValue.getDependencyEdge(from.getNature() + "@" + to.getNature());
		if (dependencyEdge != null) {
			from.setDepyName(dependencyEdge.getKey());
			return;
		}
		from.setDepyName("unknow");
	}


	public static void setScore(Element[] elements, Depy depy, int index) {

		List<Feature> template = MODEL.getTemplate();

		double score = 0;

		featureLoop:
		for (Feature feature : MODEL.getTemplate()) {
			List<KeyValue<Integer, Integer>> kvs = feature.getxList();

			StringBuilder sb = new StringBuilder(feature.getName());
			sb.append(":");

			boolean notFirst = false;

			for (KeyValue<Integer, Integer> kv : kvs) {
				if (notFirst) {
					sb.append("/");
				}
				int indexValue = index + kv.getKey();
				if (indexValue < 0 || indexValue >= elements.length) {
					continue featureLoop;
				}

				if (kv.getValue() == 0) {
					sb.append(elements[indexValue].getTerm().getName());
				} else {
					sb.append(elements[indexValue].getNature());
				}
				notFirst = true;
			}
			score += MODEL.get(sb.toString(), depy.getTag());
		}

		depy.setScore(score);
	}

	public static Element[] parse(String str) {
		return parse(str, DicLibrary.get(StaticValue.getDicParsing()));
	}

}
