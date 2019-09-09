package net.chen.cloudatlas.crow.common.serialize.hessian2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.chen.cloudatlas.crow.common.SerializationType;
import net.chen.cloudatlas.crow.common.serialize.ObjectInput;
import net.chen.cloudatlas.crow.common.serialize.ObjectOutput;
import net.chen.cloudatlas.crow.common.serialize.Serializer;

public class Hessian2Serializer implements Serializer{

	@Override
	public String getName() {
		return SerializationType.HESSIAN2.getText();
	}

	@Override
	public ObjectOutput serialize(OutputStream output) throws IOException {
		return new Hessian2ObjectOutput(output);
	}

	@Override
	public ObjectInput deserialize(InputStream input) throws IOException {
		return new Hessian2ObjectInput(input);
	}

}
