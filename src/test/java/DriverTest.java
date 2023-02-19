import org.junit.Before;
import org.junit.Test;

public class DriverTest {

    @Before
    public void init(){

    }

    @Test
    public void testConnection(){
       /* Driver driver = GraphDatabase.driver("bolt://localhost:7687");
        Session s = driver.session();
        Transaction tx =s.beginTransaction();
        String cy = "create(n:province{name:'koko',age:12}) return n";
        //val map: Map[String,Object] = new Map[String,Object]()
        Map<String,Object> para = new HashMap<>();
        //para.put("version",123);
        StatementResult res = tx.run(cy,100);
        Record r= res.next();
        Entity e = r.get(0).asEntity();
        //tx.run(cy);

        tx.success();
        tx.close();*/
        //println(res)
    }
    @Test
    public void testExcuteCypher(){
       /* Driver driver = GraphDatabase.driver("bolt://localhost:7687");
        Session s = driver.session();
        Transaction tx =s.beginTransaction();
        String cy1 = "createv(n:province{name:'koko',age:12}) at(1) return n";
        String cy2 = "createv(n:province{name:'jojo',age:21}) at(6) return n";
        String cy3 = "createv(n:province{name:'jojo',age:21}) at(6) return n";

        tx.run(cy1);
        tx.run(cy2);
        tx.run(cy3);


        tx.success();
        tx.close();

        Transaction tx1 = s.beginTransaction();
        String cy4 = "match(n) at(6) return n";
        StatementResult res = tx1.run(cy4);
        while(res.hasNext()){
            Record r = res.next();
            System.out.println(r);
        }
*/
        //RunMessageEncoder


    }




}
