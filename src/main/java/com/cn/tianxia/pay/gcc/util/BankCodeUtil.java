package com.cn.tianxia.pay.gcc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class BankCodeUtil {

	interface MatchHander {
		boolean compare(int a, int b);
	}

	/***
	 * 银行列表
	 * @return
	 */
	public static Map<String, String> getBankNameMap() {

		Map<String, String> map = new HashMap<String, String>();

		map.put("102", "中国工商银行");
		map.put("103", "中国农业银行");
		map.put("103", "中国农业发展银行"); 
		map.put("104", "中国银行");
		map.put("105", "中国建设银行");
		map.put("106", "中信银行");
		map.put("309", "兴业银行");
		map.put("403", "中国邮政储蓄银行");
		map.put("310", "招商银行");
		map.put("320", "广发银行");
		map.put("340", "平安银行");
		map.put("350", "交通银行");
		map.put("340", "平安银行");
		map.put("350", "交通银行");
		map.put("360", "中国民生银行");
		
		/*
		 * map.put("CODE_203", "中国农业发展银行"); map.put("CODE_301", "交通银行");
		 * 
		 * map.put("CODE_303", "中国光大银行"); map.put("CODE_304", "华夏银行");
		 * 
		 * 
		 * 
		 * 
		 * map.put("CODE_310", "上海浦东发展银行"); map.put("CODE_313", "城市商业银行");
		 * map.put("CODE_314", "农村商业银行"); map.put("CODE_315", "恒丰银行");
		 * map.put("CODE_316", "浙商银行"); map.put("CODE_317", "农村合作银行");
		 * map.put("CODE_318", "渤海银行"); map.put("CODE_319", "徽商银行");
		 * map.put("CODE_320", "村镇银行"); map.put("CODE_321", "重庆三峡银行");
		 * map.put("CODE_322", "上海农村商业银行"); map.put("CODE_401", "城市信用社");
		 * map.put("CODE_402", "农村信用社");
		 * 
		 * map.put("CODE_501", "香港上海汇丰银行"); map.put("CODE_502", "东亚银行");
		 * map.put("CODE_503", "南洋商业银行"); map.put("CODE_504", "恒生银行");
		 * map.put("CODE_505", "中国银行（香港）"); map.put("CODE_989", "(香港地区)银行");
		 * map.put("CODE_506", "集友银行"); map.put("CODE_509", "星展银行（香港）");
		 * map.put("CODE_510", "永亨银行"); map.put("CODE_513", "大新银行");
		 * map.put("CODE_531", "美国花旗银行"); map.put("CODE_595", "新韩银行");
		 * map.put("CODE_621", "华侨银行"); map.put("CODE_622", "大华银行");
		 * map.put("CODE_623", "新加坡星展银行"); map.put("CODE_671", "渣打银行");
		 * map.put("CODE_775", "德富泰银行"); map.put("CODE_781", "厦门国际银行");
		 * map.put("CODE_782", "法国巴黎银行（中国）"); map.put("CODE_785", "华商银行");
		 * map.put("CODE_786", "青岛国际银行"); map.put("CODE_787", "华一银行");
		 * map.put("CODE_901", "中央结算公司"); map.put("CODE_909", "银行间清算所");
		 * map.put("CODE_999", "其他银行"); map.put("CODE_507", "创兴银行");
		 * map.put("CODE_508", "大众银行"); map.put("CODE_512", "永隆银行");
		 * map.put("CODE_596", "企业银行"); map.put("CODE_641", "奥地利奥合国际银行");
		 * map.put("CODE_661", "荷兰银行"); map.put("CODE_673", "英国巴克莱银行");
		 * map.put("CODE_681", "瑞典商业银行"); map.put("CODE_682", "瑞典北欧斯安银行");
		 * map.put("CODE_683", "瑞典银行"); map.put("CODE_691", "法国兴业银行");
		 * map.put("CODE_712", "德意志银行"); map.put("CODE_202", "中国进出口银行");
		 * map.put("CODE_762", "澳大利亚西太平洋银行"); map.put("CODE_771", "摩根士丹利国际银行");
		 * map.put("CODE_011", "国家金库"); map.put("CODE_514", "中信嘉华银行");
		 * map.put("CODE_523", "国泰世华商业银行"); map.put("CODE_532", "美国银行有限公司");
		 * map.put("CODE_533", "摩根大通银行"); map.put("CODE_534", "美国建东银行");
		 * map.put("CODE_561", "三菱东京日联银行"); map.put("CODE_563", "日本三井住友银行");
		 * map.put("CODE_597", "韩亚银行"); map.put("CODE_631", "盘古银行");
		 * map.put("CODE_201", "国家开发银行"); map.put("CODE_001", "中国人民银行");
		 * map.put("CODE_564", "瑞穗实业银行"); map.put("CODE_566", "日本三井住友信托银行");
		 * map.put("CODE_593", "友利银行"); map.put("CODE_594", "韩国产业银行");
		 * map.put("CODE_598", "韩国国民银行"); map.put("CODE_611", "马来西亚马来亚银行");
		 * map.put("CODE_616", "首都银行"); map.put("CODE_713", "德国商业银行");
		 * map.put("CODE_716", "德国北德意志州银行"); map.put("CODE_717", "中德住房储蓄银行");
		 * map.put("CODE_731", "意大利裕信银行"); map.put("CODE_732", "意大利联合圣保罗银行");
		 * map.put("CODE_742", "瑞士银行"); map.put("CODE_751", "加拿大丰业银行");
		 * map.put("CODE_906", "国泰君安证券"); map.put("CODE_907", "其他银行907");
		 * map.put("CODE_908", "其他银行908"); map.put("CODE_788",
		 * "农村信用社（含北京农村商业银行）"); map.put("CODE_793", "法国巴黎银行");
		 * map.put("CODE_789", "汇丰银行"); map.put("CODE_790", "花旗银行");
		 * map.put("CODE_888", "外资银行"); map.put("CODE_791", "星展银行");
		 * map.put("CODE_792", "华美银行"); map.put("CODE_794", "银行间市场清算所");
		 * map.put("CODE_761", "澳大利亚和新西兰银行"); map.put("CODE_969",
		 * "(澳门地区)大西洋银行");
		 */
		return map;
	}

	/**
	 * DNA分析 拼字检查 语音辨识 抄袭侦测
	 * 
	 * @createTime 2012-1-12
	 */
	public static float levenshtein(String str1, String str2) {
		// 计算两个字符串的长度。
		int len1 = str1.length();
		int len2 = str2.length();
		// 建立上面说的数组，比字符长度大一个空间
		int[][] dif = new int[len1 + 1][len2 + 1];
		// 赋初值，步骤B。
		for (int a = 0; a <= len1; a++) {
			dif[a][0] = a;
		}
		for (int a = 0; a <= len2; a++) {
			dif[0][a] = a;
		}
		// 计算两个字符是否一样，计算左上的值
		int temp;
		for (int i = 1; i <= len1; i++) {
			for (int j = 1; j <= len2; j++) {
				if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
					temp = 0;
				} else {
					temp = 1;
				}
				// 取三个值中最小的
				dif[i][j] = min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1, dif[i - 1][j] + 1);
			}
		}
		// System.out.println("字符串\""+str1+"\"与\""+str2+"\"的比较");
		// 取数组右下角的值，同样不同位置代表不同字符串的比较
		// System.out.println("差异步骤："+dif[len1][len2]);
		// 计算相似度
		float similarity = 1 - (float) dif[len1][len2] / Math.max(str1.length(), str2.length());
		// System.out.println("相似度："+similarity);

		return similarity;
	}

	// 得到最小值
	private static int min(int... is) {
		int min = Integer.MAX_VALUE;
		for (int i : is) {
			if (min > i) {
				min = i;
			}
		}
		return min;
	}

	/**
	 * 取出数组中的最大值
	 * 
	 * @param ary
	 * @return
	 */
	public static float getMax(Float[] ary) {
		float max = ary[0];
		for (int i = 1; i < ary.length; i++) {
			float b = ary[i];
			if (b > max) {
				max = b;
			}
		}
		return max;
	}

	/***
	 * 根据银行名称获取银行编码
	 * 
	 * @param bankName
	 * @return
	 */
	public static String getBankCode(String bankName) {

		// 银行列表
		Map<String, String> bankList = getBankNameMap();

		String bankCode = StringUtils.EMPTY;
		Iterator s = bankList.entrySet().iterator();
		List<Float> list = new ArrayList<>();
		Map<Float, String> rMap = new HashMap<Float, String>();

		while (s.hasNext()) {// 只遍历一次,速度快
			@SuppressWarnings("rawtypes")
			Map.Entry e = (Map.Entry) s.next();
			String name = (String) e.getValue();
			String code = String.valueOf(e.getKey());
			System.out.println(code + ":" + name);
			if (match(0, name, bankName)) {
				bankCode = code;
				break;
			}
		}
		return bankCode;
	}

	/**
	 * 百分之多少之内匹配错误可以接受 a与ab匹配为百分之50的错误率。
	 * 
	 * @param percent
	 *            设置匹配百分比
	 * @param src
	 *            字符串1
	 * @param dest
	 *            字符串2
	 * @param hander
	 *            匹配规则
	 * @return
	 */
	public static boolean match(double percent, String src, String dest, MatchHander hander) {
		char[] csrc = src.toCharArray();
		char[] cdest = dest.toCharArray();
		double score = 0;
		int max = csrc.length > cdest.length ? csrc.length : cdest.length;
		score = cal(csrc, 0, cdest, 0, hander, 0, (int) Math.ceil((1 - percent) * max));
		System.out.println("最小匹配百分比：" + percent + "，成功匹配百分比：" + score / max);
		return score / max > percent;
	}

	/**
	 * 几个错误的字符可以接受 a与ab为1个字符错误可以接受
	 * 
	 * @param percent
	 *            设置匹配百分比
	 * @param src
	 *            字符串1
	 * @param dest
	 *            字符串2
	 * @param hander
	 *            匹配规则
	 * @return
	 */
	public static boolean match(int errorNum, String src, String dest, MatchHander hander) {
		char[] csrc = src.toCharArray();
		char[] cdest = dest.toCharArray();
		int score = 0;
		score = cal(csrc, 0, cdest, 0, hander, 0, errorNum);
		int max = csrc.length > cdest.length ? csrc.length : cdest.length;
		System.out.println("可以接受错误数：" + errorNum + "，发现错误数：" + (max - score));
		return max - score <= errorNum;
	}

	/**
	 * 2个字符串75%匹配成功返回true
	 * 
	 * @param src
	 * @param dest
	 * @return
	 */
	public static boolean match(double percent, String src, String dest) {
		return match(percent, src, dest, new MatchHander() {

			@Override
			public boolean compare(int a, int b) {
				return a == b;
			}
		});
	}

	/**
	 * 2个字符串错几个字符可以接受
	 * 
	 * @param errorNum
	 * @param src
	 * @param dest
	 * @return
	 */
	public static boolean match(int errorNum, String src, String dest) {
		return match(errorNum, src, dest, new MatchHander() {

			@Override
			public boolean compare(int a, int b) {
				return a == b;
			}
		});
	}

	/**
	 * 使用递归方法匹配字符串
	 * 
	 * @param csrc
	 * @param i
	 * @param cdest
	 * @param j
	 * @param hander
	 * @return
	 */
	private static int cal(char[] csrc, int i, char[] cdest, int j, MatchHander hander, int curdeep, int maxdeep) {
		int score = 0;
		if (curdeep > maxdeep || i >= csrc.length || j >= cdest.length)
			return 0;
		boolean ismatch = hander.compare(csrc[i], cdest[j]);
		if (ismatch) {
			score++;
			if (i + 1 < csrc.length && j + 1 < cdest.length)
				score += cal(csrc, i + 1, cdest, j + 1, hander, 0, maxdeep);
		} else {
			int temp1 = 0;
			int temp2 = 0;
			int temp3 = 0;
			temp1 += cal(csrc, i, cdest, j + 1, hander, curdeep + 1, maxdeep);
			temp2 += cal(csrc, i + 1, cdest, j, hander, curdeep + 1, maxdeep);
			temp3 += cal(csrc, i + 1, cdest, j + 1, hander, curdeep + 1, maxdeep);
			int temp4 = Math.max(temp1, temp2);
			score += Math.max(temp3, temp4);
		}
		return score;
	}

	public static void main(String[] args) {
		// 测试Map
		Map<String, String> bankList = getBankNameMap();
		// System.out.println(JSON.toJSONString(bankList));

		String bankCode = getBankCode("中国农业银行");
		System.out.println("-------------------------\n" + bankCode);
	}
}
