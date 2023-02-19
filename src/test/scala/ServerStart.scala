import java.io.File

import cn.DynamicGraph.BootStrap.DynamicGraphStarter

object ServerStart {

  def delfile(file: File): Unit ={
    if(file.isDirectory){
      val files = file.listFiles()
      files.foreach(delfile)
    }
    file.delete()
  }

  def main(args: Array[String]): Unit = {
    val NEO4J_HOME = "F:\\DynamicGraphStorePlus\\Server"
    //val NEO4J_CONF = "F:\\IdCode\\DynamicGraph\\"
    //F:\IdCode\codeBabyDynamicGraph\neo4j.conf
    val NEO4J_CONF = "F:\\DynamicGraphPlus\\"

    delfile(new File(NEO4J_HOME))
    DynamicGraphStarter.main(Array(s"--home-dir=${NEO4J_HOME}", s"--config-dir=${NEO4J_CONF}"))
    //CommunityEntryPoint.main(Array(s"--home-dir=${NEO4J_HOME}", s"--config-dir=${NEO4J_CONF}"))

  }
}
