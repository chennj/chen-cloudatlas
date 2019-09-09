package net.chen.cloudatlas.crow.common;

import java.util.Enumeration;
import java.util.jar.Manifest;

import org.tinylog.Logger;

import java.net.URL;

/**
 * @author chenn
 *
 */
public class Version {

	public static final String KEY_RELEASE_NAME = "crow_release_name";
	
	public static final String KEY_RELEASE_VERSION = "crow_release_version";
	
	public static final String NAME;
	
	public static final String VERSION;
	
	static {
		String unknown = "Unknown";
		String releaseName = unknown;
		String releaseVersion = unknown;
		try {
			Enumeration<URL> resources = Version.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
			while(resources.hasMoreElements()){
				
				URL url = resources.nextElement();
				Manifest manifest = new Manifest(url.openStream());
				
				String tmpReleaseName = manifest.getMainAttributes().getValue(KEY_RELEASE_NAME);
				String tmpReleaseVersion = manifest.getMainAttributes().getValue(KEY_RELEASE_VERSION);
				
				if (null!=tmpReleaseName && null!=tmpReleaseVersion){
					Logger.trace("found:"+url);
					releaseName = tmpReleaseName;
					releaseVersion = tmpReleaseVersion;
					break;
				}
			}
		} catch (Exception e){
			Logger.trace("IO error.",e);
		}
		
		NAME = releaseName;
		VERSION = releaseVersion;
	}
	
	private Version(){
		
	}

	public static String getPrettyString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("\n").append("=======================").append("\n");
		sb.append(String.format("%s-%s", NAME,VERSION)).append("\n");
		sb.append("==============================\n");
		return sb.toString();
	}
	
	
}
