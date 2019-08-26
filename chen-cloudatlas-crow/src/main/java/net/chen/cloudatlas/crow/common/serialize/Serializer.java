package net.chen.cloudatlas.crow.common.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.chen.cloudatlas.crow.common.NameableService;

public interface Serializer extends NameableService{

	ObjectOutput serialize(OutputStream output) throws IOException;
	
	ObjectInput deserialize(InputStream input) throws IOException;
}
