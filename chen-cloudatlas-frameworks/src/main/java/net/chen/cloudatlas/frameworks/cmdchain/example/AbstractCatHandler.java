package net.chen.cloudatlas.frameworks.cmdchain.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import net.chen.cloudatlas.frameworks.cmdchain.AbstractCommandHandler;

public abstract class AbstractCatHandler extends AbstractCommandHandler{

	public final Integer MAX_FILE_READ_LINE = 10000;
	
	public final String PARAM_N = "-n";
	
	protected boolean match(String line, String regex){
		
		boolean hasRegex = !StringUtils.isEmpty(regex);
		Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
		boolean match = false;
		if (hasRegex){
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()){
				match = true;
			}
		} else {
			match = true;
		}
		return match;
	}

}
