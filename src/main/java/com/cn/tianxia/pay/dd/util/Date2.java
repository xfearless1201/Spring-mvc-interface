package com.cn.tianxia.pay.dd.util;

/**
 * <p>Title: </p>
 * <p>Description:取得系统当前时间和格式化时间字符串 </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 */
/**
 <p>** <p>调用</p>                                                       </p>
 <p>*例1：取得系统当前时间YYYYMMDDhhmmss                                 </p>
 <p>**  FormatDate.getDateTime());                                       </p>
 <p>*                                                                    </p>
 <p>*例2：取得系统当前时间hhmmss                                         </p>
 <p>**  FormatDate.getTime();                                            </p>
 <p>*                                                                    </p>
 <p>*例3：取得系统当前时间YYYYMMDD                                       </p>
 <p>**  FormatDate.getDate();                                            </p>
 <p>*                                                                    </p>
 <p>*例4：格式化时间 如:YYYYMMDDhhmmss 格式化为YYYY-MM-DD hh:mm:ss;      </p>
 <p>*	第一个参数代表：需格式化的字符串；                               </p>
 <p>*	第二个参数代表：格式化的分隔符；可以用“-” or "/";                </
 <p>**  FormatDate.formatDateTime(getDateTime(),"-");                    </p>
 <p>*                                                                    </p>
 <p>*例5：格式化时间 如:YYYYMMDD 格式化为YYYY-MM-DD ;                    </p>
 <p>**  FormatDate.formatDate(getDateTime(),"-"));                       </p>
 <p>*                                                                    </p>
 <p>*例6：格式化时间 如:hhmmss 格式化为hh:mm:ss;                         </p>
 <p>**  FormatDate.formatTime(getDateTime());                            </p>
 **/

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;


public class Date2 
{
	/**
	 * 按指定格式取系统当前时间
	 * 
	 * @param format
	 *            String
	 * @return String
	 */
	public static String getDateTime(String format) {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
		String strDate = sdf.format(new java.util.Date());

		return strDate.toString();
	}
	

	/**
	 * 按默认格式取系统当前时间，　默认格式为：yyyyMMddHHmmss
	 * 
	 * @return String
	 */
	public static String getDateTime() {
		return getDateTime("yyyyMMddHHmmss");
	}

	// 取得系统当前时间,yyyyMMdd
	public static String getDate() {
		return getDateTime("yyyyMMdd");
	}

	// 取得系统当前时间，HHmmss
	public static String getTime() {
		return getDateTime("HHmmss");
	}

	/**
	 * 是否为合法的日期字符串
	 * 
	 * @param strDate
	 *            String
	 * @param pattern
	 *            String
	 * @return boolean
	 */
	public static boolean isValidDate(String strDate, String format) {
		boolean islegal = false;

		try {
			String newDate = date2Str(str2Date(strDate, format), format);

			if (newDate.equals(strDate)) {
				islegal = true;
			}
		} catch (Exception e) {
		}

		return islegal;
	}

	/*
	 * /** 把时间转换为字串 格式：yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date 待转换的时间
	 * 
	 * @return
	 */
	public static String date2Str(Date date) {
		String format = "yyyy-MM-dd HH:mm:ss";
		return date2Str(date, format);
	}

	/**
	 * 把时间转换为字串
	 * 
	 * @param date
	 *            待转换的时间
	 * @param format
	 *            转换格式
	 * @return
	 */
	public static String date2Str(Date date, String format) {
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(date);
	}

	/**
	 * 把时间格式由一种格式转换为另一种格式为字串, 旧格式默认为 "yyyyMMddHHmmss"
	 * 
	 * @param dateStr
	 *            String 日期字符串
	 * @param newFormat
	 *            String 新格式
	 * @throws ParseException
	 * @return String
	 */
	public static String formatDateTime(String dateStr, String newFormat) {
		return formatDateTime(dateStr, newFormat, "yyyyMMddHHmmss");
	}

	/**
	 * 把时间格式由一种格式转换为另一种格式为字串
	 * 
	 * @param dateStr
	 *            String 日期字符串
	 * @param newFormat
	 *            String 新格式
	 * @param oldFormat
	 *            String 旧格式
	 * @throws ParseException
	 * @return String
	 */
	public static String formatDateTime(String dateStr, String newFormat,
			String oldFormat) {
		Date date = str2Date(dateStr, oldFormat);
		return date2Str(date, newFormat);
	}

	/**
	 * 把字串转换为日期
	 * 
	 * @param sdate
	 *            字串形式的日期
	 * @param format
	 *            字串格式
	 * @return 转换为日期类型
	 * @throws ParseException
	 */
	public static Date str2Date(String sDate, String format) {
		try {
			return (new SimpleDateFormat(format)).parse(sDate);
		} catch (ParseException ex) {
			return null;
		}
	}

