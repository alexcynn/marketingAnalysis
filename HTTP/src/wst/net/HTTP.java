package wst.net;

import java.net.*;
import java.io.*;

public class HTTP {
	public static String getResponse(String url, String refer, CookieManager cm, String data, String encoding){
		String result = "";
		
		try{
			
			URL obj = new URL(url);		
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty("User-Agent", "Mozilla/4.0");
			con.setRequestProperty("Referer", refer);
			
			// optional default is GET
			if(data.trim().length() == 0){
				con.setRequestMethod("GET");
			}else{
				con.setRequestMethod("POST");
				
				// Send post request
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.writeBytes(data);
				wr.flush();
				wr.close();
			}
			
			
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);		

			int responseCode = con.getResponseCode();
			String inputLine;
			StringBuffer response = new StringBuffer();

			
			
			String headerType = con.getContentType();  
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(),encoding));
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			
			in.close();			
			result = response.toString();
	

			
		}catch(MalformedURLException e){
			
		}catch(IOException e){
		
		}
		
		
		//result = response.toString();
		
		return result;
			
	}
	
	public static String getNaverOpenAPI(String url, String refer, String clientId, String secret)
	{
		String result = "";
		
		try{
			
			URL obj = new URL(url);		
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty("User-Agent", "curl/7.43.0");
			con.setRequestProperty("Content-Type", "application/xml");
			con.setRequestProperty("X-Naver-Client-Id", clientId);
			con.setRequestProperty("X-Naver-Client-Secret", secret);
		
			con.setRequestMethod("GET");
		
		
		
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);		
		
			int responseCode = con.getResponseCode();
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			
			
			String headerType = con.getContentType();  
			BufferedReader in;
			in = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			result = response.toString();
				
			
		}catch(MalformedURLException e){
			
		}catch(IOException e){
		
		}
	
		return result;

   
	}

	

}

