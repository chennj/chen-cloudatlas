package org.chen.cloudatlas.frameworks.cmdchain;

import java.util.ArrayList;
import java.util.List;

public enum CommandEnum {

	cat("org.chen.cloudatlas.frameworks.cmdchain.example.CatCommand");
	
	private String value="";
	
	private CommandEnum(String value){
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}
	
	public static List<String> getNames(){
		
		CommandEnum[] enums = CommandEnum.values();
		List<String> names = new ArrayList();
		for(CommandEnum c : enums){
			names.add(c.name());
		}
		return names;
	}
}
