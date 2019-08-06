package org.chen.cloudatlas.crow.remote.codec.crow;

import java.util.HashMap;
import java.util.Map;

import javax.print.DocFlavor.URL;

import org.chen.cloudatlas.crow.common.Constants;
import org.chen.cloudatlas.crow.common.ThrottleType;

public enum CrowCodecVersion {

	V10((byte)0x01,(byte)0x00, "1.0"),

	V20((byte)0x02,(byte)0x00, "2.0");
	
	private final byte majorByte;
	private final byte minorByte;
	private final String version;
	
	private CrowCodecVersion(final byte majorByte, final byte minorByte, final String version){
		this.majorByte = majorByte;
		this.minorByte = minorByte;
		this.version = version;
	}
	
	private static final Map<String, CrowCodecVersion> STRING_T0_ENUM = new HashMap<String, CrowCodecVersion>();
	static{
		for(CrowCodecVersion one : values()){
			STRING_T0_ENUM.put(one.version,one);
		}
	}
	public byte getMajorByte() {
		return majorByte;
	}
	public byte getMinorByte() {
		return minorByte;
	}
	public String getVersion() {
		return version;
	}

	public static CrowCodecVersion getDefault(){
		return V10;
	}
	
	public static CrowCodecVersion getCodecVersion(URL url){
		
		CrowCodecVersion ver = null;
		if (url != null){
			String verstr = url.getParameter(Constants.PROTOCOL_VERSION);
			if (verstr != null){
				ver = STRING_T0_ENUM.get(verstr);
			}
		}
		return ver == null ? CrowCodecVersion.getDefault() : ver;
	}
	
	public static CrowCodecVersion getCodecVersion(String protocolVersion){
		
		CrowCodecVersion ver = null;
		if (protocolVersion != null){
			ver = STRING_T0_ENUM.get(protocolVersion);
		}
		return ver == null ? CrowCodecVersion.getDefault() : ver;
	}
}
