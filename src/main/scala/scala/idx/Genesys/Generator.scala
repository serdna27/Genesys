package scala.idx.Genesys

import com.gilt.handlebars.scala.Handlebars
import scala.xml._
import scala.idx.Genesys.Domain._
import com.gilt.handlebars._
import com.gilt.handlebars.scala.binding.dynamic._
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets
import com.beust.jcommander.JCommander
import com.google.common
import com.google.common.base.CaseFormat

object Generator extends App {

  case class templateData(val name: String, val label: String, fields: Seq[Map[String, String]])

  object Result {
    var TemplatesQty: Int = _
    var TemplatesQtyProcessed: Int = _
    private var errorMsgs: List[String] = List.empty[String]

    def errors_=(value: String): Unit = {
      errorMsgs = errorMsgs.::(value)
    }

    def errors = errorMsgs

    def addError(errMsg: String) = {
      errorMsgs = errorMsgs.::(errMsg)
    }

  }

  var config: Configuration = null

  override def main(args: Array[String]) = {

    println("**Genesys Code Generator**")

    try {
      val jc = new JCommander(Args, args: _*)
      if(Args.showTemplates==true){
        if(Args.configFile.isEmpty){
          //sys.exit(1)
        }else{
          config = Configuration(Args.configFile)
          showTemplates()
          // sys.exit(1)
        }
      }
      else if(Args.generateConfig==true){
        createConfig()
      }
      else if (Args.configFile.isEmpty || Args.entities.size() == 0) {
        println("error parsing the parameters")
        jc.usage()
      }
      else {
        println("its going to process..")
        process()
      }

    } catch {

      case e: Exception => {

        println("an error ocurred")
        println("exception occurred==>" + e)
      };
    }
  }

private def createConfig()={

  import org.json4s._
  import java.io.File
  import java.nio.file._
  import org.json4s.native.Serialization
  import org.json4s.native.Serialization.{ read, write, writePretty }

  val currentPath = System.getProperty("user.dir")
  val folderXml=Paths.get(currentPath,"genesys-config/xml-data/")
  val folderTemplates=Paths.get(currentPath,"genesys-config/templates/")
  val folderSources=Paths.get(currentPath,"genesys-config/generated-sources/")

  val source=SourceData(database = DbSource(host="",user="",pwd="",dbType = ""),xml = XmlSource(folderContent = folderXml.toString),json=JsonSource(folderContent = "//path-json"))
  val templates=List(TemplateData(content=folderTemplates.toString,setting = TemplateSettings(saveOnDisk = true,directory = folderSources.toString,author = "your-name",prefixName = "",suffixName = "")))

  val config=Configuration(sourceType = "db",typeMapping = Map("varchar"->"string"),
    source = source,templates=templates,
    keysFormat=List(KeysFormat(name="name",format = "pascalCase"),KeysFormat(name="#fields.name",format="pascalCase")))
  implicit val formats = Serialization.formats(NoTypeHints)

  val file=Paths.get(currentPath,"configuration.json").toString()
  println("config file==>"+file)
  var fileInfo = new File(file.toString)

  val dirExists = Files.exists(folderXml.getParent())

  //if directory genesys-config  does not exits create it
  if (dirExists == false) {
    Files.createDirectories(folderXml.getParent)
  }

  if(Files.exists(folderXml)==false) {
    println(s"folder:${folderXml} was created")
    Files.createDirectories(folderXml)
  }
  if(Files.exists(folderTemplates)==false) {
    println(s"folder:${folderTemplates} was created")
    Files.createDirectories(folderTemplates)
  }
  if(Files.exists(folderSources)==false) {
    println(s"folder:${folderSources} was created")
    Files.createDirectories(folderSources)
  }

  val configJson=writePretty(config)
  Files.write(Paths.get(file), configJson.getBytes(StandardCharsets.UTF_8))


}

 def cleanConfig()={
   import java.io.File
   import java.nio.file._


   val currentPath = System.getProperty("user.dir")
   val folderXml=Paths.get(currentPath,"genesys-config/xml-data/")
   val folderTemplates=Paths.get(currentPath,"genesys-config/templates/")
   val folderSources=Paths.get(currentPath,"genesys-config/generated-sources/")
   val folderConfig=folderSources.getParent
   if(Files.exists(folderXml))
      Files.delete(folderXml)
   if(Files.exists(folderTemplates))
      Files.delete(folderTemplates)
   if(Files.exists(folderSources)) {
     Files.delete(folderSources)

   }
   Files.delete(Paths.get(currentPath,"configuration.json"))
   Files.delete(folderConfig)



}

private def showTemplates()={
  var numberTemplates=1
  println()
  println("**Templates**")
  while(numberTemplates<=config.templates.size){
    val template=config.templates(numberTemplates-1)
    val templateName=Paths.get(template.content).getFileName()

    println(f"$numberTemplates%d-${templateName}%s")
    numberTemplates+=1
  }

}

