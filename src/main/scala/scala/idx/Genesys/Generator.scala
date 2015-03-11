package scala.idx.Genesys

import com.gilt.handlebars.scala.Handlebars
import com.gilt.handlebars.scala.helper._
import scala.xml._
import scala.idx.Genesys.Domain._
import com.gilt.handlebars._
import com.gilt.handlebars.scala.binding.dynamic._
import java.nio.file.{ Paths, Files }
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

    def errors_=(value: String): Unit = { errorMsgs = errorMsgs.::(value) }

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
      process()
    } catch {
      case e: Exception => println("exception occurred==>" + e)
    }
  }

  private def process() {

    config = Configuration(Args.configFile)
    var entities: List[EntityDef] = List()

    config.sourceType match {
      case "xml" => entities = (EntityReader(config.source.xml, Args.entities))
      case "db"  => entities = (EntityReader(config.source.database, Args.entities))
      case _     => throw new Exception("invalid source type")
    }

    val formatName = config.keysFormat.find { x => x.name == "name" }

    try {
      Result.TemplatesQty = config.templates.size
      for (entity <- entities) {

        var entName = entity.name
        if (!formatName.isEmpty) {
          formatName.get.format match {
            case "camelCase"  => entName = toCamelCase(entName)
            case "pascalCase" => entName = toPascalCase(entName)
            case _            => throw new Exception("invalid key format")
          }
        }

        var fields: Seq[Map[String, String]] = Seq.empty
        entity.getFields().foreach(f => {
          f.entName=entName
          fields = fields.+:(getTemplateField(f))
        })

        for (template <- config.templates) {

          val parse = Handlebars(Configuration.getTemplate(template.content))
          val filePath = Paths.get(template.setting.directory, Paths.get(template.content).getFileName.toString()).toString()

          saveTemplate(filePath, parse(templateData(entName, entity.label, fields)))

          Result.TemplatesQtyProcessed += 1
        }

      }
    } catch {
      case e: Exception => Result.errors = e.getMessage
    } finally {
      println("Results===>")
      println("Total Templates:" + Result.TemplatesQty)
      println("Total Templates Processed:" + Result.TemplatesQtyProcessed)
      if (Result.errors.size > 0) {
        println("Errors===>")
        Result.errors.foreach { x => println(x) }
      }
    }

  }

  private def getTemplateField(f: FieldDef): Map[String, String] = {

    val typeMap = if (config.typeMapping.contains(f.typeValue)) config.typeMapping(f.typeValue) else f.typeValue

    var mapDict = Map("isPrimary" -> f.primary.getOrElse(false).toString())
      .+("isNullable" -> f.nullable.getOrElse(false).toString()).+("length" -> f.length.getOrElse(0).toString())
      .+("type" -> typeMap).+("type" -> typeMap).+("entName" -> f.entName)

    val formatField = config.keysFormat.find { x => x.name == "#fields.name" }

    if (!formatField.isEmpty) {
      formatField.get.format match {
        case "camelCase"  => return (mapDict.+("name" -> toCamelCase(f.name)).+("label" -> toCamelCase(f.name)))
        case "pascalCase" => return (Map("name" -> toPascalCase(f.name)).+("label" -> toPascalCase(f.name)))
        case _            => throw new Exception("no key format found")
      }
    } else {
      return mapDict.+("name" -> f.name).+("label" -> "")
    }
  }

  def toPascalCase(content: String): String = {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, content.replace(" ", "_"));
  }

  def toCamelCase(content: String): String = {

    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, content.replace(" ", "_"));

  }

  private def saveTemplate(file: String, template: String) = {
    println("template==>" + template)
    Files.write(Paths.get(file), template.getBytes(StandardCharsets.UTF_8))
  }

}
