package org.nlpcn.parsing;

import org.junit.Test;
import org.nlpcn.parsing.util.CrfTxtModel;

import java.io.IOException;

/**
 * Created by Ansj on 30/05/2017.
 */
public class ModelZip {

	@Test
	public void zipModel() throws IOException, ClassNotFoundException {
		CrfTxtModel model = CrfTxtModel.load("corpus/pos.model.txt");
		model.writeZipModel("src/main/resources/pos.model");
	}

	@Test
	public void zipDependencyModel() throws IOException, ClassNotFoundException {
		CrfTxtModel model = CrfTxtModel.load("corpus/model.txt");
		model.writeZipModel("src/main/resources/dependency.model");
	}

	@Test
	public void loadModel() throws IOException, ClassNotFoundException {
		CrfTxtModel model = CrfTxtModel.load("src/main/resources/pos.model");

	}

	@Test
	public void loadWapiti() throws Exception {
		CrfTxtModel model = CrfTxtModel.load("corpus/dependency_pattern_lw.txt","corpus/dependency.model.txt");
		model.writeZipModel("src/main/resources/dependency.model");
		new DependencyTest().accuracyate();
	}

	@Test
	public void loadDependencyModel() throws IOException, ClassNotFoundException {
		CrfTxtModel model = CrfTxtModel.load("src/main/resources/dependency.model");
		long start = System.currentTimeMillis() ;
		for (int i = 0; i < 100000; i++) {
			model.get("U01:上海","-1_nn"); ;
			model.get("U01:上海","+1_vc"); ;
			model.get("U01:上海","+3_nn"); ;
			model.get("U01:上海","+1_nr"); ;
		}
		System.out.println(System.currentTimeMillis()-start);

	}

}
