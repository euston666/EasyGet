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
            printVector.add("��������URL......");
            System.out.println("��������URL......");
            tmp_url=new URL(urlStr);
            HttpURLConnection http=(HttpURLConnection)tmp_url.openConnection();
            if (http.getResponseCode()>=400){
                printVector.add("��Դ��������Ӧ����");
                System.out.println("��Դ��������Ӧ����");
                result.url=null;
                result.status=-1;
                result.printVector = printVector;
                return result;
            }
            else{
                printVector.add("URL���ӳɹ���");
                System.out.println("URL���ӳɹ���");
                result.url=tmp_url;
                result.status=1;
                //��ȡ�ļ�����
                result.fileLength=http.getContentLength();
                if (result.fileLength==-1){
                    printVector.add("��Դ�޷���֪�ļ����ȣ�");
                    System.out.println("��Դ�޷���֪�ļ����ȣ�"); 
                }
                else{
                    printVector.add("��Դ�ļ���СΪ"+result.fileLength+"Bytes");
                    System.out.println("��Դ�ļ���СΪ"+result.fileLength+"Bytes");
                }
                //��ȡ�ļ���
                result.fileName=getFileName(http);
                printVector.add("��Դ�ļ���Ϊ"+result.fileName);
                System.out.println("��Դ�ļ���Ϊ"+result.fileName);
                //��ȡMIME�б�
                result.http_MIME=getMIME(http);
                result.printVector = printVector;
                return result;
            }		
        }
        catch(MalformedURLException e1){
            printVector.add("URL��ʽ����");
            System.out.println("URL��ʽ����");
            result.url=null;
            result.status=-2;
            result.printVector = printVector;
            return result;
        }
        catch(IOException e2){
            printVector.add("URL���Ӵ���");
            System.out.println("URL���Ӵ���");
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
        System.out.println("=====================��ԴMIME��Ϣ=======================");
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