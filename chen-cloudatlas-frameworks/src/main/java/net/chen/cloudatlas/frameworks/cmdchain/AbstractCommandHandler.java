package net.chen.cloudatlas.frameworks.cmdchain;

public abstract class AbstractCommandHandler {

	private AbstractCommandHandler nextCmdHandler;
	
	public final String Handle(AbstractCommandVO vo){
		
		//处理结果
		String result = "";
		
		//判断是否时自己处理的参数
		if (canHandle(vo)){
			result = youHandle(vo);
		} else {
			if (nextCmdHandler != null){
				result = nextCmdHandler.Handle(vo);
			} else {
				result = "命令莫得执行";
			}
		}
		
		return result;
	}
	
	/**
	 * 设置下一个处理者
	 * @param handler
	 */
	public void setNext(AbstractCommandHandler handler){
		nextCmdHandler = handler;
	}
	
	public abstract boolean canHandle(AbstractCommandVO vo);
	
	public abstract String youHandle(AbstractCommandVO vo);
}
