package net.chen.cloudatlas.crow.common.serialize.Kryo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.chen.cloudatlas.crow.common.SerializationType;
import net.chen.cloudatlas.crow.common.serialize.ObjectInput;
import net.chen.cloudatlas.crow.common.serialize.ObjectOutput;
import net.chen.cloudatlas.crow.common.serialize.Serializer;

public class KryoSerializer implements Serializer{

	@Override
	public String getName() {
		return SerializationType.KRYO.getText();
	}

	@Override
	public ObjectOutput serialize(OutputStream output) throws IOException {
		return new KryoObjectOutput(output);
	}

	@Override
	public ObjectInput deserialize(InputStream input) throws IOException {
		return new KryoObjectInput(input);
	}

}
