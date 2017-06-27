package org.nlpcn.parsing.util;

import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.MapCount;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.parsing.POSTagging;
import org.nlpcn.parsing.domain.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ansj on 2017/5/21.
 */
public class CorpusParse {

	public static void dicMaker() throws IOException {

		Set<String> rules = Arrays.stream(new String[]{"ip", "np", "vp", "pu", "lcp", "pp", "cp", "dnp", "advp", "adjp", "dp", "qp", "nn", "nr", "nt", "pn", "vc", "ve", "as", "vv", "cc", "va", "vr"}).collect(Collectors.toSet());


		BufferedReader reader = IOUtil.getReader("corpus/ctb8_all_in_one.txt", "utf-8");
		String line = null;

		MapCount<String> ruleCount = new MapCount<>();

		Map<String, MapCount<String>> wordNatureCount = new HashMap<>();

		MapCount<String> mcTemp = new MapCount<>();

		MapCount<String> singleRuleCount = new MapCount<>();

		while ((line = reader.readLine()) != null) {
			List<TNode> parse = parse(line.trim());

			for (TNode tn : parse) {
				if (tn.getName() == null) {
					continue;
				}
				MapCount<String> mc = wordNatureCount.getOrDefault(tn.getName(), new MapCount<String>());
				mc.add(tn.getNature().toLowerCase());
				wordNatureCount.put(tn.getName(), mc);
			}


			for (int i = 0; i < parse.size(); i++) {
				TNode t1 = parse.get(i);
				List<String> rule = new ArrayList<>();
				String ruleName = t1.getNature();
				if (ruleName.contains("-")) {
					ruleName = ruleName.split("-")[0];
				}
				ruleName = ruleName.toLowerCase();
				if (!rules.contains(ruleName)) {
					System.out.println(ruleName);
					continue;
				}

				rule.add(ruleName);
				singleRuleCount.add(ruleName);
				for (int j = i + 1; j < parse.size(); j++) {
					TNode t2 = parse.get(j);
					if (t1.getLevel() - t2.getLevel() != -1) {
						break;
					}
					String name = t2.getNature();
					if (name.contains("-")) {
						name = name.split("-")[0];
					}
					if (name.contains("=")) {
						name = name.split("=")[0];
					}

					mcTemp.add(name);
					rule.add(name);
				}
				if (rule.size() > 1) {
					ruleCount.add(StringUtil.joiner(rule, "@").toLowerCase());
				}
			}
		}


		try (FileOutputStream fos = new FileOutputStream(new File("src/main/resources/WORD_NATURE_COUNT"))) {
			for (Map.Entry<String, MapCount<String>> e : wordNatureCount.entrySet()) {
				fos.write(e.getKey().getBytes("utf-8"));
				fos.write("\t".getBytes("utf-8"));

				StringBuilder sb = new StringBuilder();
				e.getValue().get().entrySet().stream().forEach(e1 -> sb.append(e1.getKey().toLowerCase() + ":" + e1.getValue().intValue() + ","));
				String s = sb.toString();
				fos.write(s.substring(0, s.length() - 1).getBytes("utf-8"));
				fos.write("\n".getBytes("utf-8"));
			}
		}

		IOUtil.writeMap(ruleCount.get(), "src/main/resources/RULE_COUNT", "utf-8");

		IOUtil.writeMap(singleRuleCount.get(), "src/main/resources/SINGLE_RULE_COUNT", "utf-8");


	}

	private static List<TNode> parse(String line) {
		List<TNode> nodes = new ArrayList<>();
		int level = 0;
		int end = 0;
		int len = line.length() - 1;
		for (int index = 1; index < len; index++) {
			if (line.charAt(index) == '(') {
				index++;
				level++;
			}
			end = next(line, index);
			nodes.add(new TNode(line.substring(index, end), level));
			char c = line.charAt(end);
			if (c == '(') {
				index = end - 1;
			} else if (c == ')') {
				level--;
				for (int i = end + 1; i < len; i++) {
					c = line.charAt(i);
					if (c == ')') {
						level--;
						index = i;
					} else if (c == '(') {
						index = i - 1;
						break;
					}
				}
			}
		}

		return nodes;

	}

	private static int next(String line, int i) {

		for (; i < line.length(); i++) {
			switch (line.charAt(i)) {
				case '(':
				case ')':
					return i;
			}
		}
		return i;
	}


	public static void crfNatureFileTrain() throws IOException {
		FileOutputStream fos = new FileOutputStream("corpus/train.pos");

		String[] natures = new String[]{"ad", "as", "ba", "cc", "cd", "cs", "dec", "deg", "der", "dev", "dt", "etc", "fw", "ij", "jj", "lb", "lc", "m", "msp", "nn", "nr", "nt", "od", "on", "p", "pn", "pu", "sb", "sp", "va", "vc", "ve", "vv"};


		Set<String> set = Arrays.stream(natures).collect(Collectors.toSet());

		try (BufferedReader br = IOUtil.getReader("corpus/test.pos", "utf-8")) {
			String temp = null;
			while ((temp = br.readLine()) != null) {
				if (StringUtil.isBlank(temp)) {
					continue;
				}

				StringBuilder sb = new StringBuilder();
				String[] split = temp.split(" ");
				for (int i = 0; i < split.length; i++) {
					String[] s1 = split[i].split("_");
					String nature = s1[1].trim().toLowerCase();
					if (nature.contains("-")) {
						nature = nature.split("-")[0];
					}
					if (set.contains(nature)) {
						fos.write((s1[0] + "\t" + nature + "\n").getBytes("utf-8"));
					} else {
						System.err.println(nature);
						System.err.println(temp);
						sb = null;
						break;
					}
				}
				if (sb != null) {
					fos.write(sb.toString().getBytes("utf-8"));
				}
				fos.write("\n".getBytes("utf-8"));
			}
		}

	}

