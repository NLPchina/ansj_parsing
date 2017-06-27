package org.nlpcn.parsing.util;

import org.ansj.library.DicLibrary;
import org.ansj.util.MyStaticValue;
import org.nlpcn.commons.lang.tire.ObjTree;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.commons.lang.util.logging.Log;
import org.nlpcn.commons.lang.util.logging.LogFactory;
import org.nlpcn.commons.lang.util.tuples.KeyValue;
import org.nlpcn.parsing.domain.Nature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ansj on 29/03/2017.
 */
public class StaticValue {

	private static final Log LOG = LogFactory.getLog();


	/**
	 * 词典key
	 */
	private static final String DIC_PARSING = "dic_parsing";

	/**
	 * 词语	词性1:词频1,词性2:词频2
	 */
	private static final Map<String, Nature[]> WORD_NATURE_COUNT = new HashMap<>();


	/**
	 * 词性关系映射
	 */
	private static final Map<String, String> NATURE_MAPPING = new HashMap<>();

	/**
	 * 规则合并
	 */
	private static final ObjTree<KeyValue<String, Double>> RULE_COUNT = new ObjTree();


	private static final Map<String, KeyValue<String, Double>> DEPENDENCY_EDGE = new HashMap<>();


	/**
	 * 单独每条规则的频次
	 */
	private static final Map<String, Double> SINGLE_RULE_COUNT = new HashMap<>();


	static {
		init();
	}

	/**
	 * 加载词典
	 *
	 * @throws IOException
	 */
	public static CrfTxtModel init() {

		try {

			HashMap<String, String> natureMapping = IOUtil.loadMap(getInputStreamInJar("/nature.map"), "utf-8", String.class, String.class);
			NATURE_MAPPING.putAll(natureMapping);
			LOG.info("load NATURE_MAPPING OK");

			HashMap<String, Double> singleRuleCount = IOUtil.loadMap(getInputStreamInJar("/SINGLE_RULE_COUNT"), "utf-8", String.class, Double.class);
			SINGLE_RULE_COUNT.putAll(singleRuleCount);
			LOG.info("load SINGLE_RULE_COUNT OK");


			try (BufferedReader reader = IOUtil.getReader(getInputStreamInJar("/DEPENDENCY_EDGE"), "utf-8")) {
				String temp = null;
				while ((temp = reader.readLine()) != null) {
					String[] split = temp.split("\t");

					StringBuilder sb = new StringBuilder();

					String joiner = StringUtil.joiner(Arrays.copyOf(split, split.length - 2), "@");

					KeyValue<String, Double> kv = KeyValue.with(split[split.length - 2], Double.parseDouble(split[split.length - 1]));


					KeyValue<String, Double> tempkv = DEPENDENCY_EDGE.get(joiner);

					if (tempkv == null) {
						DEPENDENCY_EDGE.put(joiner, kv);
					} else {
						if (kv.getValue() > tempkv.getValue()) {
							DEPENDENCY_EDGE.put(joiner, kv);
						}
					}

				}
			}
			LOG.info("load DEPENDENCY_EDGE OK");


			try (BufferedReader br = IOUtil.getReader(getInputStreamInJar("/RULE_COUNT"), "utf-8")) {
				String temp = null;
				while ((temp = br.readLine()) != null) {
					String[] kvStr = temp.split("\t");
					String[] split = kvStr[0].split("@");
					if (split.length < 2) {
						continue;
					}
					Object[] objs = new Object[split.length - 1];
					for (int i = 1; i < split.length; i++) {
						objs[i - 1] = split[i];
					}
					RULE_COUNT.add(KeyValue.with(split[0], Double.parseDouble(kvStr[1])), objs);
				}
			}
			LOG.info("load RULE_COUNT OK");


			Forest forest = new Forest();
			MyStaticValue.putLibrary(DIC_PARSING, DIC_PARSING, forest);
			try (BufferedReader br = IOUtil.getReader(getInputStreamInJar("/WORD_NATURE_COUNT"), "utf-8")) {
				String temp = null;
				while ((temp = br.readLine()) != null) {
					String[] split = temp.split("\t");

					Nature[] natures = Arrays.stream(split[1].split(",")).map(str -> {
						String[] natureFreq = str.split(":");
						return new Nature(natureFreq[0], Double.parseDouble(natureFreq[1]));
					}).toArray(Nature[]::new);
					WORD_NATURE_COUNT.put(split[0], natures);
					Double sum = Arrays.stream(natures).mapToDouble(n -> n.getFreq()).sum();

					DicLibrary.insert(DIC_PARSING, split[0], "userDefine", sum.intValue());
				}
			}
			LOG.info("load WORD_NATURE_COUNT OK");

		} catch (Exception e) {
			LOG.error("init nature file err : ", e);
		}
		return null;

	}

	public static InputStream getInputStreamInJar(String path) {
		return StaticValue.class.getResourceAsStream(path);
	}

	public static String getDicParsing() {
		return DIC_PARSING;
	}

	public static Nature[] getWordNatureCount(String key) {
		return WORD_NATURE_COUNT.get(key);
	}

	public static String getNatureMapping(String key) {
		return NATURE_MAPPING.get(key);
	}

	public static ObjTree<KeyValue<String, Double>> getRuleCount() {
		return RULE_COUNT;
	}

	public static double getSingleRuleCount(String key) {
		return SINGLE_RULE_COUNT.getOrDefault(key, 1D);
	}

	public static KeyValue<String, Double> getDependencyEdge(String key) {
		return DEPENDENCY_EDGE.get(key);
	}

}

