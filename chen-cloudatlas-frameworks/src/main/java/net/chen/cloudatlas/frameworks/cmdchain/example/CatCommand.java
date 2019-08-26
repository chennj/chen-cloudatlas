package net.chen.cloudatlas.frameworks.cmdchain.example;

import net.chen.cloudatlas.frameworks.cmdchain.AbstractCommandHandler;
import net.chen.cloudatlas.frameworks.cmdchain.AbstractCommandSimple;
import net.chen.cloudatlas.frameworks.cmdchain.AbstractCommandVO;

public class CatCommand extends AbstractCommandSimple{

	@Override
	public String execute(AbstractCommandVO vo) {

		AbstractCommandHandler first = super.buildChain(AbstractCatHandler.class).get(0);
		
		return first.Handle(vo);
	}

}
