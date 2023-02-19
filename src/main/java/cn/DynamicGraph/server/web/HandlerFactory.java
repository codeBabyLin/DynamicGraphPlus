package cn.DynamicGraph.server.web;

//import cn.DynamicGraph.DataLoadAndRead;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

class MapDateHandler extends AbstractHandler {
    private GraphDatabaseFacade gdb;
    MapDateHandler(GraphDatabaseFacade gdb){
        this.gdb = gdb;
    }



    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        httpServletResponse.setContentType("text/html;charset=utf-8");
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        String res = "hello world";
       /* if(s.contains("all")){
            res = Arrays.toString(DataLoadAndRead.dateArray());
        }
        else {
            String date = s.replace("/", "");
            //httpServletResponse.set;

            //String hj = "helloworld";
            res = DataLoadAndRead.getData(gdb, date);
        }*/
        httpServletResponse.getWriter().write(res);
        request.setHandled(true);
    }
}


public class HandlerFactory {

    public ResourceHandler getResourceHandler() throws URISyntaxException {
        ResourceHandler rh = new ResourceHandler();
        rh.setDirectoriesListed(true);
        //URI uri = HandlerFactory.class.getResource("WebPage/map").toURI();
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File p = new File(path).getParentFile().getParentFile();
        //String uri = Objects.requireNonNull(this.getClass().getClassLoader().getResource("WebPage/map")).getPath();
        String uri = new File(p,"conf/").getAbsolutePath();
        System.out.println(uri);
        rh.setResourceBase(uri);
        rh.setWelcomeFiles(new String[]{"map.html"});
        return rh;
    }

    public  ContextHandler getContextHandlerWithDate(GraphDatabaseFacade gdb) throws URISyntaxException {
        ContextHandler ch = new ContextHandler();

        ch.setContextPath("/browser/map/date");
        //ch.setContextPath("/map/date");
        ch.setHandler(new MapDateHandler(gdb));
        return ch;

    }

    public  ContextHandler getContextHandler() throws URISyntaxException {
        ContextHandler ch = new ContextHandler();
        ch.setContextPath("/browser/map");
        //ch.setContextPath("/map");
        ch.setHandler(getResourceHandler());
        return ch;

    }


}
