package org.nlpcn.parsing;

import org.nlpcn.commons.lang.tire.ObjTree;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.commons.lang.util.tuples.KeyValue;
import org.nlpcn.commons.lang.viterbi.Node;
import org.nlpcn.commons.lang.viterbi.Viterbi;
import org.nlpcn.commons.lang.viterbi.function.Score;
import org.nlpcn.commons.lang.viterbi.function.Values;
import org.nlpcn.parsing.domain.Element;
import org.nlpcn.parsing.domain.RuleGroup;
import org.nlpcn.parsing.util.StaticValue;

import java.util.*;

/**
 * Created by Ansj on 23/05/2017.
 */
public class Parsing {

	public static void parse(String str) {
		parse(POSTagging.parse(str));
	}

	public static void parse(Element[] elements) {

		List<RuleGroup>[] graph = Arrays.stream(elements).map(e -> {
			RuleGroup ruleGroup = new RuleGroup(e.getTerm().getName(), e.getNature());
			List arrayList = new ArrayList();
			arrayList.add(ruleGroup);
			return arrayList;
		}).toArray(List[]::new);


		int size = graph.length;
		List<RuleGroup> compute = null;
		do {
			if (compute == null) {
				size = graph.length;
			} else {
				size = compute.size();
			}
			compute = mergeTree(graph);

			for (int i = 0; i < graph.length; i++) {
				graph[i] = Collections.emptyList();
			}

			int index = 0;
			for (int i = 0; i < compute.size(); i++) {
				List<RuleGroup> list = new ArrayList<>();
				list.add(compute.get(i));
				graph[index] = list;
				index += (compute.get(i).getStep());
			}

		} while (size != compute.size());


		System.out.println(StringUtil.joiner(compute, "\n"));

	}

	private static List<RuleGroup> mergeTree(List<RuleGroup>[] graph) {

		for (int i = 0; i < graph.length; i++) {
			if (graph[i] == null || graph[i].size() == 0) {
				continue;
			}
			ObjTree<KeyValue<String, Double>> ruleCount = StaticValue.getRuleCount();
			KeyValue<String, Double> kv = null;
			RuleGroup group = null;
			int toIndex = i;

			do {
				group = graph[toIndex].get(0);
				ruleCount = ruleCount.getObjTree(group.getNature());
				if (ruleCount == null) {
					break;
				}
				Map<Object, ObjTree<KeyValue<String, Double>>> branches = ruleCount.getBranches();

				if ((kv = ruleCount.getObj()) != null) {
					RuleGroup ruleGroup = new RuleGroup(kv.getKey(), kv.getValue() / StaticValue.getSingleRuleCount(kv.getKey()));
					ArrayList<RuleGroup> subs = new ArrayList<>();
					for (int k = i; k <= toIndex; k++) {
						subs.add(graph[k].get(0));
					}
					ruleGroup.setSubs(subs);

System.out.println("add\t"+ruleGroup);
					graph[i].add(ruleGroup);
				}

				toIndex = toIndex + group.getStep();

			} while (ruleCount != null && toIndex < graph.length);
		}

		Viterbi<RuleGroup> viterbi = new Viterbi<RuleGroup>(graph, new Values<RuleGroup>() {
			@Override
			public int step(Node<RuleGroup> node) {
				return node.getObj().getStep();
			}

			@Override
			public double selfSscore(Node<RuleGroup> node) {
				List<RuleGroup> subs = node.getObj().getSubs();
				int size = 1;
				if (subs != null) {
					size = subs.size() + 1;
				}
				return node.getObj().getWeight() * size;
			}
		});


		return viterbi.rmLittlePath().compute(new Score() {
			@Override
			public double score(Node from, Node to) {
				return from.getScore() + to.getSelfScore();
			}

			@Override
			public boolean sort() {
				return true;
			}
		});
	}


	public static void main(String[] args) {
//		Parsing.parsing("上海浦东近年来颁布实行了涉及经济、贸易、建设、规划、科技、文教等领域的七十一件法规性文件，确保了浦东开发的有序进行。\n");


		Parsing.parse("这是一个简单的语法树啊。");

//		Parsing.parsing("海外投资企业在改善中国出口商品结构中发挥了显著作用。");

	}
}
