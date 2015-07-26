package scala.idx.Genesys

import collection.mutable.Stack

import org.scalatest._
import scala.idx.Genesys
import com.beust.jcommander.JCommander

class GeneSpec extends FlatSpec with Matchers{
 
  
  "It" should  "parse the json config file" in{
    
      val configFile = "/Users/andresktejada/Documents/Development/Scala/Code/Genesys/configuration/"
    
      Generator.main(Array("-f",configFile,"-e","crm_city"))
      
      Generator.Result.errors.size should be equals 0
      Generator.Result.TemplatesQty should be >0

  }

  "It" should "show the help" in{
    Generator.main(Array("--help"))
    Generator.Result.errors.size should be equals 0

  }

  "It" should "show the templates being used" in{

    val configFile = "/Users/andresktejada/Documents/Development/Scala/Code/Genesys/configuration/"
    Generator.main(Array("-f",configFile,"-st"))
    Generator.Result.errors.size should be equals 0

  }

  "It" should "process templates specified" in{

    val configFile = "/Users/andresktejada/Documents/Development/Scala/Code/Genesys/configuration/"
    Generator.main(Array("-f",configFile,"-e","crm_city","-tf","ent.cs"))
    Generator.Result.errors.size should be equals 0

  }
  
}
