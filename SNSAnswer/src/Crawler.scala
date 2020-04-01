
import wst.net.HTTP
import wst.net.CookieManager
import wst.net.Posting
import scala.util.control.Breaks._ 
import scala.util.matching.Regex
import org.apache.commons.lang3.StringEscapeUtils
import java.io._
import java.util.Calendar
import java.text.SimpleDateFormat


/**
 * @author whitesun
 */
class Crawler { 
 
  def searchNaverBlog(keyword: String, start:Int, display:Int) = {
    import wst.net.Posting._
    val temp = Posting.getSearchList(Posting.Portal.Naver, Posting.Section.Blog, keyword, start, display, Posting.Order.Date , "X_Ygu1xiH1J4eJEtsBtu", "RufbKJASkr")
    val lines = temp.split("\n")    
    
    var cm = new CookieManager;
    var collect = 0;
    for(i <- 0 to lines.length - 1) {
      try{
        collect += crawlingBlog(lines, i, cm)
      }catch{
        case ioe : StringIndexOutOfBoundsException
          => println("Index out of bounds " + lines(i))
      }
      
    }
    
    collect.toString()
    
    //url + "\t" + title + "\t" + postDate + "\t" + content

    
  }

  def crawlingBlog(lines: Array[String], i: Int, cm: wst.net.CookieManager) = {
    var strResult = HTTP.getResponse(lines(i), "", cm, "", "UTF-8")
    var result = 0
    var title = ""
    var postDate = ""
    var url = ""
    var content = ""
    if (strResult.indexOf("blog.naver") > 0)
    {
      url = "http://blog.naver.com" + strResult.substring(strResult.indexOf("/PostView.nhn?blogId"));
      url = url.substring(0, url.indexOf("\""));
      strResult = HTTP.getResponse(url, "", cm, "", "EUC-KR");
      title = getPhrase(strResult, "<meta property=\"og:title\" content=\"", "\"").replace("\r", "").replace("\n", "").replace("\t", "")
      if(strResult.indexOf("_postAddDate\">") > 0){
          postDate = getPhrase(strResult, "_postAddDate\">", "<")
      }else{
          postDate = getPhrase(strResult, "<span class=\"se_publishDate pcol2 fil5\">", "<")
      }
      
      postDate = postDate.replace(".", "-").trim().substring(0, 10); 
      
      if (strResult.indexOf("<!-- SE3-TEXT { -->") > 0)
      {
        content = getPhrase(strResult, "<!-- SE3-TEXT { -->", "<!-- } SE3-TEXT -->").replace("\r", "").replace("\n", "").replace("\t", "")
      }
      else
      {
        content = getPhrase(strResult, "<div id=\"postViewArea\">", "<div class=\"post_footer_contents\">").replace("\r", "").replace("\n", "").replace("\t", "")
      }      
      
      title = title.replaceAll("  ", " ")
      title = title.replaceAll("""<.*?>""", "")
      title = StringEscapeUtils.UNESCAPE_HTML4.translate(title).replace("\t", " ").replace("\r", "").replace("\n", "");
      title = title.replaceAll("  ", " ")
      
      content = content.replaceAll("  ", " ")
      content = content.replaceAll("""<.*?>""", "")
      content = StringEscapeUtils.UNESCAPE_HTML4.translate(content).replace("\t", " ").replace("\r", "").replace("\n", "");
      content = content.replaceAll("  ", " ")
      
      
      saveCollect(url, title, postDate, content)
      result = 1
    
    }
    else if (strResult.indexOf("tistory.com") > 0)
    {
        if(strResult.indexOf("<meta name=\"title\" content=\"") > 0){
          title = getPhrase(strResult, "<meta name=\"title\" content=\"", "\"").replace("\t", " ").replace("\r", "").replace("\n", "");
        }else{
          title = getPhrase(strResult, "<title>", "<").replace("\t", " ").replace("\r", "").replace("\n", "");
        }
                
        url = getPhrase(strResult, "<meta property=\"og:url\" content=\"", "\"");
        if(strResult.indexOf("<div class=\"article\">") > 0){
          content = getPhrase(strResult, "<div class=\"article\">", "<div class=\"another_category").replace("\t", " ").replace("\r", "").replace("\n", "");  
        }else{
          content = getPhrase(strResult, "<div class=\"entry-content\">", "<div class=\"entry").replace("\t", " ").replace("\r", "").replace("\n", "");
        }
        
        postDate = postDate.replace(".", "-").trim().substring(0, 10); 

        title = title.replaceAll("  ", " ")
        title = title.replaceAll("""<.*?>""", "")
        title = StringEscapeUtils.UNESCAPE_HTML4.translate(title).replace("\t", " ").replace("\r", "").replace("\n", "");
        title = title.replaceAll("  ", " ")
        
        content = content.replaceAll("  ", " ")
        content = content.replaceAll("""<.*?>""", "")
        content = StringEscapeUtils.UNESCAPE_HTML4.translate(content).replace("\t", " ").replace("\r", "").replace("\n", "");
        content = content.replaceAll("  ", " ")
        
        if (strResult.indexOf("<div class=\"date\"") > 0)
        {
            postDate = getPhrase(strResult, "<div class=\"date\"", "<");
        }
        else
        {
          
          val today = Calendar.getInstance().getTime()
          val dateFormat = new SimpleDateFormat("yyyy-MM-dd.")
          
            postDate = dateFormat.format(today)
        }
        

    
        
        saveCollect(url, title, postDate, content)
        result = 1
    
        //txtResult.Text = txtResult.Text + line +  "\r\n";
        
    }
    
    /*
    else if(strResult.indexOf("blog.daum") > 0)
    {
      
      url = "http://blog.daum.com" + strResult.substring(strResult.indexOf("/_blog/BlogTypeView"));
      url = StringEscapeUtils.UNESCAPE_HTML4.translate(url.substring(0, url.indexOf("\"")));
      strResult = HTTP.getResponse(url, "", cm, "", "UTF-8");
      title = getPhrase(strResult, "<meta name=\"title\" content=\"", "\"").replace("\t", " ").replace("\r", "").replace("\n", "");
        
      println(strResult)
      /*
      title = getPhrase(strResult, "<meta property=\"og:title\" content=\"", "\"").replace("\r", "").replace("\n", "").replace("\t", "")
      if(strResult.indexOf("_postAddDate\">") > 0){
          postDate = getPhrase(strResult, "_postAddDate\">", "<")
      }else{
          postDate = getPhrase(strResult, "<span class=\"se_publishDate pcol2 fil5\">", "<")
      }
      
      if (strResult.indexOf("<!-- SE3-TEXT { -->") > 0)
      {
        content = getPhrase(strResult, "<!-- SE3-TEXT { -->", "<!-- } SE3-TEXT -->").replace("\r", "").replace("\n", "").replace("\t", "")
      }
      else
      {
        content = getPhrase(strResult, "<div id=\"postViewArea\">", "<div class=\"post_footer_contents\">").replace("\r", "").replace("\n", "").replace("\t", "")
      }
      
      content = content.replaceAll("  ", " ")
      content = content.replaceAll("""<.*?>""", "")
      content = StringEscapeUtils.UNESCAPE_HTML4.translate(content)
      */
      //saveCollect(url, title, postDate, content)
    
    }
    * 
    */
    else{
       //println(lines(i));
        //txtResult.Text = txtResult.Text + list[i] + "\r\n";
    }  
    
    result
  }
  
  def getPhrase(source:String, begin:String, end:String) = {
    var result = source.substring(source.indexOf(begin));
    result = result.replace(begin, "");
    result = result.substring(0, result.indexOf(end));
    result
  }
  
  def clearCollect(){
    val writer = new PrintWriter(new File("collect.txt")) 
    writer.write("")
    writer.close()
  }
  
  def saveCollect(url:String, title:String, postDate:String, content:String){
    var c2 = content.replace("모바일에서 작성된 글입니다.", "")
    c2 = c2.replace("블로그앱 설치 URL을네이버앱 알림으로 전송했습니다.", "")
    c2 = c2.replace("알림이 오지 않는다면,네이버앱을 최신버전으로 업데이트 하거나,로그아웃상태인지 확인해주세요", "")    
      val writer = new PrintWriter(new FileOutputStream(new File("collect.txt"),true)) 
      writer.write(url + "\t" + title + "\t" + postDate + "\t" + c2 + "\n")
      writer.close()
  }
}