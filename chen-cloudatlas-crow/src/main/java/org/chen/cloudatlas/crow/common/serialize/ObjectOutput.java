package org.chen.cloudatlas.crow.common.serialize;

import java.io.IOException;

public interface ObjectOutput extends DataOutput{

	void writeObject(Object object) throws IOException;
}
