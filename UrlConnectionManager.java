package Maps;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JOptionPane;

public class UrlConnectionManager {


    public static HttpURLConnection getHttpUrlconnection(String serverURL,boolean post) {
	HttpURLConnection httpurlc = null;
	try {
	    URL url = new URL(serverURL);
	    URLConnection urlc = url.openConnection();
	    httpurlc = (HttpURLConnection) urlc;
	    httpurlc.setDoOutput(true);
	    if (!post)
	    {
	    	httpurlc.setRequestMethod("GET");
		}
		else
		{
			httpurlc.setRequestMethod("POST");
		}
	    httpurlc.setRequestProperty(
		    "Content-Type", "application/octet-stream");
	    httpurlc.connect();
	} catch (MalformedURLException mfue) {
	    //logger.error("MalformedURLException :" + mfue);
	    return null;
	} catch (SocketTimeoutException ste) {
	    //logger.error("SocketTimeoutException :" + ste);
	    return null;
	} catch (IOException ioe) {
	    //logger.error("IOException  :" + ioe);
	    return null;
	}
	return httpurlc;
    }

    public static void sendRequest(HttpURLConnection httpurlc, Object request) {
	ObjectOutputStream oos = null;
	try {
	    oos = new ObjectOutputStream(httpurlc.getOutputStream());
	    oos.writeObject(request);
	    oos.flush();
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(null, "Exception"+e.getMessage());
	} finally {
	    try {
		oos.close();
	    } catch (IOException ioe) {
		//logger.error("IOException  :" + ioe);
	    }
	}
    }

    public static Object readResponse(HttpURLConnection httpurlc)
	    throws IOException {
	ObjectInputStream ois = null;
	Object respObj = null;
	try {
	    ois = new ObjectInputStream(httpurlc.getInputStream());
	    respObj = ois.readObject();
	} catch (Exception e) {
		JOptionPane.showMessageDialog(null, "Exception"+e.getMessage());
	} finally {
	    ois.close();
	}
	return respObj;
    }

    /*public static InputStream readXmlResponse(HttpURLConnection httpurlc)
	    throws IOException {
	InputStream in = null;
	try {
	    in = httpurlc.getInputStream();
	} catch (Exception e) {
	    if (null != in) {
		in.close();
	    }
	}
	return in;

    }*/

    public static void closeConnection(HttpURLConnection httpurlc) {
	if (null != httpurlc) {
	    httpurlc.disconnect();
	}
    }

}
