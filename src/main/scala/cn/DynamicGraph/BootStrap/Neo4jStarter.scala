package cn.DynamicGraph.BootStrap

import java.io.File


object Neo4jStarter {

  def delfile(file: File): Unit ={
    if(file.isDirectory){
      val files = file.listFiles()
      files.foreach(delfile)
    }
    file.delete()
  }

  def main(args: Array[String]): Unit = {
    val NEO4J_HOME = "F:\\DynamicGraphStore\\Server"
    val NEO4J_CONF = "F:\\IdCode\\codeBabyDynamicGraph\\"
    //F:\IdCode\codeBabyDynamicGraph\DynamicGraph-tool\src\main\resources\neo4j.conf

    delfile(new File(NEO4J_HOME))
    DynamicGraphStarter.main(Array(s"--home-dir=${NEO4J_HOME}", s"--config-dir=${NEO4J_CONF}"))
    //CommunityEntryPoint.main(Array(s"--home-dir=${NEO4J_HOME}", s"--config-dir=${NEO4J_CONF}"))

  }
}
