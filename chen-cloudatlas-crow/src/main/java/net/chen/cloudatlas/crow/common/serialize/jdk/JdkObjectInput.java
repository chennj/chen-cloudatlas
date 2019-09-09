package net.chen.cloudatlas.crow.common.serialize.jdk;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.serialize.ObjectInput;

public class JdkObjectInput implements ObjectInput{

	private ObjectInputStream in;
	
	public JdkObjectInput(InputStream is){
		try {
			this.in = new ObjectInputStream(in);
		} catch (IOException e) {
			Logger.error("error occurs while new JdkObjectInput ",e);
		}
	}
	
	@Override
	public boolean readBool() throws IOException {
		return in.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return (byte)in.readInt();
	}

	@Override
	public short readShort() throws IOException {
		return (short)in.readInt();
	}

	@Override
	public int readInt() throws IOException {
		return in.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return in.readLong();
	}

	@Override
	public float readFloat() throws IOException {
		return (float)in.readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return in.readDouble();
	}

	@Override
	public String readUTF() throws IOException {
		return in.readUTF();
	}

	@Override
	public byte[] readBytes() throws IOException {

		int len = in.readInt();
		if (len < 0){
			return null;
		} else if (len==0){
			return new byte[]{};
		} else {
			byte[] result = new byte[len];
			in.readFully(result);
			return result;
		}
		
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {
		return in.readObject();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
		return (T) in.readObject();
	}

}
