package net.chen.cloudatlas.crow.common.serialize.Kryo;

import java.io.IOException;
import java.io.InputStream;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import net.chen.cloudatlas.crow.common.serialize.ObjectInput;

public class KryoObjectInput implements ObjectInput{
	
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
	
	private com.esotericsoftware.kryo.io.KryoObjectInput in;
	
	public KryoObjectInput(InputStream is){
		this.in = new com.esotericsoftware.kryo.io.KryoObjectInput(getKryo(), new Input(is));
	}
	
	@Override
	public boolean readBool() throws IOException {
		return in.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return in.readByte();
	}

	@Override
	public short readShort() throws IOException {
		return in.readShort();
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
		return in.readFloat();
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
		if (len<0){
			return null;
		} else if (len==0){
			return new byte[]{};
		} else {
			byte[] bs = new byte[len];
			in.readFully(bs);
			return bs;
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
