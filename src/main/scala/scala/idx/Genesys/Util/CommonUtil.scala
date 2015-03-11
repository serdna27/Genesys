package scala.idx.Genesys.Domain

import scala.idx.Genesys._
import scala.xml._
import scala.io._
import java.io.File
import scala.io.Source
import java.io.FileReader
import java.sql.{DriverManager,DatabaseMetaData}

abstract class ObjDef(val name: String)

class FieldDef(override val name: String, val typeValue: String,var entName:String="",val length:Option[Int]=None,val nullable:Option[Boolean]=None,val primary:Option[Boolean]=None) extends ObjDef(name)

class EntityDef(override val name: String, val label: String) extends ObjDef(name) {

  private var fields: List[FieldDef] = List()
  private var _fieldsMap: Map[String, FieldDef] = Map[String, FieldDef]()
  def addField(fieldMap: (String, FieldDef)) = { _fieldsMap += fieldMap }

  def getField(field: String): FieldDef = {
    return this._fieldsMap(field)
  }
  def getFields(): List[FieldDef] = this._fieldsMap.map(t => t._2).toList
}

trait SourceParser{
  def getEntity(sourceModel:Any):EntityDef=new EntityDef("","")
}

trait XmlParser extends SourceParser{
  
  override def getEntity(sourceModel:Any):EntityDef={
    
    val model=XML.loadString(sourceModel.asInstanceOf[String])
    val name = (model \\ "entity" \\ "name").text
    val label = (model \\ "entity" \\ "label").text
    var entity = new EntityDef(name, label)

    (model \\ "entity" \\ "fields" \\ "field").foreach { field =>

      var fieldType = (field \ "@type").text
      entity.addField(field.text -> new FieldDef(field.text, fieldType))
    }
    println("loading xml-parser..")
    println(entity.label)
    return entity
    
  }

}

trait DbParser extends SourceParser{
  
  private def getPrimaryKey(table:String,metadata:DatabaseMetaData):Set[String]={
     var resultPks=metadata.getPrimaryKeys(null, null, table)
     val pks=Set.empty[String]
     while(resultPks.next()){
       pks.+(resultPks.getString("COLUMN_NAME"))
      }
     return pks
     
   }
  
  override def getEntity(sourceModel:Any):EntityDef={
    
    val model=sourceModel.asInstanceOf[(String, DatabaseMetaData)]
    val tableName=model._1
    val metadata=model._2
    var entity = new EntityDef(tableName, tableName)
    var columnsResult=metadata.getColumns(null, null, tableName, null)
    val pks=getPrimaryKey(tableName,metadata)
    while (columnsResult.next()) {
      entity.addField(
              columnsResult.getString("COLUMN_NAME")->new FieldDef(
              name=columnsResult.getString("COLUMN_NAME"),
              entName=tableName,
              typeValue=columnsResult.getString("TYPE_NAME").split(" ")(0),//put this fix to avoid extra type info comming from the db
              length=Option(columnsResult.getInt("COLUMN_SIZE")),
              nullable=Option(columnsResult.getInt("NULLABLE")==DatabaseMetaData.columnNullable),
              primary=Option(pks.contains(columnsResult.getString("COLUMN_NAME")))
              )
          )
    }
    

    println("loading db-parser..")
    return entity
    
  }

}

object EntityReader{
import scala.collection.JavaConverters._
import scala.idx.Genesys.Util._

 def apply(config:Config,ents: java.util.List[String]=null):List[EntityDef]={
    var entityList:List[EntityDef]=List()
    config match{
      case XmlSource(folder)=>{
        var files=new File(folder).listFiles().filter { _.getName().endsWith(".xml") }
        for(file <- files){
            val fileReader=Source.fromFile(file)
            val parser=new EntityParser(fileReader.mkString) with XmlParser
            entityList=entityList.::(parser.parse())//call the entityParser with the xmlParser
            fileReader.close()
        }
      }
      case DbSource(host,user,pwd,dbType)=>{
        
        if(!(CommonUtil.dbDriverMapping.contains(dbType))){
          throw new Exception("Invalid db type")
        }
          
        Class.forName(CommonUtil.dbDriverMapping(dbType))
        val connection=DriverManager.getConnection(host,user,pwd)
        val metadata=connection.getMetaData
        var tableResult=metadata.getTables(null, null, null, Array("TABLE"))
        while (tableResult.next()) {
          val model=(tableResult.getString("TABLE_NAME"),metadata)
          //filtering the table to generate
          if((ents!=null && ents.asScala.exists { x => x==model._1 }) || ents==null){
            val parser=new EntityParser(model) with DbParser
            entityList=entityList.::(parser.parse())//call the entityParser with the dbParse
          }
        }
      }
      case _ => new Exception("error not a valid source")
    }
    return entityList
  }
  
  private class EntityParser(source:Any) extends SourceParser {
    
    def parse():EntityDef={
      println("initialize entity parser")
      return this.getEntity(source)
    }
  }

}
