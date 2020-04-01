package wst.net;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.w3c.dom.*;

import javax.xml.parsers.*;

import java.io.*;

import org.xml.sax.InputSource; 
import org.xml.sax.SAXException;

import kr.co.shineware.nlp.komoran.core.analyzer.Komoran;
import kr.co.shineware.util.common.model.Pair;



public class Posting {
	static Komoran komoran = new Komoran(".");	
	
	public enum Portal{
		Naver, Daum
	}
	
	public enum Section{
		Mobile, Total, Blog, Cafe, News, Web, Kin
	}
	
	public enum Order{
		Date, Sim
	}
	
	private static String getSearchListUrl(Portal portal, Section section, String keyword, int start, int display, Order order)
    {
		
		
		try{
			keyword = URLEncoder.encode(keyword, "UTF-8");
		}
		catch(UnsupportedEncodingException e){
			
		}
		
        
        String url = "";
        switch (portal)
        {
            case Naver:
                
                switch (section)
                {
                        
                    case Mobile:                            
                        url = "http://m.search.naver.com/search.naver?where=m&sm=mtp_hty&query=" + keyword;
                        
                        break;
                    case Total:                            
                        url = "https://search.naver.com/search.naver?where=nexearch&query=" + keyword + "&sm=top_hty&fbm=1&ie=utf8";
                        
                        break;
                    case Blog:
                        if (order == Order.Date)
                        {
                            url = "https://openapi.naver.com/v1/search/blog.xml?query=" + keyword + "&display=" + String.valueOf(display) + "&start=" + String.valueOf(start) + "&sort=date";
                        }
                        else
                        {
                            url = "https://openapi.naver.com/v1/search/blog.xml?query=" + keyword + "&display=" + String.valueOf(display) + "&start=" + String.valueOf(start) + "&sort=sim";
                        }
                        
                        
                        break;
                    case Cafe:

                        if (order == Order.Date)
                        {
                            url = "https://openapi.naver.com/v1/search/cafearticle.xml?query=" + keyword + "&display=" + String.valueOf(display) + "&start=" + String.valueOf(start) + "&sort=date";
                        }
                        else
                        {
                            url = "https://openapi.naver.com/v1/search/cafearticle.xml?query=" + keyword + "&display=" + String.valueOf(display) + "&start=" + String.valueOf(start) + "&sort=sim";
                        }

                        break;
                    case News:
                        if (order == Order.Date)
                        {
                            url = "https://openapi.naver.com/v1/search/news.xml?query=" + keyword + "&display=" + String.valueOf(display) + "&start=" + String.valueOf(start) + "&sort=date";
                        }
                        else
                        {
                            url = "https://openapi.naver.com/v1/search/news.xml?query=" + keyword + "&display=" + String.valueOf(display) + "&start=" + String.valueOf(start) + "&sort=sim";
                        }
                        break;
                    case Web:
                        
           
                        
                        break;                            
                    case Kin:
                        if (order == Order.Date)
                        {
                            url = "https://openapi.naver.com/v1/search/kin.xml?query=" + keyword + "&display=" + String.valueOf(display) + "&start=" + String.valueOf(start) + "&sort=date";
                        }
                        else
                        {
                            url = "https://openapi.naver.com/v1/search/kin.xml?query=" + keyword + "&display=" + String.valueOf(display) + "&start=" + String.valueOf(start) + "&sort=sim";
                        }
                        break;   

                }
                break;
            case Daum:
                switch (section)
                {
                    case Mobile:
                        //if (page > 1)
                        //{
                            //url = "https://m.search.daum.net/search?w=fusion&q=" + keyword + "&DA=PGD&p=" + page.ToString() + "&lv=2";
                        //}
                        //else
                        //{
                            url = "https://m.search.daum.net/search?w=tot&q=" + keyword + "&DA=NTB";
                        //}
                        break;
                    case Total:
                        
                        url = "http://search.daum.net/search?w=tot&DA=YZR&t__nil_searchbox=btn&sug=&sugo=&sq=&o=&q=" + keyword + "&tltm=1";
                        
                        break;
                    case Blog:
                        
                        //if (page > 1)
                        //{
                            //url = "http://search.daum.net/search?w=blog&nil_search=btn&DA=PGD&enc=utf8&q=" + keyword + "&page=" + page.ToString() + "&m=board";
                        //}
                        //else
                        //{
                            url = "http://search.daum.net/search?w=blog&nil_search=btn&DA=NTB&enc=utf8&q=" + keyword;
                        //}
                        
                        break;
                    case Cafe:
                        
                        break;
                    case News:
                        
                        break;
                    case Web:
                        
                        break;
                    case Kin:
                        
                        break;
                }
                break;
            default:
                break;
        }
        return url;
    }
	
	public static String getSearchList(Portal portal, Section section, String keyword, int start, int display, Order order, String key, String secret){
		
		StringBuffer sb = new StringBuffer();

		try {
			String url = getSearchListUrl(portal, section, keyword, start, display, order);
			String strResult = HTTP.getNaverOpenAPI(url, "", key, secret);
			
			InputSource   is = new InputSource(new StringReader(strResult)); 

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			
			NodeList nList = doc.getElementsByTagName("link");
			for(int i = 0; i < nList.getLength(); ++i){
				String link = nList.item(i).getFirstChild().getTextContent(); 
				if(link.indexOf("search.naver.com") < 0){
					sb.append(link + "\n");
				}
			}
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return sb.toString();
		
	}
	
	
	
	public static String wordAnalysis(String source){
				

		StringBuffer sb = new StringBuffer(); 
				
		komoran.setUserDic("dic.user");

		try {
			
			List<List<Pair<String,String>>> result = komoran.analyze(source);
			for (List<Pair<String, String>> eojeolResult : result) {
				for (Pair<String, String> wordMorph : eojeolResult) {
					switch(wordMorph.getSecond()){						
						case "SW":
						case "SF":
						case "SS":
						case "SP":
						case "SE":
						case "SN":
						case "SL":
						case "SO":
						case "EC":
						case "ETN":
						case "ETM":
						case "EP":
						case "EF":
						case "MM":
						case "JKB":
						case "JKG":
						case "JKC":
						case "JKD":
						case "JKS":
						case "JKO":
						case "JKQ":
						case "JX":
						case "JC":
						case "XSN":
						case "XSV":						
						case "XSA":
						case "MAG":
						case "MAJ":
						case "NNB":
						case "NP":
						case "NR":
						case "VV":
						case "VX":
						case "VA":	
						case "VCP":
						case "VCN":							
							break;
						default:
							String word = wordMorph.getFirst();
							switch(wordMorph.getSecond()){
								case "VV":
								case "VX":
								case "VA":
								case "VCP":
								case "VCN":
									word = word + "다";
							}
							
							sb.append(word + " ");
							
							break;
					}
					
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return sb.toString();

	}
}
