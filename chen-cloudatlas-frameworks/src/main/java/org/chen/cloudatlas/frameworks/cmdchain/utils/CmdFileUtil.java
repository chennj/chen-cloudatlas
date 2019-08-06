package org.chen.cloudatlas.frameworks.cmdchain.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class CmdFileUtil {

	public static int getFileLines(String filePath){
		
		int lines = -1;
		try{
			File file = new File(filePath);
			if (file.exists()){
				long fileLength = file.length();
				LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
				lineNumberReader.skip(fileLength);
				lines = lineNumberReader.getLineNumber();
				lineNumberReader.close();
			}
		} catch (IOException e){
			e.printStackTrace();
		}
		return lines;
	}
	
	public static int getFileLines(File file){
		
		int lines = -1;
		try{
			if (file.exists()){
				long fileLength = file.length();
				LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
				lineNumberReader.skip(fileLength);
				lines = lineNumberReader.getLineNumber();
				lineNumberReader.close();
			}
		} catch (IOException e){
			e.printStackTrace();
		}
		return lines;
	}
}
