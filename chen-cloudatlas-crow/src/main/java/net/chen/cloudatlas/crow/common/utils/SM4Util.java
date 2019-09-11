package net.chen.cloudatlas.crow.common.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Base64;

public class SM4Util {

	/**
	 * SM4 加密
	 * @param sourceData 明文
	 * @param password 密钥
	 * @return base64加密后的密文
	 * @throws Exception
	 */
	public static byte[] encryptMessageBySM4(byte[] sourceData, String password)throws Exception{
		return Base64.encode(pbeWithSM4Encrypt(true, password, sourceData));
	}
	
	private static byte[] pbeWithSM4Encrypt(boolean isEncrypt, String pwd, byte[] data)  throws Exception{
		
		try {
			byte[] src = pwd.getBytes("UTF8");
			byte[] hash = kdf(src);
			byte[] iv = new byte[16];
			System.arraycopy(hash, 0, iv, 0, 16);
			byte[] key = new byte[16];
			System.arraycopy(hash, 0, key, 0, 16);
			PaddedBufferedBlockCipher cipher = 
					new PaddedBufferedBlockCipher(new CBCBlockCipher(new SM4Engine()), new PKCS7Padding());
			ParametersWithIV params = new ParametersWithIV(new KeyParameter(key),iv);
			cipher.init(isEncrypt, params);
			int len = cipher.getOutputSize(data.length);
			byte[] tmpData = new byte[len];
			int len1 = cipher.processBytes(data, 0, data.length, tmpData, 0);
			int len2 = cipher.doFinal(tmpData, len1);
			int total = len1 + len2;
			if (total < len){
				byte[] removeZeroSourceData = new byte[total];
				System.arraycopy(tmpData, 0, removeZeroSourceData, 0, total);
				return removeZeroSourceData;
			}
			return tmpData;			
		} catch (Exception e){
			throw new Exception("encrypt failure.",e);
		}
	}

	private static byte[] kdf(byte[] src) {
		
		byte[] ct = {0, 0, 0, 1};
		SM3Digest sm3 = new SM3Digest();
		sm3.update(src, 0, src.length);
		sm3.update(ct, 0, ct.length);
		byte[] hash = new byte[32];
		sm3.doFinal(hash, 0);
		return hash;
	}

	/**
	 * SM4解密
	 * @param encryptData
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptMessageBySM4(byte[] encryptData, String password) throws Exception{
		return pbeWithSM4Encrypt(false, password, Base64.decode(encryptData));
	}
	
	/**
	 * SM4 CBC 加密
	 * @param sourceData
	 * @param iv 向量
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptMessageBySM4(byte[] sourceData, byte[] iv, byte[] key) throws Exception{
		return Base64.encode(doSM4WithCBC(true, iv, key, sourceData));
	}
	
	public static void encryptFileBySM4(FileInputStream in, FileOutputStream out, byte[] sm4iv, byte[] sm4key) throws Exception{
		
		byte[] plainTextData = null;
		byte[] encryptTextData = null;
		
		try {
			int dataLen = in.available();
			plainTextData = new byte[dataLen];
			in.read(plainTextData);
			encryptTextData = doSM4WithCBC(true, sm4iv, sm4key, plainTextData);
			out.write(encryptTextData);
		} catch (Exception e){
			throw new Exception("encrypt file failure.", e);
		}
	}
	
	public static void decryptFileBySM4(FileInputStream in, FileOutputStream out, byte[] sm4iv, byte[] sm4key) throws Exception{
		
		byte[] decryptedData = null;
		byte[] encryptedData = null;
		
		try {
			int dataLen = in.available();
			encryptedData = new byte[dataLen];
			in.read(encryptedData);
			decryptedData = doSM4WithCBC(false, sm4iv, sm4key, encryptedData);
			out.write(decryptedData);
		} catch (Exception e){
			throw new Exception("decrypt file failure.", e);
		}
	}
	
	/**
	 * SM4 CBC 解密
	 * @param encryptData
	 * @param iv
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptMessageBySM4(byte[] encryptData, byte[] iv, byte[] key) throws Exception{
		return doSM4WithCBC(false, iv, key, Base64.decode(encryptData));
	}
	
	public static byte[] doSM4WithCBC(boolean isEncrypt, byte[] iv, byte[] key, byte[] data) throws Exception{
		
		if ((iv.length != 16) || (key.length != 16)){
			throw new Exception("iv.length and key.length must be 16 bytes!");
		}
		
		try {
			PaddedBufferedBlockCipher cipher = 
					new PaddedBufferedBlockCipher(new CBCBlockCipher(new SM4Engine()), new PKCS7Padding());
			ParametersWithIV params = new ParametersWithIV(new KeyParameter(key),iv);
			cipher.init(isEncrypt, params);
			int len = cipher.getOutputSize(data.length);
			byte[] tmpData = new byte[len];
			int len1 = cipher.processBytes(data, 0, data.length, tmpData, 0);
			int len2 = cipher.doFinal(tmpData, len1);
			int total = len1 + len2;
			if (total < len){
				byte[] removeZeroSourceData = new byte[total];
				System.arraycopy(tmpData, 0, removeZeroSourceData, 0, total);
				return removeZeroSourceData;
			}
			return tmpData;
		} catch (Exception e){
			throw new Exception("encrypt/decrypt failure.",e);
		}
	}
}
