package net.chen.cloudatlas.crow.common.serialize.hessian2;

import java.io.IOException;
import java.io.OutputStream;

import com.caucho.hessian.io.HessianOutput;

import net.chen.cloudatlas.crow.common.serialize.ObjectOutput;

public class Hessian2ObjectOutput implements ObjectOutput{

	private HessianOutput out;
	
	public Hessian2ObjectOutput(OutputStream os){
		this.out = new HessianOutput(os);
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
		out.writeString(v);
	}

	@Override
	public void writeBytes(byte[] v) throws IOException {
		out.writeBytes(v);
	}

	@Override
	public void writeBytes(byte[] v, int offset, int len) throws IOException {
		out.writeBytes(v, offset, len);
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
