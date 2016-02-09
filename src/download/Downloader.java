package download;

import gui.*;
import java.util.Vector;

public class Downloader{
    public TaskInfo taskInfo;
    private DownloadThread[] threads;
    private MainFrame mainFrame;

    public Downloader(TaskInfo taskInfo, MainFrame mainFrame){
        this.taskInfo=taskInfo;
        this.mainFrame = mainFrame;
    }	
    
    public void addItemToTable(){
        Vector tmp_vector = new Vector();
        tmp_vector.add(taskInfo.getStatus());
        tmp_vector.add(taskInfo.getFileName());
        tmp_vector.add(taskInfo.getFileLength());
        tmp_vector.add(taskInfo.getDownProgress());
        tmp_vector.add(taskInfo.getDownSpeed()+"KB/s");
        mainFrame.getTableModel().insertRow(taskInfo.getTaskID(), tmp_vector);
        mainFrame.getTable().repaint();
    }

    private void startThreads(){
        //开始下载
        threads=new DownloadThread[taskInfo.getThreadNum()];
        for(int i=0;i<threads.length;i++){
            threads[i]=new DownloadThread(taskInfo, i); 
            threads[i].setPriority(7); 
            threads[i].start(); 
        }	
    }

    public void download(){
        addItemToTable(); //在表格中添加一条下载记录
        if(taskInfo.isValidate()){ 
            startThreads(); //启动下载进程
            new TaskListener(taskInfo,threads,mainFrame);//创建监听器 
        }   
    }	     
}	