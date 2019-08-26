package net.chen.cloudatlas.frameworks.cmdchain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 抽象命令
 * 解析命令，建立命令族（责任链）
 * @author chenn
 *
 */
public abstract class AbstractCommand {

	public abstract String execute(AbstractCommandVO vo);
	
	private IClassReflect reflectUtil = initReflectUtil();

	public abstract IClassReflect initReflectUtil();
	
	protected final List<AbstractCommandHandler> buildChain(Class<? extends AbstractCommandHandler> cmdHandler){
		
		//获取命令名所有子类
		Set<Class<? extends AbstractCommandHandler>> classes = reflectUtil.getSonClass(cmdHandler);
		
		//存放命令处理实例，建立链表
		List<AbstractCommandHandler> commandHandlerList = new ArrayList();
		for (Class cls : classes){
			AbstractCommandHandler handler = null;
			//构建实例
			try{
				handler = (AbstractCommandHandler)Class.forName(cls.getName()).newInstance();
			} catch (Exception e){
				e.printStackTrace();
			}
			//建立链表
			if (commandHandlerList.size() > 0){
				commandHandlerList.get(commandHandlerList.size() - 1).setNext(handler);
			}
			commandHandlerList.add(handler);
		}
		
		return commandHandlerList;
	}
}
