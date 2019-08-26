package net.chen.cloudatlas.frameworks.cmdchain.example;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import net.chen.cloudatlas.frameworks.cmdchain.AbstractCommandVO;

public class CatDefaultHandler extends AbstractCatHandler{

	public static final List<String> okFileList;
	static {
		okFileList = new ArrayList<String>();
		String[] strs = {"java","class","txt","c","cpp","h","hpp","xml","html"};
		okFileList.addAll(Arrays.asList(strs));
	}
	
	@Override
	public boolean canHandle(AbstractCommandVO vo) {
		String filePath = vo.getOpList().get(0);
		if (filePath.lastIndexOf(".")>0){
			String suffix = filePath.substring(
					filePath.lastIndexOf(".") + 1);
			if (okFileList.contains(suffix.toLowerCase())){
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public String youHandle(AbstractCommandVO vo) {

		String filePath = vo.getOpList().get(0);
		String cmdExtStr = vo.getCommandExtStr();
		String regex = "";
		
		if (!StringUtils.isEmpty(cmdExtStr)){
			String[] exts = cmdExtStr.split("\\|");
			if (exts.length>1)regex = exts[1].trim();
		}
		
		StringBuffer output = new StringBuffer();
		
		try{
			InputStream in = new BufferedInputStream(
					new FileInputStream(filePath));
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			String line = "";
			int lines = 0;
			while((line = reader.readLine())!=null){
				if (match(line,regex)){
					if (MAX_FILE_READ_LINE < lines++)break;
					output.append(lines + ":\t" + line+"\n");
				}
			}
			in.close();
		} catch (IOException e){
			e.printStackTrace();
			return "文件读取错误：" +  e.getMessage();
		}
		return output.toString();
	}

}
