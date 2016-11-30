package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        HttpResponse hrs = new HttpResponse();
        int ret = 0;
        
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	HttpRequest hr = new HttpRequest(in);

            String url = hr.getURL();            
            
            if ("/user/create".equals(url)) {
                ret = 301;
                
            } else if ("/user/login".equals(url)) {
                User user = DataBase.findUserById(hr.getParameter("userId"));
                if (user != null) {
                    if (user.login(hr.getParameter("password"))) {
                    	ret = 302;                         
                    } else {
                    	ret = 400;                    	
                    }
                } else {
                	ret = 400;                	
                }
            } else if ("/user/list".equals(url)) {
                if (!hr.getLogined()) {
                	ret = 401;                	
                    return;
                }
                ret = 200;
                
            } else if (url.endsWith(".css")) {
            	ret = 101; 
            	
            } else {
            	ret = 102;            	
            }
            hrs.Response(ret, hr, out, url);
            
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
