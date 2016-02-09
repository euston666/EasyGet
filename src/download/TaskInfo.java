package download;

import java.io.*;
import java.util.HashMap;
import java.util.Vector;

public class TaskInfo implements Serializable{
    private boolean isValidate;
    private String taskURL;
    private String fileName;
    private String saveFilePath;
    private TestResult testResult; 
    private int threadNum;
    private long predownLength; //用来计算下载速度
    private long downLength;	     
    private float downSpeed;
    private long[][] downPos;
    private boolean finished;
    private boolean error;
    private boolean stopped;
    private int taskID;
    private Vector printVector;
    private HashMap<String,String> http_MIME;
	     
    public TaskInfo(String urlStr,String saveFilePath,int threadNum, int taskID){
        this.taskURL=urlStr;
        this.fileName = saveFilePath.substring(saveFilePath.lastIndexOf(File.separator)+1);
        this.saveFilePath = saveFilePath;
        this.testResult=ConnectionTest.testURL(urlStr);
        this.printVector = testResult.printVector;
        this.http_MIME = testResult.http_MIME;
        if ((testResult.status==1)&&(testResult.fileLength>0)){
            this.isValidate=true;
            setThreadNum(threadNum);
            setSaveFile();
            setDownPos();
            this.taskID = taskID;
        }
        else{ //无响应的任务没有分配线程，没有设置本地文件。
            this.taskID = taskID;
            this.isValidate=false;
        }	     	           	      
    }

    public String getTaskURL(){
        return taskURL;
    }

    public String getSaveFilePath(){
        return saveFilePath;
    }	
    
    public String getFileName(){
        return fileName;
    }

    public boolean setSaveFile(){ // 创建本地文件
        File tmp_File=new File(saveFilePath);
        try{
            RandomAccessFile out=new RandomAccessFile(tmp_File,"rw");
            out.setLength(testResult.fileLength);
            out.close();
            return true;
        }
        catch(IOException e){
             return false;
        }		            
    }

    public int getThreadNum(){
        return threadNum;
    }

    public void setThreadNum(int num){
        if(!isValidate){
            return;
        }
        else{
            threadNum=num;
        }		
    }	
    
    public void setValidate(boolean b){
        this.isValidate = b;
    }

    public String getFileLength(){
        double KB = Math.pow(1024, 1);
        double MB = Math.pow(1024, 2);
        double GB = Math.pow(1024, 3);
        long bytes = testResult.fileLength;
        if(bytes>=GB){
            return String.format("%.2f", bytes/GB)+"GB";
        }
        else if(bytes>=MB){
            return String.format("%.2f", bytes/MB)+"MB";
        }
        else if(bytes>=KB){
            return String.format("%.2f", bytes/KB)+"KB";
        }
        else return bytes+"B";
    }

    public HashMap<String,String> getMIME(){
        return testResult.http_MIME;
    }	
    
    public void setDownLength(long len){
        downLength=downLength+len;
    }	

    public long getDownLength(){
        return downLength;
    }
    
    public int getDownProgress(){
        return Math.round(100.0f*getDownLength()/testResult.fileLength);
    }

    public int getDownSpeed(){
        downSpeed=(downLength-predownLength)/1024.0f; // KB/s
        predownLength=downLength;
        return Math.round(downSpeed);
    }

    public String getStatus(){
        if(isValidate==false){
            return "无响应";
        }
        else if(finished==true){
            return "完成";
        }
        else if(error==true){
            return "错误";
        }
        else if(stopped==true){
            return "暂停";
        }
        else{
            return "进行中";
        }
    }
    
    public int getTaskID(){
        return taskID;
    }
    
    public void setTaskID(int newID){
        this.taskID = newID;
    }

    public long[][] getDownPos(){
        return downPos;
    }	

    public void setDownPos(){
        downPos=new long[threadNum][2];
        long len=testResult.fileLength;
        long block=(len/threadNum+1);
        for(int i=0;i<threadNum;i++){
            len=len-block;
            downPos[i][0]=i*block;
            downPos[i][1]=block;
            if(len<0)downPos[i][1]=block+len;
        }	
    }	
    
    public Vector getPrintVector(){
        return printVector;
    }
    
    public HashMap<String,String> getHTTPMIME(){
        return http_MIME;
    }
    
    public boolean isValidate(){
        return isValidate;
    }

    public boolean isFinished(){
        return finished;
    }

    public void setFinished(boolean f){
        finished=f;
    }										

    public boolean isError(){
        return error;
    }				

    public void setError(boolean e){
        error=e;
    }	
    
    public boolean isStopped(){
        return stopped;
    }
    
    public void setStopped(boolean s){
        stopped = s;
    }
    
    public String toString(){
        return saveFilePath;
    }	
}	