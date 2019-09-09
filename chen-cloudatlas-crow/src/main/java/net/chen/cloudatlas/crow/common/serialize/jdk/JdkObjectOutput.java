package net.chen.cloudatlas.crow.common.serialize.jdk;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.tinylog.Logger;

import net.chen.cloudatlas.crow.common.serialize.ObjectOutput;

public class JdkObjectOutput implements ObjectOutput{

	private ObjectOutputStream out;
	
	public JdkObjectOutput(OutputStream os){
		try {
			this.out = new ObjectOutputStream(os);
		} catch (IOException e) {
			Logger.error("error occurs while new JdkObjectOut ",e);
		}
	}
	
	@Override
	public void writeBool(boolean v) throws IOException {
		out.writeBoolean(v);
	}

	@Override
	public void writeByte(byte v) throws IOException {
		out.writeInt(v);
	}

	@Override
	public void writeShort(short v) throws IOException {
		out.writeInt(v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		out.writeInt(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		out.writeLong(v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		out.writeDouble(v);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		out.writeDouble(v);
	}

	@Override
	public void writeUTF(String v) throws IOException {
		out.writeUTF(v);
	}

	@Override
	public void writeBytes(byte[] v) throws IOException {
		
		if (null == v){
			out.writeInt(-1);
		} else {
			writeBytes(v, 0, v.length);
		}
	}

	@Override
	public void writeBytes(byte[] v, int offset, int len) throws IOException {
		
		if (null==v){
			out.writeInt(-1);
		} else {
			out.writeInt(len);
			out.write(v, offset, len);
		}
	}

	@Override
	public void flushBuffer() throws IOException {
		out.flush();
	}

	@Override
	public void writeObject(Object object) throws IOException {
		out.writeObject(object);
	}

}
