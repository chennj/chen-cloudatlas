package org.chen.cloudatlas.frameworks.cmdchain;

/**
 * 命令调度器
 * @author chenn
 *
 */
public class CommandInvoker {

	public String exec(String cmd){
		
		String result = "";
		
		//解析命令
		AbstractCommandVO vo = new CommandVO(cmd);
		
		//检查是否支持该命令
		if (CommandEnum.getNames().contains(vo.getCommandName())){
			//构建命令对象
			String clsName = CommandEnum.valueOf(vo.getCommandName()).getValue();
			AbstractCommand command;
			try{
				command = (AbstractCommand)Class.forName(clsName).newInstance();
				result = command.execute(vo);
			} catch (Exception e){
				result = e.getMessage();
			}
		} else {
			result = "命令不存在，换个";		
		}
		
		return result;
	}
	
	public static void main(String[] args) throws Exception{
		
		String cmd = "cat C:/Users/chenn/Desktop/润和/银联项目/自助入网项目/cj/cjcommon/src/main/java/com/unionpay/crossjoin/common/ac/AC.java";
		cmd = "cat C:/MySQL/mysql-5.6.24-winx64/data/master-bin.000001";
		String result = new CommandInvoker().exec(cmd);
		System.out.println(result);
	}
}