  private def process() {

    config = Configuration(Args.configFile)
    var entities: List[EntityDef] = List()

    //read the entities based on the source type
    config.sourceType match {
      case "xml" => entities = (EntityReader(config.source.xml, Args.entities))
      case "db" => entities = (EntityReader(config.source.database, Args.entities))
      case _ => throw new Exception("invalid source type")
    }

    //find the format type  used for entities name
    val formatName = config.keysFormat.find { x => x.name == "name"}

    try {
      var templates=config.templates
      //if the templates files are being specified by command line process them
      if(Args.templatesFiles.size()>0){
        templates=templates.filter({
          k=> Args.templatesFiles.contains(Paths.get(k.content).getFileName.toString)==true
        })
      }
      //check if we have at least one template
      if(templates.size==0){
        println("Templates specfied don't existy;!")
        sys.exit(1)
      }

      Result.TemplatesQty = config.templates.size
      for (entity <- entities) {

        var fields: Seq[Map[String, String]] = Seq.empty

        //populate the fields found in the entity
        entity.getFields().foreach(f => {
          fields = fields.+:(getTemplateField(f, entity.name))
        })

        for (template <- templates) {
          val parse = Handlebars(Configuration.getTemplate(template.content))


          var entLabel = entity.name
          var entName = entity.name
          if (!formatName.isEmpty) {
            formatName.get.format match {
              case "camelCase" => entLabel = toCamelCase(entName)
              case "pascalCase" => entLabel = toPascalCase(entName)
              case _ => throw new Exception("invalid key format")
            }
          }
          if(template.name!=""){
            entLabel=template.name
          }
          var fileName = template.setting.prefixName + entLabel + template.setting.suffixName + "." + getFileExtension(template.content)
          var filePath=""
          if(template.setting.createFolder==true){
            filePath = Paths.get(template.setting.directory,entLabel, fileName).toString()
          }
          else{
            filePath = Paths.get(template.setting.directory, fileName).toString()
          }
          saveTemplate(filePath, parse(templateData(entName, entLabel, fields)))

          Result.TemplatesQtyProcessed += 1
        }

      }
    } catch {
      case e: Exception => Result.errors = e.getMessage
    }
    finally {
      println("Results===>")
      println("Total Templates:" + Result.TemplatesQty)
      println("Total Templates Processed:" + Result.TemplatesQtyProcessed)
      if (Result.errors.size > 0) {
        println("Errors===>")
        Result.errors.foreach { x => println(x)}
      }
    }

  }

  private def getTemplateField(f: FieldDef, tableName: String): Map[String, String] = {

    //get the mapped type used, i.e database type varchar->string
    val typeMap = if (config.typeMapping.contains(f.typeValue)) config.typeMapping(f.typeValue) else f.typeValue

    //map containing the basic information of the column
    var mapDict = Map("length" -> f.length.getOrElse(0).toString())
      .+("type" -> typeMap).+("columnName" -> f.name).+("tableName" -> tableName)


    if (f.nullable.get == true) {
      mapDict = mapDict.+("isNullable" -> "true")
    }
    if (f.primary.get == true) {
      mapDict = mapDict.+("isPrimary" -> "true")
    }
    if (f.foreignKey.isDefined) {
      mapDict = mapDict.+("foreignKey" -> toPascalCase(f.foreignKey.get))
    }

    //get the format for fields name  
    val formatField = config.keysFormat.find { x => x.name == "#fields.name"}

    if (!formatField.isEmpty) {
      formatField.get.format match {
        case "camelCase" => return mapDict + ("name" -> toCamelCase(f.name))
        case "pascalCase" => return mapDict + ("name" -> toPascalCase(f.name))
        case _ => throw new Exception("no key format found")
      }
    } else {
      return mapDict + ("name" -> f.name)
    }
  }

  def getFileExtension(file: String): String = {
    val dot = file.lastIndexOf(".")
    return file.substring(dot + 1)
  }


  def toPascalCase(content: String): String = {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, content.replace(" ", "_"));
  }

  def toCamelCase(content: String): String = {

    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, content.replace(" ", "_"));

  }

  private def saveTemplate(file: String, template: String) = {
    import java.io.File
    import java.nio.file._

    val formmatedTemplate = template.replaceAll("(?m)^[ \t]*\r?\n", "")

    println("template==>" + formmatedTemplate)
    var fileInfo = new File(file)


    println("path==>" + fileInfo.getParent())
    val dirExists = Files.exists(Paths.get(fileInfo.getParent()))
    //if directory does not exits create it
    if (dirExists == false) {
      Files.createDirectories(Paths.get(fileInfo.getParent()))
    }


    Files.write(Paths.get(file), formmatedTemplate.getBytes(StandardCharsets.UTF_8))
  }


}