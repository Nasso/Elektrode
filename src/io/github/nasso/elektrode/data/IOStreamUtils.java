package io.github.nasso.elektrode.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class IOStreamUtils {
	public static String decodeUTF8(InputStream in) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		
		StringBuilder sb = new StringBuilder();
		
		String line = null;
		while((line = br.readLine()) != null){
			sb.append(line+"\n");
		}
		
		return sb.toString();
	}
	
	public static void encodeUTF8(OutputStream out, String str) throws IOException{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
		
		bw.write(str);
	}
}
