package org.saipal.fmisutil.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateUtil {
	private static List<Map<Integer, DateMeta>> dateMeta = new ArrayList<>();
	String dateMetaData = "20001#19434#4:30:14:18:1943:5,20002#19435#6:32:14:19:1943:6,20003#19436#3:31:15:17:1943:7,20004#19437#6:32:16:17:1943:8,20005#19438#3:31:17:16:1943:9,20006#19439#6:30:17:15:1943:10,20007#194310#1:30:17:16:1943:11,20008#194311#3:30:16:16:1943:12,20009#194312#5:29:16:17:1944:1,200010#19441#6:30:14:19:1944:2,200011#19442#1:29:13:18:1944:3,200012#19443#2:31:13:20:1944:4,20011#19444#5:31:13:19:1944:5,20012#19445#1:31:14:19:1944:6,20013#19446#4:32:14:18:1944:7,20014#19447#1:31:16:17:1944:8,20015#19448#4:31:16:17:1944:9,20016#19449#0:31:16:16:1944:10,20017#194410#3:30:17:16:1944:11,20018#194411#5:29:16:16:1944:12,20019#194412#6:30:15:18:1945:1,200110#19451#1:29:14:19:1945:2,200111#19452#2:30:12:18:1945:3,200112#19453#4:30:14:19:1945:4,20021#19454#6:31:13:19:1945:5,20022#19455#2:31:14:19:1945:6,20023#19456#5:32:14:18:1945:7,20024#19457#2:32:16:17:1945:8,20025#19458#5:31:17:16:1945:9,20026#19459#2:30:17:15:1945:10,20027#194510#4:30:17:16:1945:11,20028#194511#6:29:16:16:1945:12,20029#194512#0:30:15:18:1946:1,200210#194";

	public DateUtil() {
		Map<Integer, DateMeta> nepMeta = new HashMap<>();
		Map<Integer, DateMeta> engMeta = new HashMap<>();
		String[] dateMetaInfo = dateMetaData.split(",");
		for (int i = 0; i < dateMetaInfo.length; i++) {
			String[] keyAndData = dateMetaInfo[i].split("#");
			int engKey = Integer.getInteger(keyAndData[1]);
			int nepKey = Integer.getInteger(keyAndData[0]);
			String[] dt = keyAndData[2].split(":");
			DateMeta dm = new DateMeta();
			dm.years = Integer.parseInt((nepKey + "").substring(0, 4));
			dm.months = Integer.parseInt((nepKey + "").substring(4));
			dm.engyear = Integer.parseInt((engKey + "").substring(0, 4));
			dm.engmonth = Integer.parseInt((engKey + "").substring(4));
			dm.weekDays = Integer.getInteger(dt[0]);
			dm.maxDay = Integer.getInteger(dt[1]);
			dm.engDay = Integer.getInteger(dt[2]);
			dm.transitNepDay = Integer.getInteger(dt[3]);
			dm.transitYear = Integer.getInteger(dt[4]);
			dm.transitMonth = Integer.getInteger(dt[5]);
			
			nepMeta.put(nepKey, dm);
			engMeta.put(engKey, dm);
		}
		dateMeta.add(nepMeta);
		dateMeta.add(engMeta);
	}

	public static String getNepDate() {
		return getNepDate(null);
	}

	public static String getNepDate(String engDate) {
		if (engDate == null) {
			engDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
		}
		if (engDate.isBlank()) {
			engDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		LocalDate date1 = LocalDate.parse(engDate, formatter);

		int pramyear = date1.getYear();
		int prammonth = date1.getMonthValue();
		int pramday = date1.getDayOfMonth();
		int flag = 0;
		String Keys = pramyear + "" + prammonth;
		DateMeta dm = dateMeta.get(1).get(Keys);
		if ((dm.maxDay - dm.transitNepDay) + 1 < pramday) {
			prammonth = prammonth + 1;
			if (prammonth > 12) {
				prammonth = 1;
				pramyear = pramyear + 1;
			}
			Keys = pramyear + "" + prammonth;
			dm = dateMeta.get(1).get(Keys);
			flag = 1;
		}

		String ret = dm.years + "/" + fixZero(dm.months) + "/";

		if (flag == 1) {
			flag = (pramday - dm.engDay) + 1;
		} else {
			flag = (dm.transitNepDay + pramday - 1);
		}
		return ret + fixZero(flag);
	}
	
	public static String getEngDate() {
		return getEngDate(null);
	}

	public static String getEngDate(String nepDate) {
		if (nepDate == null) {
			nepDate = DateUtil.getNepDate();
		}
		if (nepDate.isBlank()) {
			nepDate = DateUtil.getNepDate();
		}
		String[] npDate = nepDate.split("/");
		int Ny = Integer.parseInt(npDate[0]);
		int Nm = Integer.parseInt(npDate[1]);
		int Nd = Integer.parseInt(npDate[2]);
		String Keys = Ny + "" + Nm;
		DateMeta dm = dateMeta.get(0).get(Keys);
		String Ry = "";
		String Rm = "";
		String Rd = "";
		if (dm.transitNepDay <= Nd) {
			Rm = "" + dm.transitMonth;
			Rd = "" + (1 + Nd - dm.transitNepDay);
			Ry = "" + dm.transitYear;
		} else {
			Rm = "" + dm.engmonth;
			Rd = "" + (dm.engDay + Nd - 1);
			Ry = "" + dm.engyear;
		}
		return fixZero(Integer.parseInt(Rm)) + "/" + fixZero(Integer.parseInt(Rd)) + "/"
				+ fixZero(Integer.parseInt(Ry));
	}
	
	public int getFiscalYearId(String date) {
		if(date==null) {
			date = getNepDate();
		}
		String[] npDate = date.split("/");
		int Ny = Integer.parseInt(npDate[0]);
		int Nm = Integer.parseInt(npDate[1]);
		if(Nm > 3) {
			return Ny;
		}
		return Ny-1;
	}
	
	public String getFiscalYear(String date) {
		if(date==null) {
			date = getNepDate();
		}
		String[] npDate = date.split("/");
		int Ny = Integer.parseInt(npDate[0]);
		int Nm = Integer.parseInt(npDate[1]);
		if(Nm > 3) {
			return Ny+"/"+(Ny+1);
		}
		return (Ny-1)+"/"+Ny;
	}
	
	public int nepDateStamp(String date) {
		if(date==null) {
			date = getNepDate();
		}
		return Integer.parseInt(date.replace("/",""));
	}

	private static String fixZero(int number) {
		if (number < 10) {
			return "0" + number;
		}
		return number + "";
	}
}

class DateMeta {
	public int years;
	public int months;
	public int engyear;
	public int engmonth;
	public int weekDays;
	public int maxDay;
	public int engDay;
	public int transitNepDay;
	public int transitYear;
	public int transitMonth;
}
