package org.nlpcn.parsing.util;

import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.commons.lang.util.logging.Log;
import org.nlpcn.commons.lang.util.logging.LogFactory;
import org.nlpcn.commons.lang.util.tuples.Triplet;
import org.nlpcn.parsing.domain.Feature;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

/**
 * Created by Ansj on 28/05/2017.
 */
public class CrfTxtModel implements Serializable {

	private static final String POM = "ansj_model";

	private static final Log LOG = LogFactory.getLog();

	private List<String> tag = new ArrayList<>();

	private Map<String, Integer> tagIndex = new HashMap<>();

	private List<Feature> template = new ArrayList<>();

	private Map<String, float[]> features = new HashMap<>();

	private float[][] bigramFeature = new float[0][0];

	/**
	 * 是否是稀疏特征
	 */
	private boolean spare = false;


	/**
	 * 用来加载wapiti dump出来的模型
	 *
	 * @param patternPath
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static CrfTxtModel load(String patternPath, String path) throws IOException, ClassNotFoundException {

		CrfTxtModel model = new CrfTxtModel();

		model.spare = true;

		long start = System.currentTimeMillis();

		String temp = null;

		try (BufferedReader reader = IOUtil.getReader(patternPath, "utf-8")) {

			while ((temp = reader.readLine()) != null) { //读取tag
				if (StringUtil.isNotBlank(temp)) {
					if (temp.charAt(0) == 'U') {
						model.template.add(new Feature(temp));
					} else {
						LOG.info("skip feature " + temp);
					}
				} else {
					break;
				}
			}
		}

		List<Triplet<Integer, Integer, Float>> bigram = new ArrayList<>();

		try (BufferedReader reader = IOUtil.getReader(path, "utf-8")) {

			int index = -1;

			while ((temp = reader.readLine()) != null) { //读取tag
				if (StringUtil.isBlank(temp)) {
					continue;
				}


				String[] split = temp.split("\t");
				String feature = split[0];
				String tag = split[2];

				Integer tempIndex = model.tagIndex.get(tag);
				if (tempIndex == null) {
					tempIndex = ++index;
					model.tagIndex.put(tag, tempIndex);
				}

				float weight = Float.parseFloat(split[3]);

				if (feature.charAt(0) == 'u') {
					char[] chars = feature.toCharArray();
					chars[0] = Character.toUpperCase(chars[0]);
					feature = new String(chars);
				} else if ("b".equals(feature)) { //级联特征
					String tagF = split[1];
					String tagT = tag;
					tempIndex = model.tagIndex.get(tagF);
					if (tempIndex == null) {
						tempIndex = ++index;
						model.tagIndex.put(tag, tempIndex);
					}
					bigram.add(Triplet.with(model.tagIndex.get(tagF), model.tagIndex.get(tagT), weight)) ;
					continue;
				}

				float[] floats = model.features.get(feature);
				if (floats == null) {
					floats = new float[1];
				} else {
					floats = Arrays.copyOf(floats, floats.length + 1);
				}
				model.features.put(feature, floats);

				floats[floats.length - 1] = tempIndex * 1000 + weight;
			}

			for (float[] floats : model.features.values()) {
				Arrays.parallelSort(floats);
			}
		}

		model.bigramFeature = new float[model.tagIndex.size()][model.tagIndex.size()];

		for (Triplet<Integer, Integer, Float> triplet : bigram) {
			model.bigramFeature[triplet.getValue0()][triplet.getValue1()] = triplet.getValue2();
		}

		LOG.info("read modle ok use time : " + (System.currentTimeMillis() - start));

		return model;
	}


	/**
	 * 加载crf模型和内置的压缩模型
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static CrfTxtModel load(String path) throws IOException, ClassNotFoundException {

		CrfTxtModel model = new CrfTxtModel();

		long start = System.currentTimeMillis();

		boolean flag = false;

		try (InputStream is = new FileInputStream(path)) {
			flag = isAnsjModel(is);
		}

		if (flag) {
			readAnsjModel(path, model);
		} else {
			readTxt(path, model);
		}

		LOG.info("read modle ok use time : " + (System.currentTimeMillis() - start));

		return model;
	}

	public static boolean isAnsjModel(InputStream input) throws IOException, ClassNotFoundException {
		String pom = null;
		try (ObjectInputStream is = new ObjectInputStream(new GZIPInputStream(input))) {
			byte[] bytes = new byte[POM.getBytes().length];
			is.read(bytes);
			pom = new String(bytes);
		} catch (ZipException | StreamCorruptedException e) {

		}
		return POM.equals(pom);
	}

	public static void readAnsjModel(String path, CrfTxtModel model) throws IOException, ClassNotFoundException {
		readAnsjModel(new FileInputStream(path), model);
	}

	public static void readAnsjModel(InputStream is, CrfTxtModel model) throws IOException, ClassNotFoundException {
		try (ObjectInputStream oos = new ObjectInputStream(new GZIPInputStream(is))) {
			oos.read(new byte[POM.getBytes().length]); // skip pom
			model.spare = oos.readBoolean(); //是否稀疏矩阵
			model.tag = (List<String>) oos.readObject();
			model.tagIndex = (Map<String, Integer>) oos.readObject();
			model.template = (List<Feature>) oos.readObject();
			model.bigramFeature = (float[][]) oos.readObject();
			int size = oos.readInt();
			model.features = new HashMap<>(size);
			for (int i = 0; i < size; i++) {
				String name = oos.readUTF();
				model.features.put(name, (float[]) oos.readObject());
			}
		}
	}

	public static void readTxt(String path, CrfTxtModel model) throws IOException {
		try (BufferedReader reader = IOUtil.getReader(path, "utf-8")) {
			LOG.info("read version : " + reader.readLine());
			LOG.info("read cost-factor: : " + reader.readLine());
			LOG.info("read maxid : " + reader.readLine());
			LOG.info("read xsize : " + reader.readLine());

			reader.readLine();//empty line

			String temp = null;
			int index = 0;
			while ((temp = reader.readLine()) != null) { //读取tag
				if (StringUtil.isNotBlank(temp)) {
					model.tag.add(temp);
					model.tagIndex.put(temp, index);
					index++;
				} else {
					break;
				}
			}
			LOG.info("read tag ok : " + model.tag);

			while ((temp = reader.readLine()) != null) { //读取tag
				if (StringUtil.isNotBlank(temp)) {
					if (temp.charAt(0) == 'U') {
						model.template.add(new Feature(temp));
					} else {
						LOG.info("skip feature " + temp);
					}

				} else {
					break;
				}
			}
			LOG.info("read template ok : " + model.template);

			List<String> featureStrList = new ArrayList<>(331627);
			while ((temp = reader.readLine()) != null) { //读取tag
				if (StringUtil.isNotBlank(temp)) {
					featureStrList.add(temp);
				} else {
					break;
				}
			}
			LOG.info("read featureStrList ok, featureStrList size : " + featureStrList.size());


			for (int i = 0; i < featureStrList.size(); i++) {
				temp = featureStrList.get(i);
				String[] split = temp.split(" ");


				if (split[1].charAt(0) == 'U') {
					float[] vector = new float[model.tag.size()];
					for (int j = 0; j < model.tag.size(); j++) {
						vector[j] = Float.parseFloat(reader.readLine());
					}
					model.features.put(split[1], vector);
				} else if (split[1].charAt(0) == 'B') { //加载Bigram template
					model.bigramFeature = new float[model.tag.size()][model.tag.size()];
					for (int j = 0; j < model.tag.size(); j++) {
						for (int k = 0; k < model.tag.size(); k++) {
							model.bigramFeature[j][k] = Float.parseFloat(reader.readLine());
						}
					}
				} else {
					LOG.warn("err feature " + temp);
				}
			}

		}
	}


	public List<String> getTag() {
		return tag;
	}

	public List<Feature> getTemplate() {
		return template;
	}


	/**
	 * 获取特征权重
	 *
	 * @param feature
	 * @param tag
	 * @return
	 */
	public float get(String feature, String tag) {
		Integer index = tagIndex.get(tag);
		if (index == null) {
			return 0;
		}
		float[] floats = features.get(feature);
		if (floats == null) {
			return 0;
		}

		float value = index * 1000;

		if (spare) { //二分查找,最近
			int i = Arrays.binarySearch(floats, value);

			if (i >= 0) {
				value = floats[i] - value;
			} else if (i == -1) {
				value = floats[0] - value;
			} else if (Math.abs(i + 1) >= floats.length) {
				value = floats[floats.length - 1] - value;
			} else {
				i = Math.abs(i + 1);
				if ((Math.abs(value - floats[i - 1]) > Math.abs(value - floats[i]))) {
					value = floats[i] - value;
				} else {
					value = floats[i - 1] - value;
				}
			}
			if (Math.abs(value) > 500) {
				return 0f;
			}
			return value;
		} else {
			return floats[index];
		}
	}

	public float getBigramWeight(String from, String to) {
		Integer fromIndex = tagIndex.get(from);
		Integer toIndex = tagIndex.get(to);
		if (fromIndex == null || toIndex == null) {
			return 0;
		}
		return bigramFeature[fromIndex][toIndex];
	}

	public void writeZipModel(String path) throws IOException {
		try (ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(path)))) {
			out.writeBytes(POM);
			out.writeBoolean(spare);
			out.writeObject(tag);
			out.writeObject(tagIndex);
			out.writeObject(template);
			out.writeObject(bigramFeature);
			out.writeInt(features.size());
			for (Map.Entry<String, float[]> entry : features.entrySet()) {
				out.writeUTF(entry.getKey());
				out.writeObject(entry.getValue());
			}
		}
	}


}
