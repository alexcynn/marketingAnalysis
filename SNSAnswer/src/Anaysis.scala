import wst.net._
import java.io._
import org.apache.spark._
import SparkContext._ 
import org.apache.spark.mllib.fpm.FPGrowth
import org.apache.spark.rdd.RDD
import scala.collection.mutable.MutableList
import scala.collection.mutable.StringBuilder

class Analysis {
  var emotion:Map[String,Int] = Map()
  val sparkConf = new SparkConf().setAppName("SNSAnalysis").setMaster("local[2]").setSparkHome("SPARK_HOME")
  val sc = new SparkContext(sparkConf)
  
  def loadEmtionWord() {
    var sql = new wst.net.SQL();
    val raw = sql.getEmotionWord();    
    val lines = raw.split('\n')
    val k = lines.length -1
    for(i <- 0 to k){
      //if(lines(i).split('\t')(0).length() == 2){
      
      emotion += (lines(i).split('\t')(0) -> lines(i).split('\t')(1).toInt)
      //}
    }
  }
  
  def getEmotionPoint(line:String) = {
    var result = 0
    if(line.split('\t').length == 4){
      val temp = line.split('\t')(1) + " " + line.split('\t')(3)
      val words = temp.split(' ')
      for(i <- 0 to words.length - 1){
        if(emotion.contains(words(i))){
          result = result + emotion(words(i))
        }
      }
    }
    result
  }
  
  def getWordMorph(index:Int, line:String) = {
    //var result = ""
    if(line.split('\t').length == 4){
      if(line.indexOf("임대") > 0 || line.indexOf("*****") > 0){
        
      }else{
        val temp = line.split('\t')(1) + " " + line.split('\t')(3)
        val date = line.split('\t')(2)
        val words = temp.split(' ')
        val builder = StringBuilder.newBuilder        
        builder.append(Posting.wordAnalysis(temp))        
        saveCollect(index, date,builder.toString());
      }
       
    }
    //result
    
  }
  
  def clearWords(){
    val writer = new PrintWriter(new File("words.txt")) 
    writer.write("")
    writer.close()
  }
  
  def saveCollect(index:Int, date:String, words:String){
      val writer = new PrintWriter(new FileOutputStream(new File("words.txt"),true)) 
      //writer.write(source + "\t" + words + "\n")
      writer.write(index.toString() + "\t" + date + "\t" + words + "\n")
      //writer.write(words + "\n")
      writer.close()
  }
  
  def wordRanking(source:String) = {
    
    val textFile = sc.textFile("words.txt")
    val result = textFile.flatMap(line => line.split("\\s+"))
        .map(word => (word, 1)).reduceByKey(_ + _).map(item => item.swap) // interchanges position of entries in each tuple
        .sortByKey(false, 1) // 1st arg configures ascending sort, 2nd arg configures one task
        .map(item => item.swap)
    val arr = result.collect()
    
    val writer = new PrintWriter(new File("ranking.txt"))
    for(i <- 1 to arr.size){
      writer.write(i.toString() + "\t" + arr(i - 1) + "\n")  
    }
    writer.close()
    arr
  }
  
  def associationAnaylisys() = {
    val data = sc.textFile("words.txt")
    
    val transactions: RDD[Array[String]] = data.map(s => s.trim.split(' ').filter { x => x.length() > 1 && x != "이번"    }.distinct)
    
    val fpg = new FPGrowth()
      .setMinSupport(0.07)
      .setNumPartitions(10)
    val model = fpg.run(transactions)
    
    val nodes:MutableList[(String, Int)] = MutableList()
    
    var group = 0.toInt
    var first = ""
    model.freqItemsets.collect().foreach { itemset =>      
      println(itemset.items.mkString("[", ",", "]") + ", " + itemset.freq)
      if(itemset.items(0) != first){
        first = itemset.items(0)
        group = group + 1 
      }
      if(itemset.items.size == 1){
        val p =  (itemset.items(0), group)        
        nodes += p 
      }
    }
    
    val sb = new StringBuilder 
    sb.append("{\"nodes\":[")
    for(i <- 0 to nodes.size - 1){
      sb.append("{\"name\":\"" + nodes(i)._1 + "\",\"group\":" + nodes(i)._2 +"},")
    }
    
    var temp = sb.toString()
    temp = temp.substring(0, temp.length() - 1) + "],\"links\":[";
    
    sb.clear()
    
    val minConfidence = 0.3
    model.generateAssociationRules(minConfidence).collect().foreach { rule =>
      println(
        rule.antecedent.mkString("[", ",", "]")
      + " => " + rule.consequent .mkString("[", ",", "]")
      + ", " + rule.confidence)
      
      if(rule.antecedent.size == 1 && rule.consequent.size == 1){
        var from = -1
        var to = -1
        for(i <- 0 to nodes.size - 1){
          if(rule.antecedent(0) == nodes(i)._1){
            from = i
          }
          
          if(rule.consequent(0) == nodes(i)._1){
            to = i
          }
        }
        
        sb.append("{\"source\":" + from.toString() +",\"target\":" + to.toString() + ",\"value\": " + (rule.confidence * 10).toInt.toString() +"},")
      }
    }
    
    temp = temp + sb.toString()
    temp = temp.substring(0, temp.length() - 1) + "]}";
    
    val writer = new PrintWriter(new File("/var/www/html/SNS/topic.csv")) 
    writer.write(temp)
    writer.close()

  }
  
}