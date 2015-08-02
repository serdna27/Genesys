package scala.idx.Genesys

sealed trait Config

sealed trait SourceFile extends Config{
  val folderContent:String
}

case class DbSource(host:String,user:String,pwd:String,dbType:String) extends Config

case class XmlSource(folderContent:String) extends SourceFile

case class JsonSource(folderContent:String) extends SourceFile

case class KeysFormat(name:String,val format:String) extends Config

case class TemplateSettings(saveOnDisk:Boolean,directory:String,author:String,prefixName:String="",suffixName:String="",createFolder:Boolean=false) extends Config

case class TemplateData(content:String,setting:TemplateSettings) extends Config 

case class SourceData(database:DbSource,xml:XmlSource,json:JsonSource) extends Config

case class Configuration(sourceType:String,typeMapping:Map[String,String],source:SourceData,templates:List[TemplateData],keysFormat:List[KeysFormat]) extends Config
 

object Configuration{
    
  import org.json4s._
  import org.json4s.native.JsonMethods._
  import scala.io.Source
  import java.io.FileReader
  import org.json4s.native.Serialization
  import org.json4s.native.Serialization.{ read, write, writePretty }
  
    def apply(sourceConfig:String):Configuration={
      
      implicit val formats=Serialization.formats(NoTypeHints)
      val reader=Source.fromFile(sourceConfig)
      var jsonData=reader.mkString
      reader.close()
      val configObj=read[Configuration](jsonData)
      
      return configObj
      
      }
    
    def getTemplate(template: String): String = {
    
      val reader = Source.fromFile(template)
      var data = reader.mkString
      reader.close()
      return data
    }
      
    
  }

