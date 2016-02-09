package download;

import java.io.*;
import java.net.*;

public class DownloadThread extends Thread{
    private InputStream in; //���������ļ�������
    private RandomAccessFile out; //�����ļ������
    private URL downURL;
    private TaskInfo taskInfo;
    private long startP;
    private long block; 
    private int threadID; 
    private boolean finished=false;
    private boolean error=false;
                     
    public DownloadThread(TaskInfo taskInfo,int threadID){
        this.taskInfo=taskInfo;  
        this.threadID=threadID;
    }
       
    public boolean isFinished(){
        return finished;
    }	

    public boolean isError(){
        return error;
    }	
    
    public void run(){
        HttpURLConnection http;
        int errCount = 0; //��������������
        quit:
        for(int i=0;; i++){
            try{
                if(taskInfo.isError()) break quit; 
                if(taskInfo.isStopped()) break quit;
                if(i>0){
                    Thread.sleep(1000); //��������·�������
                    System.out.println("�ļ� ["+taskInfo.getFileName()+"] �߳�"+threadID+"�����������ӷ�����......");
                }
            }
            catch(Exception e){
            }
            
            try{ 
                downURL=new URL(taskInfo.getTaskURL());
                http=(HttpURLConnection)downURL.openConnection();
                //setHeader(http);
                http.setConnectTimeout(5000); //��ֹ��Ϊ�����쳣������
                http.setReadTimeout(5000);
                
                startP = taskInfo.getDownPos()[threadID][0]; //�����߳̽�����Ϣ���������������ӷ������������
                block = taskInfo.getDownPos()[threadID][1];
                String sProperty = "bytes=" + startP + "-";
                http.setRequestProperty("Range",sProperty); 
                if(http.getResponseCode() != HttpURLConnection.HTTP_OK && http.getResponseCode() != HttpURLConnection.HTTP_PARTIAL){
                    error = true;
                    continue;
                }
                in=http.getInputStream(); 
                out=new RandomAccessFile(taskInfo.getSaveFilePath(),"rw"); 
            } 
            catch(IOException e1){ 
                System.out.println("�ļ� ["+taskInfo.getFileName()+"] �߳�"+threadID+"�����������");
                if((errCount++)>=19) break; //����20�α�����������ж��̴߳�������forѭ��
                continue; //�̱߳�������������·�������
            } 

            errCount = 0; //���ü���
            byte[] buffer=new byte[1024]; 
            int len=0; 
            long localSize=0; 
            long step = 0;
            System.out.println("�ļ� ["+taskInfo.getFileName()+"] �߳�"+threadID+"��ʼ����"); 
            
            try { 
                out.seek(startP);
                step=(block>1024)?1024:block; //��ȷ����in.read����ȡ����󳤶ȣ�����block<1024ʱ��len��׼ȷ������localSize����
                while (((len = in.read(buffer,0,(int)step))>0)&&localSize<=block) { //һ��Ҫ>0,����>-1��!-1,��Ϊ�������ļ�ĩ�ε��߳���ɺ󷵻�0
                    if(taskInfo.isStopped()) break;
                    if(taskInfo.isError()) break quit; //��һ�߳�error��ֱ����ֹ���������߳�
                    if(!taskInfo.isValidate()) break quit; //�����Ƴ�����ֹ�����߳�
                    out.write(buffer,0,len); 
                    localSize+=len; 
                    //�޸��ļ������߳̽�����Ϣ
                    taskInfo.getDownPos()[threadID][0]=startP+localSize;
                    taskInfo.getDownPos()[threadID][1]=block-localSize;
                    long tmp_block=block-localSize;
                    step=(tmp_block>1024)?1024:tmp_block;
                    //���������ļ�����
                    taskInfo.setDownLength(len);
                } 
                out.close(); 
                in.close(); 
                http.disconnect();
                
                if(taskInfo.isStopped()){
                    System.out.println("�ļ� ["+taskInfo.getFileName()+"] �߳�"+threadID+"��ͣ����");
                    break;
                }
                else if((block-localSize)!=0){ //len=-1����whileѭ�����²�ԭ����url����Ӧ����Դ�б仯��
                    System.out.println("�ļ� ["+taskInfo.getFileName()+"] �߳�"+threadID+"��ȡ���ݴ���");
                    //error = true;
                    //break;
                }
                else{
                    finished=true;
                    System.out.println("�ļ� ["+taskInfo.getFileName()+"] �߳�"+threadID+"�������"); 
                    break; //������ɣ�����ѭ������ֹ�߳�����������
                }
            }
            catch(Exception e2){ //in.read(buffer)�����쳣��java.net.SocketTimeoutException: Read timed out
                System.out.println("�ļ� ["+taskInfo.getFileName()+"] �߳�"+threadID+"��ȡ���ݳ�ʱ"); 
                try{
                    out.close();
                    in.close();
                    http.disconnect();   
                }
                catch(Exception e1){
                }	
                continue; //�߳����س������·�������
            } 
        } 
        if(errCount>19) error = true; //��������20���Բ��ɹ����ж����̴߳���
    }	      	
}
	