	/**
	 * 取某一日期增减 n 值后的日期, n 由 dateField 决定是年、月、日 根据增加or减少的时间得到新的日期
	 * 
	 * @param date
	 *            Date 参照日期
	 * @param counts
	 *            int 增减的数值
	 * @param dateField
	 *            int 需操作的日期字段, 取值范围如下 Calendar.YEAY 年 Calendar.MONTH 月
	 *            Calendar.DATE 日 .... Calendar.SECOND 秒
	 * @return Date
	 */
	public static Date addDate(Date date, int counts, int dateField) {
		GregorianCalendar curGc = new GregorianCalendar();

		if (date != null)
			curGc.setTime(date);

		curGc.add(dateField, counts);

		return curGc.getTime();
	}

	/**
	 * 将日期增减 n 天
	 * 
	 * @param date
	 *            Date 参照日期,如果为null则取当前日期
	 * @param days
	 *            int 增减的天数
	 * @return Date
	 */
	public static Date addDate(Date date, int days) {
		return addDate(date, days, Calendar.DATE);
	}

	/**
	 * 将字符串型日期增减 n 天
	 * 
	 * @param date
	 *            String
	 * @param days
	 *            int
	 * @param format
	 *            String
	 * @return String
	 */
	public static String addDate(String date, int days, String format) {
		return date2Str(addDate(str2Date(date, format), days, Calendar.DATE),
				format);
	}
	
	/**
	 * 将日期增加 n 个月
	 * 
	 * @param date
	 *            Date
	 * @param months
	 *            int
	 * @return Date
	 */
	public static Date addMonth(Date date, int months) {
		return addDate(date, months, Calendar.MONTH);
	}

	/**
	 * 将字符串型日期增加 n 个月
	 * 
	 * @param date
	 *            String
	 * @param months
	 *            int
	 * @param format
	 *            String
	 * @return String
	 */
	public static String addMonth(String date, int months, String format) {
		return date2Str(
				addDate(str2Date(date, format), months, Calendar.MONTH), format);
	}
	
	/**
	 * 将日期增加 n 年
	 * 
	 * @param date Date
	 * @param years int
	 * @return Date
	 */
	public static Date addYear(Date date, int years) {
		return addDate(date, years, Calendar.YEAR);
	}
	/**
	 * 将字符串型日期增加 n 年
	 * @param date String
	 * @param years int
	 * @param format String
	 * @return String
	 */
	public static String addYear(String date, int years, String format)
	{
		return date2Str(addDate(str2Date(date, format), years, Calendar.YEAR), format);
	}
	
	/**
	 * 将日期增加 n 小时
	 * @param date Date
	 * @param hour int
	 * @return Date
	 */
	public static Date addHour(Date date, int hour)
	{
		return addDate(date, hour, Calendar.HOUR);
	}
	/**
	 * 将字符串型日期增加 n 小时
	 * @param date String
	 * @param hour int
	 * @param format String
	 * @return String
	 */
	public static String addHour(String date, int hour, String format)
	{
		return date2Str(addDate(str2Date(date, format), hour, Calendar.HOUR), format);
	}
	
	/**
	 * 将日期增加 n 分钟
	 * @param date Date
	 * @param minute int
	 * @return Date
	 */
	public static Date addMinute(Date date, int minute)
	{
		return addDate(date, minute, Calendar.MINUTE);
	}
	/**
	 * 将字符串型日期增加 n 分钟
	 * @param date String
	 * @param minute int
	 * @param format String
	 * @return String
	 */
	public static String addMinute(String date, int minute, String format)
	{
		return date2Str(addDate(str2Date(date, format), minute, Calendar.MINUTE), format);
	}
	
	/**
	 * 将日期增加 n 秒钟
	 * @param date Date
	 * @param second int
	 * @return Date
	 */
	public static Date addSecond(Date date, int second)
	{
		return addDate(date, second, Calendar.SECOND);
	}
	/**
	 * 将字符串型日期增加 n 秒钟
	 * @param date String
	 * @param second int
	 * @param format String
	 * @return String
	 */
	public static String addSecond(String date, int second, String format)
	{
		return date2Str(addDate(str2Date(date, format), second, Calendar.SECOND), format);
	}
	
	/**
	 * 取得月最后一天 先取得下月月首,再减一,得月末
	 * 
	 * @param sSource
	 *            String
	 * @return String
	 */
	public static String lastDateOfMonth(String date) {
		return date2Str(
				addDate(addDate(
						str2Date(date.substring(0, 6) + "01", "yyyyMMdd"), 1,
						Calendar.MONTH), -1, Calendar.DATE), "yyyyMMdd");
	}

