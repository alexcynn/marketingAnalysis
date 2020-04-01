import wst.net.CookieManager;
import scala.collection.mutable.MutableList
import scala.collection.mutable.StringBuilder
import java.io._

/**
 * @author whitesun
 */
object SNSAnswer {
  def main(args: Array[String]): Unit = {
    
    val c = new Crawler;
    val cm = new CookieManager();
    val analysis = new Analysis;
    /*
    c.clearCollect()
    println("crawling started")
    val keyword = "80D"
    for(i <- 1 to 10){
      println(c.searchNaverBlog(keyword,1 + (i - 1) * 100,100) + " crawled");  
    }
    println("crawling finished")
    
    */
    analysis.loadEmtionWord()
    AnalysisEmotionNWordMorph(analysis)    
    
    val source = scala.io.Source.fromFile("words.txt")
    val articles = try source.mkString finally source.close()
    val words = analysis.wordRanking(articles)    
     println(words.length)
    val sb = new StringBuilder 
    sb.append("[")
    sb.append(wordRanking(articles, words(0)))
    sb.append(wordRanking(articles, words(1)))
    sb.append(wordRanking(articles, words(2)))
    sb.append(wordRanking(articles, words(3)))
    sb.append(wordRanking(articles, words(4)))
    sb.append(wordRanking(articles, words(5)))
    var temp = sb.toString()
    temp = temp.substring(0, temp.length() - 1) + "]";                  
    val writer = new PrintWriter(new File("/var/www/html/SNS/ranking.csv")) 
    writer.write(temp)
    writer.close()
    println(temp)
    println("단어빈도분석 완료")
    
    analysis.associationAnaylisys()
    
    println("finished")
    
  }

  def wordRanking(articles: String, word: (String, Int)) = {
    val sb = new StringBuilder
    val article = articles.split('\n')
    val wordList:MutableList[Pair[String, Int]] = MutableList()    
    for(i <- 0 to article.length - 1){
      if(article(i).split('\t').length >= 2){
        val date = article(i).split('\t')(1)
        
        if(article(i).indexOf(word._1) > 0){
          var exist = false
          for(j <- 0 to wordList.size - 1){
            if(wordList(j)._1 == date){
              wordList(j) = (wordList(j)._1, wordList(j)._2 + 1)                
              exist = true;
            }
          }
          if(exist == false){
            val p =  (date, 1)
            wordList += p
          }
        }
      }
    }
    
    
    for(i <- 0 to wordList.size - 1){
      sb.append("{\"count\":" + wordList(i)._2.toString() +",\"date\":\"" + wordList(i)._1 + "\",\"channel\":\"blog\",\"key\":\"" + word + "\"},")
    }
    sb.toString()    
  }
  
  
  def AnalysisEmotionNWordMorph(analysis: Analysis) = {
    val source = scala.io.Source.fromFile("collect.txt")
    val articles = try source.mkString finally source.close()
    val article = articles.split('\n')
    val emotionPlus:MutableList[Pair[String, Int]] = MutableList()
    val emotionMinus:MutableList[Pair[String,Int]] = MutableList()    
    for(i <- 0 to article.length - 1){
      val result = analysis.getEmotionPoint(article(i))
      val date = article(i).split('\t')(2)
      if(result > 0){
        var exist = false
        for(j <- 0 to emotionPlus.size - 1){
          if(emotionPlus(j)._1 == date){
            emotionPlus(j) = (emotionPlus(j)._1, emotionPlus(j)._2 + 1)                
            exist = true;
          }
        }
        if(exist == false){
          val p =  (date, 1)
          emotionPlus += p
        }
        
      }
      if(result < 0){
        var exist = false
        for(j <- 0 to emotionMinus.size - 1){
          if(emotionMinus(j)._1 == date){
            emotionMinus(j) = (emotionMinus(j)._1, emotionMinus(j)._2 + 1)                
            exist = true;
          }
        }
        if(exist == false){
          val p =  (date, 1)
          emotionMinus += p
        }
      }
    }
    val sb = new StringBuilder 
    sb.append("[")
    for(i <- 0 to emotionPlus.size - 1){
      sb.append("{\"dt\":\"" + emotionPlus(i)._1 + "\",\"sentiment\":\"positive\",\"count\":" + emotionPlus(i)._2.toString() +",\"date\":\"" + emotionPlus(i)._1.replace("-", "") + "\"},")
      

    }
    
    for(i <- 0 to emotionMinus.size - 1){
      sb.append("{\"dt\":\"" + emotionMinus(i)._1 + "\",\"sentiment\":\"negative\",\"count\":" + emotionMinus(i)._2.toString() +",\"date\":\"" + emotionMinus(i)._1.replace("-", "") + "\"},")      
    }
    
    var emotion = sb.toString()
    emotion = emotion.substring(0, emotion.length() - 1) + "]";
    val writer = new PrintWriter(new File("/var/www/html/SNS/sentiment.csv")) 
    writer.write(emotion)
    writer.close()
    
    println(article.length.toString() + " 감성분석완료")
    analysis.clearWords();
    for(i <- 0 to article.length - 1){      
      analysis.getWordMorph(i, article(i)) 
    }
    
    println(article.length.toString() + " 형태소분석 완료")
  }
}