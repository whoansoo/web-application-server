package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private Map<String, String>ResponseHeder;
    
    public HttpResponse() {		
    	ResponseHeder = new HashMap<String, String>();
		ResponseHeder.put("response200Header",    "HTTP/1.1 200 OK \r\nContent-Type: text/html;charset=utf-8\r\n");
		ResponseHeder.put("response200CssHeader", "HTTP/1.1 200 OK \r\nContent-Type: text/css;charset=utf-8\r\n");
		ResponseHeder.put("response302Header",    "HTTP/1.1 302 Redirect \r\nLocation: /index.html \r\n\r\n");
		ResponseHeder.put("response302LoginSuccessHeader", "HTTP/1.1 302 Redirect \r\nSet-Cookie: logined=true \r\nLocation: /index.html \r\n\r\n");
	}

	public void Response(int ret, HttpRequest hr, OutputStream out, String url) throws IOException {
    	log.debug("===========================>>>>" + ret);
    	switch (ret) {
		case 100:
			
			break;
		case 101:
			forward(out, url, true);			
			break;
		case 102:
			forward(out, url, false);
			break;
		case 200:
            Collection<User> users = DataBase.findAll();
            StringBuilder sb = new StringBuilder();
            sb.append("<table border='1'>");
            for (User user : users) {
                sb.append("<tr>");
                sb.append("<td>" + user.getUserId() + "</td>");
                sb.append("<td>" + user.getName() + "</td>");
                sb.append("<td>" + user.getEmail() + "</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            byte[] body = sb.toString().getBytes();
            
            response200Header(out, body.length);
            responseBody(out, body);
            
			break;
		case 301:
			User user = new User(hr.getParameter("userId"), hr.getParameter("password"), hr.getParameter("name"),
            		hr.getParameter("email"));
            log.debug("user : {}", user);
            DataBase.addUser(user);
            response302Header(out);
			break;
		case 302:
			response302LoginSuccessHeader(out);
			break;
		case 401:  //not login
			sendRedirect(out, "/user/login.html");			
			break;
		case 400:
			sendRedirect(out, "/user/login_failed.html");			
			break;
		default:
			break;
		}
    }
    
    
    @SuppressWarnings("unused")
    public  void forward(OutputStream out, String url, boolean css) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        if (css)
        	response200CssHeader(dos, body.length);
        else
        	response200Header(dos, body.length);
        responseBody(dos, body);
    }
    
    public void sendRedirect(OutputStream out, String url) throws IOException {
    	forward(out, url, false);
    }
    
    public  void response200Header(OutputStream out, int lengthOfBodyContent) {
        try {
        	DataOutputStream dos = new DataOutputStream(out);
            dos.writeBytes(ResponseHeder.get("response200Header"));
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
        	dos.writeBytes(ResponseHeder.get("response200CssHeader"));
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void response302Header(OutputStream out) {
        try {
        	DataOutputStream dos = new DataOutputStream(out);
        	dos.writeBytes(ResponseHeder.get("response302Header"));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public  void response302LoginSuccessHeader(OutputStream out) {
        try {
        	DataOutputStream dos = new DataOutputStream(out);
        	dos.writeBytes(ResponseHeder.get("response302LoginSuccessHeader"));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public  void responseBody(OutputStream out, byte[] body) {
        try {
        	DataOutputStream dos = new DataOutputStream(out);
            dos.write(body, 0, body.length);
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }    
}
