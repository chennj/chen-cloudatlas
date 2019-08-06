package org.chen.cloudatlas.frameworks.cmdchain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.tinylog.Logger;

public abstract class AbstractCommandVO {

	/**
	 * 定义参数的分隔符
	 */
	public final static String DIVIDE_FALG_REGEX = "\\s+";
	public final static String DIVIDE_FALG = " ";
	
	/**
	 * 定义参数前的符号
	 */
	public final static String PREFIX_REGEX = "\\s+-";
	public final static String PREFIX = "-";
	
	/**
	 * 命令扩展
	 */
	public final static String COMMAND_EXT_GREP = "grep";
	
	/**
	 * 扩展命令分隔符
	 */
	public final static String COMMAND_EXT_DIVIDE = "\\|";
	
	/**
	 * 原始命令行
	 */
	protected String originCommandLine = "";
	
	/**
	 * 命令
	 */
	protected String commandName = "";
	
	protected String commandExtStr = "";
	
	/**
	 * 参数+参数的操作数列表
	 */
	protected Map<String, List<String>> paramMap = new LinkedHashMap<>();
	
	/**
	 * 非参数的操作数列表
	 */
	protected List<String> opList = new ArrayList<>();
	
	public AbstractCommandVO(String commandStr){
		
		parse(commandStr);
	}
	
	protected void parse(String cmdStr){
		
		if (!StringUtils.isEmpty(cmdStr)){
			
			//获取扩展参数
			int extIndex = cmdStr.indexOf(AbstractCommandVO.COMMAND_EXT_GREP);
			if (extIndex>0){
				commandExtStr = cmdStr.substring(extIndex);
				cmdStr = cmdStr.substring(0, extIndex);
			}
			
			//第一个参数时执行命令
			commandName = cmdStr.substring(0,cmdStr.indexOf(AbstractCommandVO.DIVIDE_FALG)).trim();
			
			String commandRest = cmdStr.substring(cmdStr.indexOf(AbstractCommandVO.DIVIDE_FALG)).trim();
			
			//原始命令行
			originCommandLine = commandRest + " " +commandExtStr;
			
			if (StringUtils.isEmpty(commandRest)){
				return ;
			}
			
			//拆分出参数
			String[] complexAry = commandRest.split(AbstractCommandVO.PREFIX_REGEX);
			
			for (int i=0; i<complexAry.length; i++){
				
				String str = complexAry[i].trim();
				if (!str.startsWith(AbstractCommandVO.PREFIX)){
					//没有参数前缀的加入普通操作数列表
					String[] ops = str.split(AbstractCommandVO.DIVIDE_FALG_REGEX);
					opList.addAll(Arrays.asList(ops));
				} else {
					//拆分出参数及参数的操作数
					String[] params = str.split(AbstractCommandVO.DIVIDE_FALG_REGEX);
					ArrayList<String> paramList = new ArrayList<>();
					String pkey = params[0];
					for (int n=1; n<params.length; n++){
						paramList.add(params[n]);
					}
					//params[0]包含前缀符号，作为map的key，
					//key中可以包含参数的第一个操作数
					//例如：-n123 = -n 123
					paramMap.put(pkey, paramList);
				}
			}
		} else {
			Logger.error("没有命令,解析个锤子！");
		}
	}

	public String getCommandName() {
		return commandName;
	}
	
	public Map<String, List<String>> getParamMap(){
		return paramMap;
	}
	
	public List<String> getOpList(){
		return opList;
	}
	
	public void setOpList(List<String> opList){
		this.opList = opList;
	}
	
	public String getOriginCommandLine(){
		return this.originCommandLine;
	}
	
	public String getCommandExtStr(){
		return this.commandExtStr;
	}
}
