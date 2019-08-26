package net.chen.cloudatlas.crow.common.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 正整数
 * @author chenn
 *
 */
public class AtomicPositiveInteger extends Number{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2543178976252143268L;

	private final AtomicInteger i;
	
	public AtomicPositiveInteger(){
		i = new AtomicInteger();
	}
	
	public AtomicPositiveInteger(int initialValue){
		i = new AtomicInteger(initialValue);
	}
	
	/**
	 * 1.取得当前值
	 * 2.计算+1后的值
	 * 3.如果当前值没有被覆盖的话设置那个+1后的值
	 * 4.如果设置没成功, 返回+1前的值
	 * @return
	 */
	public final int getAndIncrement(){
		
		for(;;){
			int current = i.get();
			int next = current >= Integer.MAX_VALUE ? 0 : current + 1;
			if (i.compareAndSet(current, next)){
				return current;
			}
		}
	}
	
	public final int getAndDecrement(){
		
		for(;;){
			int current = i.get();
			int next = current <= 0 ? Integer.MAX_VALUE : current - 1;
			if (i.compareAndSet(current, next)){
				return current;
			}
		}
	}
	
	/**
	 * 1.取得当前值
	 * 2.计算+1后的值
	 * 3.如果当前值没有被覆盖的话设置那个+1后的值
	 * 4.如果设置没成功, 返回+1后的值
	 * 
	 * @return
	 */
	public final int incrementAndGet() {  
        for (;;) {  
            int current = i.get();  
            int next = current >= Integer.MAX_VALUE ? 0 : current + 1;
            if (i.compareAndSet(current, next))  
                return next;  
        }  
    }  
	
	public final int decrementAndGet() {
		for(;;){
			int current = i.get();
			int next = current <=0 ? Integer.MAX_VALUE : current -1 ;
			if (i.compareAndSet(current, next)){
				return next;
			}
		}
	}
	
	public final int get(){
		return i.get();
	}
	
	public final void set(int newValue){
		if (newValue < 0){
			throw new IllegalArgumentException("new value" + newValue + " < 0");
		}
		i.set(newValue);
	}
	
	public final int getAndSet(int newValue){
		if (newValue < 0){
			throw new IllegalArgumentException("new Value " + newValue + " < 0");
		}
		return i.getAndSet(newValue);
	}
	
	public final int getAndAdd(int delta){
		if (delta < 0){
			throw new IllegalArgumentException("delta " + delta + " < 0");
		}
		for(;;){
			int current = i.get();
			int next = current >= Integer.MAX_VALUE - delta + 1 ? delta - 1 : current + delta;
			if (i.compareAndSet(current, next)){
				return current;
			}
		}
	}
	
	public final int addAndGet(int delta){
		if (delta < 0){
			throw new IllegalArgumentException("delta " + delta + " < 0");
		}
		for(;;){
			int current = i.get();
			int next = current >= Integer.MAX_VALUE - delta + 1 ? delta - 1 : current + delta;
			if (i.compareAndSet(current, next)){
				return next;
			}
		}
	}
	
	public final boolean compareAndSet(int expect, int update){
		if (update < 0){
			throw new IllegalArgumentException("update value " + update + " < 0");
		}
		return i.compareAndSet(expect, update);
	}
	
	public final boolean weakCompareAndSet(int expect, int update){
		if (update < 0){
			throw new IllegalArgumentException("update value " + update + " < 0");
		}
		return i.weakCompareAndSet(expect, update);
	}
	
	@Override
	public double doubleValue() {	
		return i.byteValue();
	}

	@Override
	public float floatValue() {
		return i.floatValue();
	}

	@Override
	public int intValue() {
		return i.intValue();
	}

	@Override
	public long longValue() {
		return i.longValue();
	}

	@Override
	public String toString(){
		return i.toString();
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (i == null ? 0 : i.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		AtomicPositiveInteger other = (AtomicPositiveInteger)obj;
		if (i==null && other.i != null){
			return false;
		} else if (!i.equals(other.i)){
			return false;
		}
		
		return true;
	}
}
