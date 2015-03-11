package scala.idx.Genesys

import java.io.File
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
    var configFile: String=_
    @Parameter(names=Array("--help"),help=true)
    private var help:Boolean=_
    @Parameter(
      names=Array("-e","--entities"),
      description="Entities to process separated by comma"
    )
    var entities:java.util.List[String]=_
  }
