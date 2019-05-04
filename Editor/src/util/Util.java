package util;

public class Util {
	public static boolean nonEmpty(String s) {
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) != ' ')
				return true;
		return false;
	}

}
