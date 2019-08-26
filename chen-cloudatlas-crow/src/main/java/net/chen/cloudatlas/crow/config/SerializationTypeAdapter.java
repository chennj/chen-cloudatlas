package net.chen.cloudatlas.crow.config;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.chen.cloudatlas.crow.common.SerializationType;

public class SerializationTypeAdapter extends XmlAdapter<String, SerializationType>{

	@Override
	public String marshal(SerializationType value) throws Exception {
		if (null == value)
			return null;
		
		return value.getText();
	}

	@Override
	public SerializationType unmarshal(String value) throws Exception {
		if (null == value)
			return null;
		
		return SerializationType.fromString(value);
	}

}
