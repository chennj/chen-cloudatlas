package org.chen.cloudatlas.honeybadger.client;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class HttpUtil {

	private static String URL_PATH = "https://www.a89871.com/Tncode.php";

	public HttpUtil(){

	}

	public static void ssl_downloadFile(String fileUrl, String fileLocal) throws Exception {
		
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		/**
		 * 如果有证书则加入下面的code
		 */
		/*
		FileInputStream instream = new FileInputStream(new File("d:\\tomcat.keystore"));
		try {
			// 加载keyStore d:\\tomcat.keystore  
			trustStore.load(instream, "123456".toCharArray());
		} catch (CertificateException e) {
			e.printStackTrace();
		} finally {
			try {
				instream.close();
			} catch (Exception ignore) {
			}
		}
		*/
		
		// 相信自己的CA和所有自签名的证书	
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();

		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(
	          sslContext,
	          new String[]{"TLSv1.2"},
	          null,
	          NoopHostnameVerifier.INSTANCE);

		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
	          .register("https", sslConnectionFactory)
	          .register("http", PlainConnectionSocketFactory.INSTANCE)
	          .build();

		HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);

		CloseableHttpClient httpclient = HttpClientBuilder.create()
	          .setSSLSocketFactory(sslConnectionFactory)
	          .setConnectionManager(ccm)
	          .build();
		
		// 创建http请求(get方式)
		HttpGet httpget = new HttpGet(fileUrl);
		System.out.println("executing request" + httpget.getRequestLine());
		CloseableHttpResponse response = httpclient.execute(httpget);
		try {
			HttpEntity entity = response.getEntity();
			System.out.println("----------------------------------------");
			System.out.println(response.getStatusLine());
			if (entity != null) {
				//System.out.println("Response content length: " + entity.getContentLength());
				//System.out.println(EntityUtils.toString(entity));
				//EntityUtils.consume(entity);
				// 读文件流
				DataInputStream in = new DataInputStream(entity.getContent());
				DataOutputStream out = new DataOutputStream(new FileOutputStream(fileLocal));
				byte[] buffer = new byte[2048];
				int count = 0;
				while ((count = in.read(buffer)) > 0) {
					out.write(buffer, 0, count);
				}
				out.close();
				in.close();
				EntityUtils.consume(entity);
			}
		} finally {
			response.close();
		}
	}
	
	public static void saveImageToDisk(){
		InputStream inputStream=getInputStream();
		byte[] data=new byte[1024];
		int len=0;
		FileOutputStream fileoutputStream =null;
		try{
			fileoutputStream=new FileOutputStream("D:\\vs2015\\GdiplusTest\\Debug\\test3.png");
			while((len=inputStream.read(data))!=-1){
			fileoutputStream.write(data,0,len);
		}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(inputStream!=null){
				try{
					inputStream.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
	}

	/**
	* 获取服务端的数据,以InputStream形式返回
	* @return
	*/
	public static InputStream getInputStream(){
		InputStream inputStream=null;
		HttpURLConnection httpURLConnection=null;
		try{
			URL url=new URL(URL_PATH);
			if(url!=null){
				httpURLConnection =(HttpURLConnection)url.openConnection();
				//设置连接网络超时时间
				httpURLConnection.setConnectTimeout(3000);
				httpURLConnection.setDoInput(true);
				//表示设置本次http请求使用的GET方式请求
				httpURLConnection.setRequestMethod("GET");
				int responseCode=httpURLConnection.getResponseCode();
				if(responseCode==200){
					//从服务器中获得一个输入流
					inputStream=httpURLConnection.getInputStream();
				}
			}
		}catch(MalformedURLException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		return inputStream;
	}

	public static void main(String[] args) throws Exception{
		//从服务器获得图片保存到本地
		//saveImageToDisk();
		ssl_downloadFile(URL_PATH, "D:\\vs2015\\GdiplusTest\\Debug\\test3.png");
	}
}
