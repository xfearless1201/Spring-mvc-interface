package com.cn.tianxia.pay.tx.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class DateUtil {
	/**
	 * 时间单位
	 */
	public static final String YEAR = "YEAR";
	public static final String MONTH = "MONTH";
	public static final String DAY = "DAY";
	public static final String HOUR = "HOUR";
	public static final String MINUTE = "MINUTE";
	public static final String SECOND = "SECOND";

	/**
	 * yyyyMMdd格式字符
	 */
	public static final String dataFormatyyyyMMdd = "yyyyMMdd";

	/**
	 * HHmmss格式字符
	 */
	public static final String dataFormatHHmmss = "HHmmss";

	/**
	 * yyyyMMddHHmmss格式字符
	 */
	public static final String dataFormatyyyyMMddHHmmss = "yyyyMMddHHmmss";

	/**
	 * yyyy-MM-dd格式字符
	 */
	public static final String dataFormatyyyy_MM_dd = "yyyy-MM-dd";
	/**
	 * yyyyMM格式字符
	 */
	public static final String dataFormatyyyyMM = "yyyyMM";
	
	/**
	 * yyyy格式字符
	 */
	public static final String dataFormatyyyy = "yyyy";

	/**
	 * HH:mm:ss格式字符
	 */
	public static final String dataFormatHH_mm_ss = "HH:mm:ss";

	/**
	 * 转换为时间（天,时:分:秒.毫秒）
	 * 
	 * @param timeMillis
	 * @return
	 */
	public static String formatDateTime(long timeMillis) {
		long day = timeMillis / (24 * 60 * 60 * 1000);
		long hour = (timeMillis / (60 * 60 * 1000) - day * 24);
		long min = ((timeMillis / (60 * 1000)) - day * 24 * 60 - hour * 60);
		long s = (timeMillis / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		long sss = (timeMillis - day * 24 * 60 * 60 * 1000 - hour * 60 * 60
				* 1000 - min * 60 * 1000 - s * 1000);
		return (day > 0 ? day + "," : "") + hour + "小时" + min + "分" + s + "秒"
				+ sss + "毫秒";
	}


	/**
	 * 取当前时间
	 * 
	 * @return
	 */
	public static Date getDate() {
		return new Date();
	}

	public static Calendar getCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate());
		return cal;
	}

	public static boolean isMonthFirstDay(Date date) {
		Calendar cDay = Calendar.getInstance();
		cDay.setTime(date);
		int today = cDay.get(Calendar.DAY_OF_MONTH);
		if (today == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isWeekFirstDay(Date fromDate){
		Calendar cDay = Calendar.getInstance();
		cDay.setTime(fromDate);
		int today = cDay.get(Calendar.DAY_OF_WEEK);
		if (today == 1) {
			return true;
		} else {
			return false;
		}
	}
	public static boolean isFirstDay(Date fromDate) {
		Calendar cDay = Calendar.getInstance();
		cDay.setTime(fromDate);
		int today = cDay.get(Calendar.WEEK_OF_YEAR);
		if (today == 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 根据format串返回当前日期串
	 * 
	 * @param fomatString
	 * @return
	 */
	public static String getDateStringByFormatString(String fomatString) {
		Date date = getDate();
		SimpleDateFormat df = new SimpleDateFormat(fomatString);
		return df.format(date);
	}

	/**
	 * 获取指定日期时间的增加偏移量后格式化的字符串
	 * 
	 * @param minute
	 *            分钟偏移量
	 * @return 格式化的日期时间字符串
	 */
	public String getSpecifyDateStringByMinuteOffset(int value) {
		Calendar calendar = getCalendar();
		calendar.add(Calendar.MINUTE, value);

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
		return df.format(calendar.getTime());
	}

	/**
	 * 获取指定日期时间的增加偏移量后格式化的字符串
	 * 
	 * @param minute
	 *            分钟偏移量
	 * @return 格式化的日期时间字符串
	 */
	public static String getSpecifyDateStringByMonthOffset(int value) {
		Calendar calendar = getCalendar();
		calendar.add(Calendar.MONTH, value);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
		return df.format(calendar.getTime());
	}

	/**
	 * Title: convertDateToString<br/>
	 * Description: 把data转为制定格式的字符串<br/>
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String convertDateToString(Date date, String format) {
		if (date == null) {
			return null;
		}
		if (format == null) {
			format = "yyyyMMddHHmmss";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		String result = sdf.format(date);
		return result;
	}

	/**
	 * 根据long类型的时间转出format的时间
	 * 
	 * @param timeLongVal
	 * @param descFormat
	 * @return
	 */
	public static String formatDateLongToString(Long timeLongVal,
			String descFormat) {
		Date date = new Date(timeLongVal);
		SimpleDateFormat df = new SimpleDateFormat(descFormat);
		return df.format(date);
	}

	/**
	 * 
	 * Title: convertFormatDataToString<br/>
	 * Description: 从yyyy-MM-dd HH:mm:ss 格式的时间里面取出 ddHHmmss<br/>
	 * 
	 * @param date
	 * @return
	 */
	public String convertFormatDataToString(String date) {
		String[] datesplite = date.split(" ");
		String[] before = datesplite[0].split("-");
		String[] after = datesplite[1].split(":");

		StringBuilder builder = new StringBuilder();
		builder.append(before[2]).append(after[0]).append(after[1])
				.append(after[2]);
		return builder.toString();
	}

	/**
	 * 
	 * Title: convertStringDateToFormatString<br/>
	 * Description: 时间字段转换<br/>
	 * 
	 * @param srcDateStr
	 *            源时间字符
	 * @param srcFormat
	 *            源时间格式
	 * @param descFormat
	 *            目标时间格式
	 * @return
	 */
	public static String convertStringDateToFormatString(String srcDateStr,
			String srcFormat, String descFormat) {
		if (srcDateStr == null || "".equals(srcDateStr)) {
			return "";
		} else {
			long longTime = formatDateStringToLong(srcDateStr, srcFormat);
			return formatDateLongToString(longTime, descFormat);
		}
	}

	/**
	 * 将formatStr的时间 转成 long
	 * 
	 * @param date
	 * @return
	 */
	public static Long formatDateStringToLong(String date, String formatStr) {
		SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
		try {
			formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			return formatter.parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0l;
	}

	/**
	 * 将formatStr的时间 转成 long
	 * 
	 * @param date
	 * @return
	 */
	public static Date formatDateStringToDate(String date, String formatStr) {
		SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
		try {
			formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			return formatter.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 计算时间间隔
	 * 
	 * @param lastEndTime
	 *            上一次结束时间
	 * @param currentBeginTime
	 *            这一次开始时间
	 * @param timeFormatStr
	 *            时间格式
	 * @return
	 */
	public static long getIntervalSecond(String lastEndTime,
			String currentBeginTime, String timeFormatStr) {
		long dlast, dbegin, dInterval;
		SimpleDateFormat df = new SimpleDateFormat(timeFormatStr);
		try {
			dlast = df.parse(lastEndTime).getTime();
			dbegin = df.parse(currentBeginTime).getTime();
			dInterval = Math.abs((dbegin - dlast) / 1000);// 时间间隔
		} catch (Exception e) {
			dInterval = 0;
			e.printStackTrace();
		}
		return dInterval;
	}

	/**
	 * 根据秒数，计算时间
	 * 
	 * @param time
	 * @return
	 */
	public String computSecond(long time) {
		long minute = time % 3600 / 60;// 分
		long second = time % 60;// 秒
		return String.valueOf((minute < 10 ? ("0" + minute) : minute) + ":"
				+ (second < 10 ? ("0" + second) : second));
	}

	/**
	 * 根据秒数，计算时间
	 * 
	 * @param time
	 * @return
	 */
	public String computSecondToFullTime(long time) {
		long hour = time / 3600;// 时
		long minute = time % 3600 / 60;// 分
		long second = time % 60;// 秒
		return String.valueOf((hour < 10 ? ("0" + hour) : hour) + ":"
				+ (minute < 10 ? ("0" + minute) : minute) + ":"
				+ (second < 10 ? ("0" + second) : second));
	}

	/**
	 * 对日期进行计算
	 * 
	 * @param date
	 *            日期
	 * @param unit
	 *            计算单位
	 * @param offset
	 *            偏移量
	 * @return
	 */
	public static String calculateDate(String date, String unit, int offset) {
		// 设置日期格式
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		Date beginDt = null;
		try {
			beginDt = sdf.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// 设置计算方式
		int type;
		if (unit.equals(DateUtil.YEAR)) {
			type = Calendar.YEAR;
		} else if (unit.equals(DateUtil.MONTH)) {
			type = Calendar.MONTH;
		} else if (unit.equals(DateUtil.DAY)) {
			type = Calendar.DAY_OF_YEAR;
		} else {
			return "";
		}

		// 计算日期
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTime(beginDt);
		rightNow.add(type, offset);

		Date endDt = rightNow.getTime();
		String reStr = sdf.format(endDt);
		return reStr;
	}

	/**
	 * 
	 * Title: calculateTime<br/>
	 * Description: 对时间进行计算<br/>
	 * 
	 * @param time
	 *            时间
	 * @param unit
	 *            单位
	 * @param offset
	 *            偏移量
	 * @return
	 */
	public static String calculateTime(String time, String unit, int offset) {
		// 设置日期格式
		SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");

		Date beginTm = null;
		try {
			beginTm = sdf.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// 设置计算方式
		int type;
		if (unit.equals(DateUtil.HOUR)) {
			type = Calendar.HOUR_OF_DAY;
		} else if (unit.equals(DateUtil.MINUTE)) {
			type = Calendar.MINUTE;
		} else if (unit.equals(DateUtil.SECOND)) {
			type = Calendar.SECOND;
		} else {
			return "";
		}

		// 计算日期
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTime(beginTm);
		rightNow.add(type, offset);

		Date endTm = rightNow.getTime();
		String reStr = sdf.format(endTm);
		return reStr;
	}

	/**
	 * 
	 * Title: calculateDateTime<br/>
	 * Description: 日期时间计算 <br/>
	 * 
	 * @param dateTime
	 * @param unit
	 * @param offset
	 * @return
	 */
	public static String calculateDateTime(String dateTime, String unit,
			int offset) {
		// 设置日期格式
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		Date beginTm = null;
		try {
			beginTm = sdf.parse(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// 设置计算方式
		int type;
		if (unit.equals(DateUtil.HOUR)) {
			type = Calendar.HOUR_OF_DAY;
		} else if (unit.equals(DateUtil.MINUTE)) {
			type = Calendar.MINUTE;
		} else if (unit.equals(DateUtil.SECOND)) {
			type = Calendar.SECOND;
		} else if (unit.equals(DateUtil.HOUR)) {
			type = Calendar.HOUR_OF_DAY;
		} else if (unit.equals(DateUtil.MINUTE)) {
			type = Calendar.MINUTE;
		} else if (unit.equals(DateUtil.SECOND)) {
			type = Calendar.SECOND;
		} else {
			return "";
		}

		// 计算日期
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTime(beginTm);
		rightNow.add(type, offset);

		Date endTm = rightNow.getTime();
		String reStr = sdf.format(endTm);
		return reStr;
	}

	
	public static Date getNextMonday(int count, Date date) {
		Calendar strDate = Calendar.getInstance();
		strDate.setTime(date);
		strDate.add(Calendar.DATE, count);
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.set(strDate.get(Calendar.YEAR),
				strDate.get(Calendar.MONTH), strDate.get(Calendar.DATE));
		Date monday = currentDate.getTime();
		return monday;
	}


	/**
	 * 获取两个日期相差的周数
	 * 
	 * @param big
	 * @param small
	 * @return
	 */
	public static int getTwoDatesDifOfWeek(Date big, Date small) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(big);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(small);
		if (cal1.get(Calendar.YEAR)!= cal2.get(Calendar.YEAR)) {// 跨年
			Calendar cal3 = Calendar.getInstance();
			cal3.set(Calendar.YEAR, cal2.get(Calendar.YEAR));
			cal3.set(Calendar.MONTH, 11);
			cal3.set(Calendar.DAY_OF_MONTH, 31);
			return cal3.get(Calendar.WEEK_OF_YEAR) - cal2.get(Calendar.WEEK_OF_YEAR)+cal1.get(Calendar.WEEK_OF_YEAR);
		} else {
			return cal1.get(Calendar.WEEK_OF_YEAR)
					- cal2.get(Calendar.WEEK_OF_YEAR);
		}
	}

	public static Date getYearLast(int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DAY_OF_YEAR, -1);
		Date currYearLast = calendar.getTime();
		return currYearLast;
	}

}
