package io.github.nasso.elektrode.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class IOStreamUtils {
	public static byte[] download(URL url) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream in = url.openStream();
		
		byte[] buffer = new byte[4096];
		
		int readed = 0;
		while((readed = in.read(buffer)) > 0){
			baos.write(buffer, 0, readed);
		}
		
		in.close();
		
		return baos.toByteArray();
	}
	
	public static String decodeUTF8(InputStream in) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		
		StringBuilder sb = new StringBuilder();
		
		String line = br.readLine();
		while(line != null){
			sb.append(line);
			
			line = br.readLine();
			if(line != null){
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}
	
	public static void encodeUTF8(OutputStream out, String str) throws IOException{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
		
		bw.write(str);
	}
}
