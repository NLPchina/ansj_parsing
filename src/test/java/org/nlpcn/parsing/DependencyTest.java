package org.nlpcn.parsing;

import org.junit.Test;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.parsing.domain.Element;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ansj on 15/06/2017.
 */
public class DependencyTest {

	@Test
	public void test() {
		Element[] parse = Dependency.parse("美丽需要学习");
		System.out.println(Arrays.toString(parse));
		parse = Dependency.parse("我叫孙健是佑佑的爸爸");
		System.out.println(Arrays.toString(parse));
		parse = Dependency.parse("上海浦东开发与法制建设同步");
		System.out.println(Arrays.toString(parse));
		parse = Dependency.parse("上海浦东近年 来颁布了法律制度");
		System.out.println(Arrays.toString(parse));
		parse = Dependency.parse("警察正在详细调查事故原因");
		System.out.println(Arrays.toString(parse));
		parse = Dependency.parse("我调查了下事故的原因");
		System.out.println(Arrays.toString(parse));
		parse = Dependency.parse("上海浦东近年来颁布实行了涉及经济、贸易、建设、规划、科技、文教等领域的七十一件法规性文件，确保了浦东开发的有序进行。\n");
		System.out.println(Arrays.toString(parse));
		parse = Dependency.parse("去年初浦东新区诞生的中国第一家医疗机构药品采购服务中心，正因为一开始就比较规范，运转至今，成交药品一亿多元，没有发现一例回扣。");
		System.out.println(Arrays.toString(parse));
		parse = Dependency.parse("上海浦东开发与法制建设同步");
		System.out.println(Arrays.toString(parse));
		parse = Dependency.parse("咬死了猎人的狗");
		System.out.println(Arrays.toString(parse));
		parse = Dependency.parse("全球的游戏市场真是一块大蛋糕啊");
		System.out.println(Arrays.toString(parse));
		parse = Dependency.parse("反腐斗争是长期的");
		System.out.println(Arrays.toString(parse));
	}

	@Test
	public void accuracyate() throws Exception {
		int success = 0;
		int err = 0;
		try (BufferedReader br = IOUtil.getReader("corpus/dependency1.pos", "utf-8")) {
			String temp = null;
			StringBuilder sb = new StringBuilder();
			int index = 0;
			Map<String, String> map = new HashMap<>();
			while ((temp = br.readLine()) != null) {
				if (StringUtil.isBlank(temp)) {
					Element[] parse = Dependency.parse(sb.toString());
					index = 0;
					for (int i = 0; i < parse.length; i++) {
						Element element = parse[i];
						String key = index + "_" + element.getTerm().getName().length();
						String tagName = map.get(key);


						if (tagName != null) {

							String resultTag = null;

							if (element.getDepyIndex() != -1) {
								Element to = parse[element.getDepyIndex()];
								int count = 0;
								if (element.getDepyIndex() > i) {
									for (int j = i + 1; j <= element.getDepyIndex(); j++) {
										if (parse[j].getNature().equals(to.getNature())) {
											count++;
										}
									}
									resultTag = "+" + count + "_" + to.getNature();
								} else {
									for (int j = element.getDepyIndex(); j < i; j++) {
										if (parse[j].getNature().equals(to.getNature())) {
											count++;
										}
									}
									resultTag = "-" + count + "_" + to.getNature();
								}
							} else {
								resultTag = "-1_ROOT";
							}
							if (tagName.equals(resultTag)) {
								success++;
							} else {
								err++;
							}
						}

						index += element.getTerm().getName().length();
					}

					sb = new StringBuilder();
					index = 0;
					map = new HashMap<>();
				} else {
					String[] split = temp.split("\t");

					map.put(index + "_" + split[0].length(), split[2]);
					index += split[0].length();
					sb.append(split[0]);
				}
			}

			System.out.println("success: " + success);
			System.out.println("error: " + err);
			System.out.println(success / (double) (success + err));
		}
	}
}
