package org.nlpcn.parsing;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.recognition.impl.NatureRecognition;
import org.ansj.recognition.impl.UserDicNatureRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.util.tuples.KeyValue;
import org.nlpcn.commons.lang.viterbi.Node;
import org.nlpcn.commons.lang.viterbi.Viterbi;
import org.nlpcn.commons.lang.viterbi.function.Score;
import org.nlpcn.commons.lang.viterbi.function.Values;
import org.nlpcn.parsing.domain.Element;
import org.nlpcn.parsing.domain.Feature;
import org.nlpcn.parsing.domain.Nature;
import org.nlpcn.parsing.util.CrfTxtModel;
import org.nlpcn.parsing.util.StaticValue;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * 针对parsing特定的分词器
 * Created by Ansj on 29/03/2017.
 */
public class POSTagging {

	/**
	 * 加载CRF模型,这个必须在最下面
	 */
	private static final CrfTxtModel MODEL = init();

	private static CrfTxtModel init() {
		try (InputStream is = StaticValue.getInputStreamInJar("/pos.model")) {
			CrfTxtModel model = new CrfTxtModel();
			CrfTxtModel.readAnsjModel(is, model);
			return model;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Element[] parse(String str, Forest... forests) {
		Result parse = ToAnalysis.parse(str, forests).recognition(new UserDicNatureRecognition(forests));

		Element[] elements = new Element[parse.size()];
		int index = 0;
		for (Term term : parse) {
			elements[index] = new Element(term);
			index++;
		}
		viterbi(elements);
		return elements;
	}


	/**
	 * 将分好词的结果进行词性标注
	 *
	 * @param words
	 * @param forests
	 * @return
	 */
	public static Element[] parse(List<String> words, Forest... forests) {
		List<Term> terms = new NatureRecognition(forests).recognition(words);
		Element[] elements = new Element[terms.size()];
		int index = 0;
		for (Term term : terms) {
			elements[index] = new Element(term);
			index++;
		}
		viterbi(elements);
		return elements;
	}

	private static void viterbi(Element[] elements) {
		Nature[][] natures = new Nature[elements.length][];


		for (int i = 0; i < natures.length; i++) {
			natures[i] = elements[i].getNatures();
		}

		Viterbi<Nature> natureViterbi = new Viterbi<>(natures, new Values<Nature>() {
			@Override
			public int step(Node<Nature> node) {
				return 1;
			}

			@Override
			public double selfSscore(Node<Nature> node) {

				List<Feature> template = MODEL.getTemplate();

				int index = node.getIndex();

				double score = 0;

				featureLoop:
				for (Feature feature : template) {
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

						sb.append(elements[indexValue].getTerm().getName());
						notFirst = true;
					}
					String tag = node.getObj().getName();

					score += MODEL.get(sb.toString(), tag);
				}
				return score;
			}
		});


		List<Nature> compute = natureViterbi.compute(new Score<Nature>() {

			@Override
			public double score(Node<Nature> from, Node<Nature> to) {
				double result = from.getScore() + to.getSelfScore();
				result += MODEL.get(from.getObj().getName(), to.getObj().getName());
				return result;
			}

			@Override
			public boolean sort() {
				return true;
			}
		});

		Iterator<Nature> iterator = compute.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			Nature next = iterator.next();
			elements[index++].setNature(next.getName());
		}

	}

	public static Element[] parse(String str) {
		return parse(str, DicLibrary.get(StaticValue.getDicParsing()));
	}

	public static Element[] parse(List<String> words) {
		return parse(words, DicLibrary.get(StaticValue.getDicParsing()));
	}
}
