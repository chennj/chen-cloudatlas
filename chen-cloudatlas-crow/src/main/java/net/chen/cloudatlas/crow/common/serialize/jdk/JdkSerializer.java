package net.chen.cloudatlas.crow.common.serialize.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.chen.cloudatlas.crow.common.SerializationType;
import net.chen.cloudatlas.crow.common.serialize.ObjectInput;
import net.chen.cloudatlas.crow.common.serialize.ObjectOutput;
import net.chen.cloudatlas.crow.common.serialize.Serializer;

public class JdkSerializer implements Serializer{

	@Override
	public String getName() {
		return SerializationType.JDK.getText();
	}

	@Override
	public ObjectOutput serialize(OutputStream output) throws IOException {
		return new JdkObjectOutput(output);
	}

	@Override
	public ObjectInput deserialize(InputStream input) throws IOException {
		return new JdkObjectInput(input);
	}

}