	/**
	 * 得到星期
	 * 
	 * @param date
	 *            String
	 * @return String
	 */
	public static String getWeekDay(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("E");
		return formatter.format(str2Date(date, "yyyyMMdd"));
	}

	/**
	 * 得到星期几的数字，
	 * 
	 * @param date
	 *            String
	 * @return int
	 */
	public static int getWeekDayNum(String date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(Date2.str2Date(date, "yyyyMMdd"));
		// 天 一  二 三  四 五  六
		// 7 1 2 3 4 5 6
		int dayOfWeek = cal.get(cal.DAY_OF_WEEK) - 1;
		if (dayOfWeek == 0)
			dayOfWeek = 7;
		return dayOfWeek;
	}
	
	/**
	 * 获得加減几个工作日后的日期，
	 * 
	 * @param date String
	 * @param days int
	 * @return String
	 */
	public static String addWorkDay(String date, int days, String newFormat) 
	{
		String dateStr = "";
		int tWeek = getWeekDayNum(date);
		
		BigDecimal bdObj = new BigDecimal(days/5);
		
		int tDays = 0;
		int tWeeks = bdObj.setScale(0, BigDecimal.ROUND_HALF_DOWN).intValue();
		int tResidue = days % 5;
		
		if(days > 0)
		{
			//向后加N个工作日
			if((tResidue + tWeek) > 5)
			{
				tWeeks = tWeeks + 1; 
			}
		}
		else if(days < 0)
		{
			//向前加N个工作日
			if((tResidue + tWeek) <= 0)
			{
				tWeeks = tWeeks - 1; 
			}
		}
		
		if(tWeek > 5)
		{
			tDays = tWeeks * 2 + days + 5 - tWeek; //date为周末的算法
		}
		else
		{
			tDays = tWeeks * 2 + days; //date为周一 ~ 周五的算法
		}
		dateStr = addDate(date, tDays, newFormat);
		
		return dateStr;
	}
	
    /** 
     * 获取当月第一天 
     *  默认格式yyyyMMdd
     * @return 
     */
    public static String getFirstDayOfMonth()
    {  
        return getFirstDayOfMonth("yyyyMMdd");  
    }
    public static String getFirstDayOfMonth(String newFormat) 
    {  
        String str = "";  
        SimpleDateFormat sdf = new SimpleDateFormat(newFormat);  
      
        Calendar lastDate = Calendar.getInstance();  
        lastDate.set(Calendar.DATE, 1);// 设为当前月的1号  
        str = sdf.format(lastDate.getTime());  
        return str;  
    }
    
    /**
	 * 将日期时间字符串根据转换为指定时区的日期时间.
	 *
	 * @param srcFormater     待转化的日期时间的格式.
	 * @param srcDateTime     待转化的日期时间.
	 * @param srcTimeZoneID   待转化的时区编号.     Asia/Shanghai
	 * @param dstFormater     目标的日期时间的格式.
	 * @param dstTimeZoneID   目标的时区编号.       America/New_York
	 * @return 转化后的日期时间.
	 */
	public static String str2Timezone(String srcDateTime, String srcFormater, String srcTimeZoneID, String dstFormater, String dstTimeZoneID)
	{
		TimeZone srcTimeZone = TimeZone.getTimeZone(srcTimeZoneID);
		TimeZone dstTimeZone = TimeZone.getTimeZone(dstTimeZoneID);
		SimpleDateFormat inputFormat = new SimpleDateFormat(srcFormater);
		inputFormat.setTimeZone(srcTimeZone);
		Date date = null;
		try
		{
			date = inputFormat.parse(srcDateTime);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		SimpleDateFormat outputFormat = new SimpleDateFormat(dstFormater);
		outputFormat.setTimeZone(dstTimeZone);
		return outputFormat.format(date);
	}

	/**
	 *
	 * @param date
	 * @param dstFormater
	 * @param dstTimeZoneID
	 * @return
	 */
	public static String date2Timezone(Date date, String dstFormater, String dstTimeZoneID)
	{
		TimeZone dstTimeZone = TimeZone.getTimeZone(dstTimeZoneID);
		SimpleDateFormat outputFormat = new SimpleDateFormat(dstFormater);
		outputFormat.setTimeZone(dstTimeZone);
		return outputFormat.format(date);
	}
    
	public static void main(String[] args) {
		long baseTime = System.currentTimeMillis()-10*60*1000;
		
		System.out.println(Date2.date2Str(new Date(baseTime)));
	}
}
