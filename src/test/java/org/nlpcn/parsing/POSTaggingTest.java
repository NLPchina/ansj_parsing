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
 * Created by Ansj on 29/03/2017.
 */
public class POSTaggingTest {

    @Test
    public void parse() throws Exception {
        Element[] parse = POSTagging.parse("我爱北京天安门,天安门上太阳升。");
        System.out.println(Arrays.toString(parse));
        parse = POSTagging.parse("我叫孙健是佑佑的爸爸");
        System.out.println(Arrays.toString(parse));
        parse = POSTagging.parse("上海浦东开发与法制建设同步");
        System.out.println(Arrays.toString(parse));
        parse = POSTagging.parse("上海浦东近年 来颁布了法律制度");
        System.out.println(Arrays.toString(parse));
        parse = POSTagging.parse("警察正在详细调查事故原因");
        System.out.println(Arrays.toString(parse));
        parse = POSTagging.parse("我调查了下事故的原因");
        System.out.println(Arrays.toString(parse));
        parse = POSTagging.parse("上海浦东近年来颁布实行了涉及经济、贸易、建设、规划、科技、文教等领域的七十一件法规性文件，确保了浦东开发的有序进行。\n");
        System.out.println(Arrays.toString(parse));
        parse = POSTagging.parse("去年初浦东新区诞生的中国第一家医疗机构药品采购服务中心，正因为一开始就比较规范，运转至今，成交药品一亿多元，没有发现一例回扣。");
        System.out.println(Arrays.toString(parse));
		parse = POSTagging.parse("上海浦东开发与法制建设同步");
		System.out.println(Arrays.toString(parse));
		parse = POSTagging.parse("咬死了猎人的狗");
		System.out.println(Arrays.toString(parse));
		parse = POSTagging.parse("反腐斗争是长期的");
		System.out.println(Arrays.toString(parse));
    }

    @Test
    public void accuracyate() throws Exception {
    	int success = 0 ;
    	int err =0 ;
        try(BufferedReader br = IOUtil.getReader("corpus/test.pos","utf-8")){
            String temp = null ;
            while((temp=br.readLine())!=null){
                if(StringUtil.isBlank(temp)){
                    continue;
                }

                String[] split = temp.split(" ");

                Map<String,String> map = new HashMap<>() ;

                int index = 0 ;
                StringBuilder sb = new StringBuilder() ;
                for (int i = 0; i < split.length; i++) {
					String[] kv = split[i].split("_");
					map.put(index+"_"+kv[0].length(),kv[1].toLowerCase()) ;
					index  += kv[0].length() ;
					sb.append(kv[0]) ;
				}

				Element[] parse = POSTagging.parse(sb.toString());

				index = 0 ;
				for (int i = 0; i < parse.length; i++) {
					Element element = parse[i];
					String key = index+"_"+element.getTerm().getName().length() ;
					String natureStr = map.get(key);
					if(natureStr!=null){
						if(natureStr.equals(element.getNature())){
							success++ ;
						}else{
							err++ ;
						}
					}

					index  += element.getTerm().getName().length();
				}
			}
        }

		System.out.println("success: "+success);
		System.out.println("error: "+err);
		System.out.println(success/(double)(success+err));
	}
}

