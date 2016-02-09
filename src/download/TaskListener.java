package download;

import gui.*;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class TaskListener{
    private TaskInfo taskInfo;
    private Timer tmr1;
    private Timer tmr2;
    private boolean isFinished;
    private boolean isError;
    private DownloadThread[] threads;
    private MainFrame mainFrame;
    private DefaultTableModel tableModel;

    public TaskListener(TaskInfo taskInfo,DownloadThread[] threads,MainFrame mainFrame){
        this.taskInfo=taskInfo;
        this.threads=threads;
        this.mainFrame=mainFrame;
        this.tableModel = (DefaultTableModel)mainFrame.getTable().getModel();
        
        //启动两个定时任务
        tmr1=new Timer();
        tmr1.schedule(new MyTimerTask1(), 0, 10);
        tmr2=new Timer();
        tmr2.schedule(new MyTimerTask2(),0,1000);
    }	

    private void setListenStatus(){
        int finishedNum = 0;
        int errorNum = 0;
        for(int i=0;i<threads.length; i++){
            if(threads[i].isFinished()==true) finishedNum++;
            if(threads[i].isError()==true) errorNum++;
        }
        if(threads.length==0){ //资源服务器响应错误，没有启动任何下载线程
            isError = true;
        }
        else{
            if(finishedNum==threads.length) isFinished = true; //所有线程完成，则下载完成
            if(errorNum!=0) isError = true; //出现error线程，则下载错误
        } 
    }	
    
    private void refreshTable(){ //更新表格数据
        mainFrame.getTable().setValueAt(taskInfo.getStatus(), taskInfo.getTaskID(), 0);
        mainFrame.getTable().setValueAt(taskInfo.getFileName(), taskInfo.getTaskID(), 1);
        mainFrame.getTable().setValueAt(taskInfo.getFileLength(), taskInfo.getTaskID(), 2);
        mainFrame.getTable().setValueAt(taskInfo.getDownProgress(), taskInfo.getTaskID(), 3);
        mainFrame.getTable().setValueAt(taskInfo.getDownSpeed()+"KB/s", taskInfo.getTaskID(), 4);
    }
    
    /*public void redownByOneThread(){ //使用单线程重新下载。
        TaskInfo newdownInfo = new TaskInfo(taskInfo.getTaskURL(), taskInfo.getSaveFilePath(), 1, taskInfo.getTaskID());
        mainFrame.getTaskVector().setElementAt(newdownInfo, taskInfo.getTaskID()); //替换任务集合中对应的任务
        tableModel.removeRow(taskInfo.getTaskID());
        new Downloader(newdownInfo,mainFrame).download();
    }*/
    
    class MyTimerTask1 extends TimerTask{ //监听任务状态（完成或出错）
        public void run(){
            if(taskInfo.isValidate()){
                setListenStatus();
                if(isFinished){
                    taskInfo.setFinished(true);
                    tmr1.cancel();
                }
                else if(isError){
                    taskInfo.setError(true); //终止所有下载线程
                    tmr1.cancel();
                }
            }
            else{
                taskInfo.setError(true); 
                tmr1.cancel();
            }
        }
    }

    class MyTimerTask2 extends TimerTask{
        public void run(){
            if(taskInfo.isValidate()){ 
                if(taskInfo.isFinished()){//如果下载结束，关闭定时器，更新文件下载信息标志
                    refreshTable();//最后更新一次信息
                    tmr2.cancel();  
                    if(taskInfo.getDownProgress()!=100){ //任务下载完成，但进度却不是100%！
                        System.out.println("文件 ["+taskInfo.getFileName()+"] 下载失败");
                        taskInfo.setFinished(false);
                        taskInfo.setError(true); //任务标为Error
                        refreshTable(); //刷新任务状态
                    }
                    else{
                        System.out.println("文件 ["+taskInfo.getFileName()+"] 下载完毕");
                    }
                }
                else if(taskInfo.isError()){//如果出错，关闭定时器，更新文件下载标志，以便结束所有下载线程
                    refreshTable();//最后更新一次信息
                    tmr2.cancel();  
                    if(threads.length==0||threads.length==1){ //资源响应错误，或者使用单线程下载依然失败
                        System.out.println("文件 ["+taskInfo.getFileName()+"] 下载失败"); 
                    }
                    else{
                        System.out.println("文件 ["+taskInfo.getFileName()+"] 下载错误");
                        tmr2.cancel();
                    }
                }
                else if(taskInfo.isStopped()){
                    refreshTable();
                    tmr2.cancel();
                    System.out.println("文件 ["+taskInfo.getFileName()+"] 暂停下载");
                }
                else{//如果没出错，计算下载速度    	      	  
                    refreshTable();
                } 
            }  	
            else{ //若任务被remove，则不再执行
                tmr2.cancel();
            }
        }	
    }
}
	
