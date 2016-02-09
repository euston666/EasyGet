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
        //��ʼ����
        threads=new DownloadThread[taskInfo.getThreadNum()];
        for(int i=0;i<threads.length;i++){
            threads[i]=new DownloadThread(taskInfo, i); 
            threads[i].setPriority(7); 
            threads[i].start(); 
        }	
    }

    public void download(){
        addItemToTable(); //�ڱ�������һ�����ؼ�¼
        if(taskInfo.isValidate()){ 
            startThreads(); //�������ؽ���
            new TaskListener(taskInfo,threads,mainFrame);//���������� 
        }   
    }	     
}	