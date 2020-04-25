package devutils.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoutSqlParser {
	
	private static Pattern REGEX = Pattern.compile("^IN  (?<bind>:\\w+) => \\? \\[(?:BIGINT|INTEGER|VARCHAR) (?<value>.*)\\]$");

	public static void main(String[] args) throws IOException {
		StringBuilder sql = new StringBuilder();
		Map<String,String> binds = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
			System.out.println("Paste SQL:");
	        String s;
	        do {
	        	s = br.readLine();
	        	if ("*** UNPARSED ***".equals(s)) {
	        		sql.deleteCharAt(sql.length()-1);
	        		continue;
	        	}
	        	
	        	Matcher matcher = REGEX.matcher(s);
	        	if (matcher.matches()) {
	        		binds.put(matcher.group("bind"), matcher.group("value"));
	        		continue;
	        	}
	        	
	        	sql.append(s).append('\n');
	        } while (s != null && !s.isEmpty());
		}
		
		for (Entry<String, String> entry : binds.entrySet()) {
			int i = 0;
			while(true) {
				i = sql.indexOf(entry.getKey(), i);
				if (i == -1) {
					break;
				}
				sql.replace(i, i+entry.getKey().length(), entry.getValue());
			}
		}
		System.out.println(sql.toString());
	}
	
}
