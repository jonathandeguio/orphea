//package PLM;
//
//import org.eclipse.jetty.server.HttpConfiguration;
//import org.eclipse.jetty.server.HttpConnectionFactory;
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.server.ServerConnector;
//import org.eclipse.jetty.servlet.ServletContextHandler;
//import org.eclipse.jetty.servlet.ServletHolder;
//
////@WebServlet(name = "HelloServlet", urlPatterns = {"/"})
////public class WebLB extends HttpServlet {
////
////    @Override
////    protected void doGet(HttpServletRequest request, HttpServletResponse response)
////            throws IOException {
////
////        response.setContentType("text/plain;charset=UTF-8");
////
////        var out = response.getOutputStream();
////
////        out.print("Hello there from Servlet");
////    }
////}
//
//public class WebLB {
//
//
//    Server server = new Server();
//
//    HttpConfiguration httpConfig = new HttpConfiguration();
//    httpConfig.setOutputBufferSize(32768);
//
//    ServerConnector gitConnector = new ServerConnector(server,
//            new HttpConnectionFactory(httpConfig));
//    gitConnector.setName("plm");
//    gitConnector.setPort(8080);
//    server.addConnector(gitConnector);
//
//    ServletContextHandler gitContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
//    gitContext.setVirtualHosts(new String[]
//
//    {
//        "@plm"
//    });
//
//
//    ServletHolder gitServletHolder = new ServletHolder(BlockingServlet.class);
////
//    gitServletHolder.setInitParameter("base-path","/"); //path to the git repositories
////        gitServletHolder.setInitParameter("export-all", "true"); //yes, true, 1, on: export all repositories
////        //no, false, 0, off: export no repositories
////        gitContext.addServlet(gitServletHolder, "/julia/*");
//
//    //start up the http server
//    server.setHandler(gitContext);
//    server.start();
//    server.join();
//}

package io.bosler.julia;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "WebLB", urlPatterns = {"/"})
public class WebLB extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/plain;charset=UTF-8");

        var out = response.getOutputStream();

        out.print("OK");
    }
}