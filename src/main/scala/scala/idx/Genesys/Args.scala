package scala.idx.Genesys

import java.io.File
import java.util
import com.beust.jcommander.{JCommander,Parameter}
import collection.JavaConversions._

object Args {
    // Declared as var because JCommander assigns a new collection declared
    // as java.util.List because that's what JCommander will replace it with.
    // It'd be nice if JCommander would just use the provided List so this
    // could be a val and a Scala LinkedList.
    @Parameter(
      names = Array("-f", "--file"),
      description = "Json Configuration File.")
    var configFile: String="configuration.json"

    @Parameter(names=Array("-h","--help"),description="Show help.",help=true)
    private var help:Boolean=_


    @Parameter(
      names=Array("-e","--entities"),
      description="Entities to process separated by comma."
    )
    var entities:java.util.List[String]=new util.ArrayList[String]()

    @Parameter(names =Array("-st","--stemplates"),description = "Show the templates included in the config file." )
    var showTemplates:Boolean=_

  @Parameter(names=Array("-tf","--templates-files"),description = "Templates files to process, if not templates files are specified all templates files will be generated.")
  var templatesFiles=new util.ArrayList[String]()

  @Parameter(names=Array("-gc","--gconfig"),description="Generate Configuration Files and folders.")
  var generateConfig:Boolean=_



}
