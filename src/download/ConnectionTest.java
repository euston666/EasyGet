package download;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Vector;


public class ConnectionTest{
    public static TestResult testURL(String urlStr){
        Vector printVector = new Vector();
        TestResult result=new TestResult();
        URL tmp_url;
        try{
            printVector.add("正在连接URL......");
            System.out.println("正在连接URL......");
            tmp_url=new URL(urlStr);
            HttpURLConnection http=(HttpURLConnection)tmp_url.openConnection();
            if (http.getResponseCode()>=400){
                printVector.add("资源服务器响应错误！");
                System.out.println("资源服务器响应错误！");
                result.url=null;
                result.status=-1;
                result.printVector = printVector;
                return result;
            }
            else{
                printVector.add("URL连接成功！");
                System.out.println("URL连接成功！");
                result.url=tmp_url;
                result.status=1;
                //获取文件长度
                result.fileLength=http.getContentLength();
                if (result.fileLength==-1){
                    printVector.add("资源无法获知文件长度！");
                    System.out.println("资源无法获知文件长度！"); 
                }
                else{
                    printVector.add("资源文件大小为"+result.fileLength+"Bytes");
                    System.out.println("资源文件大小为"+result.fileLength+"Bytes");
                }
                //获取文件名
                result.fileName=getFileName(http);
                printVector.add("资源文件名为"+result.fileName);
                System.out.println("资源文件名为"+result.fileName);
                //获取MIME列表
                result.http_MIME=getMIME(http);
                result.printVector = printVector;
                return result;
            }		
        }
        catch(MalformedURLException e1){
            printVector.add("URL格式错误！");
            System.out.println("URL格式错误！");
            result.url=null;
            result.status=-2;
            result.printVector = printVector;
            return result;
        }
        catch(IOException e2){
            printVector.add("URL连接错误！");
            System.out.println("URL连接错误！");
            result.url=null;
            result.status=-3;
            result.printVector = printVector;
            return result;
        }	
    }

    private static String getFileName(HttpURLConnection http){
        String filename=http.getURL().getFile();
        return filename;//.substring(filename.lastIndexOf("/")+1);
    }

    private static HashMap<String,String> getMIME(HttpURLConnection http){
        HashMap<String,String> http_MIME=new HashMap<String,String>();
        System.out.println("=====================资源MIME信息=======================");
        for(int i=0;;i++){ 
            String mine=http.getHeaderField(i); 
            if(mine==null)break;                  
            System.out.println(http.getHeaderFieldKey(i)+":"+mine); 
            http_MIME.put(http.getHeaderFieldKey(i),http.getHeaderField(i));
        } 
        System.out.println("======================================================");
        return http_MIME;
    }			
}	
class TestResult implements Serializable{
    public URL url;
    public int status;
    public int fileLength;
    public String fileName;
    public HashMap<String,String> http_MIME;
    public Vector printVector;
}	