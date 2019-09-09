package net.chen.cloudatlas.crow.common.serialize.Kryo;

import java.io.IOException;
import java.io.OutputStream;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import net.chen.cloudatlas.crow.common.serialize.ObjectOutput;

public class KryoObjectOutput implements ObjectOutput{

	private com.esotericsoftware.kryo.io.KryoObjectOutput out;
	
	private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<>();
	
	private static Kryo getKryo(){
		Kryo ko = kryoThreadLocal.get();
		if (null==ko){
			ko = new Kryo();
			((Kryo.DefaultInstantiatorStrategy)ko.getInstantiatorStrategy())
			.setFallbackInstantiatorStrategy(
					new StdInstantiatorStrategy());
			ko.addDefaultSerializer(Throwable.class, new JavaSerializer());
			kryoThreadLocal.set(ko);
		}
		return ko;
	}
	
	public KryoObjectOutput(OutputStream os){
		this.out = new com.esotericsoftware.kryo.io.KryoObjectOutput(getKryo(), new Output(os));
	}
	
	@Override
	public void writeBool(boolean v) throws IOException {
		out.writeBoolean(v);
	}

	@Override
	public void writeByte(byte v) throws IOException {
		out.writeByte(v);
	}

	@Override
	public void writeShort(short v) throws IOException {
		out.writeShort(v);
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
		out.writeFloat(v);
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
		
		if (null==v){
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
