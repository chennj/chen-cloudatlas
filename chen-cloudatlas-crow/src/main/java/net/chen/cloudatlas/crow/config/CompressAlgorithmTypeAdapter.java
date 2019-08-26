package net.chen.cloudatlas.crow.config;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.chen.cloudatlas.crow.common.CompressAlgorithmType;

/**
 * 
 * @author chenn
 *
 */
public class CompressAlgorithmTypeAdapter extends XmlAdapter<String, CompressAlgorithmType>{

	@Override
	public String marshal(CompressAlgorithmType value) throws Exception {
		if (null == value)
			return null;
		return value.getText();
	}

	@Override
	public CompressAlgorithmType unmarshal(String value) throws Exception {
		if (null == value)
			return null;
		
		return CompressAlgorithmType.fromString(value);
	}

}
