package net.chen.cloudatlas.crow.common.serialize;

import java.io.IOException;

public interface ObjectInput extends DataInput{

	Object readObject() throws IOException, ClassNotFoundException;
	
	<T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException;
}
