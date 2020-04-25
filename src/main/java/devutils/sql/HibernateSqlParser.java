package devutils.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HibernateSqlParser {
	
	private static enum ParamType {
		VARCHAR,
		BOOLEAN,
		TIMESTAMP
	}
	
	private static class Param {
		// Thu Jan 01 01:00:00 CET 1970
		private DateFormat LOG_DATEFORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		// TIMESTAMP '1970-01-01 00:00'
		private DateFormat PSQL_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		ParamType type;
		String value;
		public Param(ParamType type, String value) {
			super();
			this.type = type;
			this.value = value;
		}
		
		public String getQuerySql() throws ParseException {
			String result;
			switch(type) {
			case BOOLEAN:
				result = value;
				break;
			case TIMESTAMP:
				Date date = LOG_DATEFORMAT.parse(value);
				result = "TIMESTAMP '"+PSQL_DATEFORMAT.format(date)+"'";
				break;
			case VARCHAR:
				result = '\''+value+'\'';
				break;
			default:
				result = null;
				break;
			}
			return result;
		}
		
		@Override
		public String toString() {
			return type+":"+value;
		}
	}
	// binding parameter [1] as [VARCHAR] - [READ]
	private static Pattern REGEX = Pattern.compile("^binding parameter \\[(?<bind>\\d+)\\] as \\[(?<type>[A-Z]+)\\] - \\[(?<value>.*)\\]$");

	public static void main(String[] args) throws IOException, ParseException {
		StringBuilder sql = new StringBuilder();
		Map<Integer,Param> binds = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
			System.out.println("Paste SQL:");
	        String s;
	        do {
	        	s = br.readLine();
	        	if (s.startsWith("select ") && sql.length() == 0 && binds.isEmpty()) {
	        		sql.append(s);
	        		continue;
	        	}
	        	
	        	Matcher matcher = REGEX.matcher(s);
	        	if (matcher.matches()) {
	        		String bind = matcher.group("bind");
	        		String type = matcher.group("type");
	        		String value = matcher.group("value");
	        		Param param = new Param(ParamType.valueOf(type), value);
	        		binds.put(Integer.valueOf(bind), param);
	        		continue;
	        	}
	        	
	        } while (s != null && !s.isEmpty());
		}
		
		Param param = null;
		int i = 1, idx = 0;
		while(true) {
			param = binds.get(i);
			idx = sql.indexOf("?", idx+1);
			
			if (param == null || idx == -1) {
				break;
			}
			
			String replacement = param.getQuerySql();
			System.out.printf("replacing bind %d with %s.\n", i, replacement);
			sql.replace(idx, idx+1, "/*?" + i + "*/" + replacement);
			idx += replacement.length();
			i++;
		}
		
		System.out.println(sql.toString());
	}
}
