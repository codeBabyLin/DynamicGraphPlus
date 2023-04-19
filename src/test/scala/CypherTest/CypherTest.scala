package CypherTest

import java.io.File

import org.junit.{After, Before, Test}
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory

class BaseTest {
  val path = "F:\\DynamicGraphStore"
  def registerShutdownHook(graphDb: GraphDatabaseService): Unit ={
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit = {
        graphDb.shutdown()
      }
    }))
  }

  var graphDb: GraphDatabaseService = _

  def delfile(file: File): Unit ={
    if(file.isDirectory){
      val files = file.listFiles()
      files.foreach(delfile)
    }
    file.delete()
  }

  @Before
  def init(): Unit ={
    //val path = "F:\\DynamicGraphStore"
    delfile(new File(path))

  }
  @After
  def close(): Unit ={
    if(graphDb!=null) {
      graphDb.shutdown()
    }
  }
}

class CypherTest extends BaseTest {

  @Test
  def testCypher(): Unit ={
    val path = "F:\\DynamicGraphStore"
    val dataBaseDir = new File(path,"data")
    graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dataBaseDir)
    registerShutdownHook(graphDb)


    //var tx = graphDb.beginTx()//1
   // val version = DGVersion.toString(tx.getVersion)
    val cy1 = "match(n) return n "
    var t = graphDb.execute(cy1).hasNext
    println(t)
    val cy2 = "create(n:student{name:'JoeJoe',age:23})"
    graphDb.execute(cy2)
   // tx.success()
   // tx.close()

   // tx = graphDb.beginTx()
    t = graphDb.execute(cy1).hasNext
    println(t)
    val cy3 = "match(n) delete n"
    graphDb.execute(cy3)
  //  tx.success()
 //   tx.close()


 //   tx = graphDb.beginTx()
    t = graphDb.execute(cy1).hasNext
    println(t)
  //  tx.success()
  //  tx.close()

  }

  @Test
  def testCreate(): Unit ={
    val path = "F:\\DynamicGraphStore\\newOral"
    val dataBaseDir = new File(path,"data")
    graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dataBaseDir)
    registerShutdownHook(graphDb)
    //var tx = graphDb.beginTx()//1
    // val version = DGVersion.toString(tx.getVersion)
    val cy1 = "createv(n:student{name:'JoeJoe',age:23}) at(5) return n"
    val cy2 = "createv(n:student{name:'Baby',age:23}) at(4) return n"
    val cy3 = "createv(n:student{name:'loli',age:23}) at(4) return n"
    //val cy4 = "matchhis(n) at 4 return count(n) "
    val cy4 = "matchhis(n) at 5 return count(n)"

    var tx = graphDb.beginTx()
    graphDb.execute(cy1)
    graphDb.execute(cy2)
    graphDb.execute(cy3)
    tx.success()
    tx.close()


    val t = graphDb.execute(cy4)
    while(t.hasNext){
      val res = t.next()
      println(res)
    }

  }

  @Test
  def testAfter(): Unit ={
    val path = "F:\\DynamicGraphStore\\newOral"
    val dataBaseDir = new File(path,"data")
    graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dataBaseDir)
    registerShutdownHook(graphDb)
    //var tx = graphDb.beginTx()//1
    // val version = DGVersion.toString(tx.getVersion)
    val cy1 = "createv(n:student{name:'JoeJoe',age:23}) at(5) return n"
    val cy2 = "createv(n:student{name:'Baby',age:23}) at(4) return n"
    val cy3 = "createv(n:student{name:'loli',age:23}) at(4) return n"
    //val cy4 = "matchhis(n) at 4 return count(n) "
    val cy4 = "matchv(n) before 5 return n "

    var tx = graphDb.beginTx()
    graphDb.execute(cy1)
    graphDb.execute(cy2)
    graphDb.execute(cy3)
    tx.success()
    tx.close()


    val t = graphDb.execute(cy4)
    while(t.hasNext){
      val res = t.next()
      println(res)
    }

  }




}
