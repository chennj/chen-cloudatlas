package net.chen.cloudatlas.crow.common.serialize.hessian2;

import java.io.IOException;
import java.io.InputStream;

import com.caucho.hessian.io.HessianInput;

import net.chen.cloudatlas.crow.common.serialize.ObjectInput;

public class Hessian2ObjectInput implements ObjectInput{

	private HessianInput input;
	
	public Hessian2ObjectInput(InputStream in){
		input = new HessianInput(in);
	}
	
	@Override
	public boolean readBool() throws IOException {
		return input.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return (byte)input.readInt();
	}

	@Override
	public short readShort() throws IOException {
		return (short)input.readInt();
	}

	@Override
	public int readInt() throws IOException {
		return input.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return input.readLong();
	}

	@Override
	public float readFloat() throws IOException {
		return (float)input.readDouble();
	}

	@Override
	public double readDouble() throws IOException {
		return input.readDouble();
	}

	@Override
	public String readUTF() throws IOException {
		return input.readString();
	}

	@Override
	public byte[] readBytes() throws IOException {
		return input.readBytes();
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {
		return input.readObject();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
		return (T)input.readObject(cls);
	}

}
