package download;

import java.io.*;
import java.net.*;

public class DownloadThread extends Thread{
    private InputStream in; //网络下载文件输入流
    private RandomAccessFile out; //下载文件输出流
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
        int errCount = 0; //报文申请错误计数
        quit:
        for(int i=0;; i++){
            try{
                if(taskInfo.isError()) break quit; 
                if(taskInfo.isStopped()) break quit;
                if(i>0){
                    Thread.sleep(1000); //两秒后重新发起连接
                    System.out.println("文件 ["+taskInfo.getFileName()+"] 线程"+threadID+"正在重新连接服务器......");
                }
            }
            catch(Exception e){
            }
            
            try{ 
                downURL=new URL(taskInfo.getTaskURL());
                http=(HttpURLConnection)downURL.openConnection();
                //setHeader(http);
                http.setConnectTimeout(5000); //防止因为网络异常而僵死
                http.setReadTimeout(5000);
                
                startP = taskInfo.getDownPos()[threadID][0]; //更新线程进度信息（适用于重新连接服务器的情况）
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
                System.out.println("文件 ["+taskInfo.getFileName()+"] 线程"+threadID+"报文申请错误！");
                if((errCount++)>=19) break; //连续20次报文申请错误，判定线程错误，跳出for循环
                continue; //线程报文申请错误，重新发起连接
            } 

            errCount = 0; //重置计数
            byte[] buffer=new byte[1024]; 
            int len=0; 
            long localSize=0; 
            long step = 0;
            System.out.println("文件 ["+taskInfo.getFileName()+"] 线程"+threadID+"开始下载"); 
            
            try { 
                out.seek(startP);
                step=(block>1024)?1024:block; //精确控制in.read所读取的最大长度，否则当block<1024时，len不准确，导致localSize出错。
                while (((len = in.read(buffer,0,(int)step))>0)&&localSize<=block) { //一定要>0,不能>-1或!-1,因为不负责文件末段的线程完成后返回0
                    if(taskInfo.isStopped()) break;
                    if(taskInfo.isError()) break quit; //任一线程error，直接终止其他所有线程
                    if(!taskInfo.isValidate()) break quit; //任务被移除，终止所有线程
                    out.write(buffer,0,len); 
                    localSize+=len; 
                    //修改文件下载线程进度信息
                    taskInfo.getDownPos()[threadID][0]=startP+localSize;
                    taskInfo.getDownPos()[threadID][1]=block-localSize;
                    long tmp_block=block-localSize;
                    step=(tmp_block>1024)?1024:tmp_block;
                    //更改下载文件长度
                    taskInfo.setDownLength(len);
                } 
                out.close(); 
                in.close(); 
                http.disconnect();
                
                if(taskInfo.isStopped()){
                    System.out.println("文件 ["+taskInfo.getFileName()+"] 线程"+threadID+"暂停下载");
                    break;
                }
                else if((block-localSize)!=0){ //len=-1跳出while循环，猜测原因是url所对应的资源有变化。
                    System.out.println("文件 ["+taskInfo.getFileName()+"] 线程"+threadID+"获取数据错误");
                    //error = true;
                    //break;
                }
                else{
                    finished=true;
                    System.out.println("文件 ["+taskInfo.getFileName()+"] 线程"+threadID+"完成下载"); 
                    break; //下载完成，跳出循环，防止线程重新启动。
                }
            }
            catch(Exception e2){ //in.read(buffer)发生异常，java.net.SocketTimeoutException: Read timed out
                System.out.println("文件 ["+taskInfo.getFileName()+"] 线程"+threadID+"获取数据超时"); 
                try{
                    out.close();
                    in.close();
                    http.disconnect();   
                }
                catch(Exception e1){
                }	
                continue; //线程下载出错，重新发起连接
            } 
        } 
        if(errCount>19) error = true; //报文申请20次仍不成功，判定该线程错误
    }	      	
}
	