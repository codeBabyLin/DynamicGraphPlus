package cn.DynamicGraph.BootStrap

object DGStarter {
  def main(args: Array[String]): Unit = {
    val NEO4J_HOME = args(0)
    val NEO4J_CONF = args(1)
    //System.out.println(this.getClass.getClassLoader.getResource("src/main/resources/WebPage/Data/newOral.txt").getPath)

    DynamicGraphStarter.main(Array(s"--home-dir=${NEO4J_HOME}", s"--config-dir=${NEO4J_CONF}"))
    //CommunityEntryPoint.main(Array(s"--home-dir=${NEO4J_HOME}", s"--config-dir=${NEO4J_CONF}"))

  }
}