	public static void crfParsingFileTrain() throws IOException {
		BufferedReader reader = IOUtil.getReader("corpus/ctb8_all_in_one.txt", "utf-8");
		String temp = null ;

		int level = -1  ;
		List<TNode> nodes = new ArrayList<>() ;

		while((temp=reader.readLine())!=null){
			List<TNode> parse = parse(temp);
			for (TNode node: parse) {
				if(node.level==level){
					nodes.add(node);
				}else{
					System.out.println(nodes);
					nodes = new ArrayList<>() ;
					nodes.add(node);
					level = node.level ;
				}
			}

			System.out.println(nodes);

			System.out.println();
		}

	}

	/**
	 * 将依存文法转换为crf训练语聊
	 */
	public static void dependencyCrfFile() throws Exception {

		FileOutputStream fos = new FileOutputStream("corpus/dependency1.pos");

		MapCount<String> edgeMap = new MapCount<>();


		try (BufferedReader br = IOUtil.getReader("corpus/train.conll", "utf-8")) {
			String temp = null;

			List<DNode> list = new ArrayList<>();

			while ((temp = br.readLine()) != null) {
				if (StringUtil.isBlank(temp)) {

					List<String> words = new ArrayList<>() ;
					for (int i = 0; i < list.size(); i++) {
						words.add(list.get(i).name);
					}

					Element[] parse = POSTagging.parse(words);

					for (int i = 0; i < list.size(); i++) {
						list.get(i).nature = parse[i].getNature() ;
					}

					for (int i = 0; i < list.size(); i++) {
						DNode dNode = list.get(i);

						if (dNode.toIndex == 0) {
							dNode.tag = "-1_ROOT";
							continue;
						}

						DNode toNode = list.get(dNode.toIndex - 1);

						edgeMap.add(dNode.nature + "\t" + toNode.nature + "\t" + dNode.edge);

						edgeMap.add(dNode.name + "\t" + toNode.nature + "\t" + dNode.edge);

						boolean direction = dNode.index < toNode.index;

						int count = 0;
						if (direction) {
							for (int j = dNode.index; j <= toNode.index - 1; j++) {
								if (toNode.nature.equals(list.get(j).nature)) {
									count++;
								}
							}
						} else {
							for (int j = toNode.index - 1; j < dNode.index - 1; j++) {
								if (toNode.nature.equals(list.get(j).nature)) {
									count++;
								}
							}
						}

						dNode.tag = (direction ? "+" : "-") + count + "_" + toNode.nature;

					}

					for (int i = 0; i < list.size(); i++) {
						fos.write(list.get(i).toString().getBytes("utf-8"));
						fos.write("\n".getBytes());
					}

					fos.write("\n".getBytes());

					list = new ArrayList<>();
				} else {
					String[] split = temp.split("\t");
					DNode node = new DNode(Integer.parseInt(split[0]), Integer.parseInt(split[6]), split[1], split[3].toLowerCase(), split[7].trim().toLowerCase());
					list.add(node);
				}
			}

		}

		IOUtil.writeMap(edgeMap.get(), "src/main/resources/DEPENDENCY_EDGE", "utf-8");

		fos.close();
		fos.flush();
	}

	public static class DNode {
		int index;
		int toIndex;
		String name;
		String nature;
		String edge;
		String tag;

		public DNode(int index, int toIndex, String name, String nature, String edge) {
			this.index = index;
			this.name = name;
			this.toIndex = toIndex;
			this.nature = nature;
			this.edge = edge;

		}

		@Override
		public String toString() {
			return name + "\t" + nature + "\t" + tag;
		}
	}

	/**
	 * Created by Ansj on 19/05/2017.
	 */
	public static class TNode {

		public TNode(String nature, int level) {
			this.nature = nature.trim();
			this.level = level;
			String[] split = nature.split(" ");
			if (split.length == 2) {
				this.nature = split[0];
				this.name = split[1];
			}

		}

		private String nature;

		private String name;

		private int level;

		private List<TNode> tNodes = new ArrayList<>();

		public String getNature() {
			return nature;
		}

		public void setNature(String nature) {
			this.nature = nature;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<TNode> gettNodes() {
			return tNodes;
		}

		public void settNodes(List<TNode> tNodes) {
			this.tNodes = tNodes;
		}

		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.level);
			for (int i = 0; i < level; i++) {
				sb.append("\t");
			}
			sb.append(this.nature);
			if (StringUtil.isNotBlank(name)) {
				sb.append("\t");
				sb.append(this.name);
			}
			return sb.toString();
		}
	}


	public static void main(String[] args) throws Exception {
//		dicMaker();
//		crfNatureFileTrain() ;
		dependencyCrfFile();
//		crfParsingFileTrain() ;
	}

}
