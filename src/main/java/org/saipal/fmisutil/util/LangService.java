package org.saipal.fmisutil.util;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.saipal.fmisutil.auth.Authenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LangService {

	@Autowired
	Authenticated auth;
	
	private static final Logger LOG = LoggerFactory.getLogger(LangService.class);

	public String tField(String field) {
		return field + auth.getLang();
	}

	public String tToNep(String number) {
		String tNumber = "";
		if (number == null)
			return tNumber;
		String[] nepNumbers = { "०", "१", "२", "३", "४", "५", "६", "७", "८", "९" };
		List<String> list = Arrays.asList(nepNumbers);
		char[] numChars = number.toCharArray();
		for (char i : numChars) {
			try {
				tNumber += list.get(Integer.parseInt(i + ""));
			} catch (Exception e) {
				tNumber += i + "";
			}
		}
		return tNumber;
	}

	public String tToEng(String number) {
		String tNumber = "";
		if (number == null)
			return tNumber;
		String[] nepNumbers = { "०", "१", "२", "३", "४", "५", "६", "७", "८", "९" };
		List<String> list = Arrays.asList(nepNumbers);
		char[] numChars = number.toCharArray();
		for (char i : numChars) {
			int sNum = list.indexOf(i + "");
			if (sNum >= 0) {
				tNumber += sNum;
			} else {
				tNumber += i + "";
			}
		}
		return tNumber;
	}

	public String tNum(String number) {
		return tNum(number, auth.getLang());
	}

	public String tNum(String number, String language) {
		if ("".equals(number)) {
			return "";
		}
		if (language.equalsIgnoreCase("Np")) {
			return this.tToNep(number);
		} else {
			return this.tToEng(number);
		}
	}

	public String truncateNum(double num, int precision) {
		long res = (long) (num * Math.pow(10, precision));
		String hash = "#.";
		for (int i = 1; i <= precision; i++) {
			hash += "#";
		}
		DecimalFormat df = new DecimalFormat(hash);
		return df.format(res / Math.pow(10, precision));

	}

	/**
	 * returns fixed point decimal precision of given number
	 * 
	 * @param num       number to be converted
	 * @param precision precision required
	 * @return fixed point decimal precision of number
	 */
	public String fNum(double num, int precision) {
		long res = (long) (num * Math.pow(10, precision));
		String hash = "#.";
		for (int i = 1; i <= precision; i++) {
			hash += "#";
		}
		DecimalFormat df = new DecimalFormat(hash);
		return tNum(df.format(res / Math.pow(10, precision)));

	}

	/**
	 * returns decimal precision upto two digits of given number
	 * 
	 * @param num number to be converted
	 * @return number with decimal upto two digits( no decimal if number doesnot
	 *         originally has precision)
	 */
	public String fNum(double num) {
		return fNum(num, 2);
	}

	public String formatNepFigure(String number) {
		String res = "";
		String[] numList = number.split("\\.");
		String num = numList[0];
		if (num.length() > 3) {
			String convertString = num.substring(0, num.length() - 3);
			res = insertPeriodically(convertString, ",", 2);
			res += "," + num.substring(num.length() - 3);
		} else {
			res = num;
		}
		return res + ((numList.length > 1 && numList[1] != null) ? ("." + numList[1]) : "");
	}

	public String dumbMoney(String number, int figureIn, String language) {

		if (number != null && !number.isEmpty()) {
			// if(isNumeric(number+""))
			// number = df2.format(number);
			String sign = "";
			String pre = "(", post = ")";
			if (number.charAt(0) == '-') {
				sign = "-";
				number = number.substring(1);
			}
			String[] numList = number.split("\\.");
			String num = numList[0];
			String num1 = "";
			if (Double.parseDouble(numList[1]) > 0)
				num1 = "." + numList[1].substring(0, 2);

			boolean isNepali = language.equalsIgnoreCase("Np");
			String res = "";

			if (isNepali) {
				if (figureIn >= 1000) {
					res += insertPeriodically(num, ",", 2);
				} else {
					if (num.length() > 3) {
						String convertString = num.substring(0, num.length() - 3);
						res = insertPeriodically(convertString, ",", 2);
						res += "," + num.substring(num.length() - 3);
						res += num1;
					} else {
						res = num + num1;
					}
				}
			} else {
				res = insertPeriodically(num, ",", 3);
				res += num1;
			}

			/*
			 * if (numList.length == 2 && !numList[1].isEmpty()) { res = res; + "." +
			 * numList[1]; }
			 */
			if (sign.isEmpty()) {
				return tNum(res, language);
			} else {
				return pre + tNum(res, language) + post;
			}
		}
		return "";
	}

	public String dumbMoney(String number, int figureIn) {
		return dumbMoney(number, figureIn, auth.getLang());
	}

	public String money(String number) {
		if (number != null && !number.isEmpty()) {

			String num = String.format("%.3f", Double.parseDouble(number));
			// String num="";
			// if (!number.isEmpty() && isNumeric(number))
			// num = df2.format(number);

			return dumbMoney(num, 0);
		}
		return "";
	}

	public String money(double number) {
		String num = String.format("%.3f", number);
		// String num = df2.format(number);
		return dumbMoney(num, 0);
	}

	public String money(String number, int figurein) {
		if (number != null && !number.isBlank()) {
			/*
			 * String hash = "";
			 * 
			 * for (int i = 0; i < precision; i++) { hash += "#"; } hash = hash.isEmpty() ?
			 * "#" : "#." + hash; DecimalFormat df = new DecimalFormat(hash); String num1 =
			 * df.format(Double.parseDouble(number));
			 */
			// String num1 = "";

			String num1 = String.format("%f", Double.parseDouble(number));
			return dumbMoney(num1, figurein);
		} else {
			return "";
		}
	}

	public String money(double number, String figurein) {
		if (number != 0) {
			/*
			 * String hash = "";
			 * 
			 * for (int i = 0; i < precision; i++) { hash += "#"; } hash = hash.isEmpty() ?
			 * "#" : "#." + hash; DecimalFormat df = new DecimalFormat(hash); String num1 =
			 * df.format(number);
			 */
			int figin = Integer.parseInt(figurein);
			// String num1 = Math.ceil(number)+"";
			String num1 = String.format("%.3f", number);
			return dumbMoney(num1, figin);
		} else {
			return "";
		}
	}

	public String money(double number, int figurein) {
		if (number != 0) {
			/*
			 * String hash = "";
			 * 
			 * for (int i = 0; i < precision; i++) { hash += "#"; } hash = hash.isEmpty() ?
			 * "#" : "#." + hash; DecimalFormat df = new DecimalFormat(hash); String num1 =
			 * df.format(number);
			 */
			String num1 = String.format("%.3f", number);
			// String num1 = df2.format(number);
			return dumbMoney(num1, figurein);
		} else {
			return "";
		}
	}

	public static String insertPeriodically(String text, String insert, int period) {
		StringBuilder builder = new StringBuilder(text.length() + insert.length() * (text.length() / period) + 1);
		text = new StringBuffer(text).reverse().toString();
		int index = 0;
		String prefix = "";
		while (index < text.length()) {
			// Don't put the insert in the very first iteration.
			// This is easier than appending it *after* each substring
			builder.append(prefix);
			prefix = insert;
			builder.append(text.substring(index, Math.min(index + period, text.length())));
			index += period;
		}
		return builder.reverse().toString();
	}

}