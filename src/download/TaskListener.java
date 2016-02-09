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
        
        //����������ʱ����
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
        if(threads.length==0){ //��Դ��������Ӧ����û�������κ������߳�
            isError = true;
        }
        else{
            if(finishedNum==threads.length) isFinished = true; //�����߳���ɣ����������
            if(errorNum!=0) isError = true; //����error�̣߳������ش���
        } 
    }	
    
    private void refreshTable(){ //���±������
        mainFrame.getTable().setValueAt(taskInfo.getStatus(), taskInfo.getTaskID(), 0);
        mainFrame.getTable().setValueAt(taskInfo.getFileName(), taskInfo.getTaskID(), 1);
        mainFrame.getTable().setValueAt(taskInfo.getFileLength(), taskInfo.getTaskID(), 2);
        mainFrame.getTable().setValueAt(taskInfo.getDownProgress(), taskInfo.getTaskID(), 3);
        mainFrame.getTable().setValueAt(taskInfo.getDownSpeed()+"KB/s", taskInfo.getTaskID(), 4);
    }
    
    /*public void redownByOneThread(){ //ʹ�õ��߳��������ء�
        TaskInfo newdownInfo = new TaskInfo(taskInfo.getTaskURL(), taskInfo.getSaveFilePath(), 1, taskInfo.getTaskID());
        mainFrame.getTaskVector().setElementAt(newdownInfo, taskInfo.getTaskID()); //�滻���񼯺��ж�Ӧ������
        tableModel.removeRow(taskInfo.getTaskID());
        new Downloader(newdownInfo,mainFrame).download();
    }*/
    
    class MyTimerTask1 extends TimerTask{ //��������״̬����ɻ����
        public void run(){
            if(taskInfo.isValidate()){
                setListenStatus();
                if(isFinished){
                    taskInfo.setFinished(true);
                    tmr1.cancel();
                }
                else if(isError){
                    taskInfo.setError(true); //��ֹ���������߳�
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
                if(taskInfo.isFinished()){//������ؽ������رն�ʱ���������ļ�������Ϣ��־
                    refreshTable();//������һ����Ϣ
                    tmr2.cancel();  
                    if(taskInfo.getDownProgress()!=100){ //����������ɣ�������ȴ����100%��
                        System.out.println("�ļ� ["+taskInfo.getFileName()+"] ����ʧ��");
                        taskInfo.setFinished(false);
                        taskInfo.setError(true); //�����ΪError
                        refreshTable(); //ˢ������״̬
                    }
                    else{
                        System.out.println("�ļ� ["+taskInfo.getFileName()+"] �������");
                    }
                }
                else if(taskInfo.isError()){//��������رն�ʱ���������ļ����ر�־���Ա�������������߳�
                    refreshTable();//������һ����Ϣ
                    tmr2.cancel();  
                    if(threads.length==0||threads.length==1){ //��Դ��Ӧ���󣬻���ʹ�õ��߳�������Ȼʧ��
                        System.out.println("�ļ� ["+taskInfo.getFileName()+"] ����ʧ��"); 
                    }
                    else{
                        System.out.println("�ļ� ["+taskInfo.getFileName()+"] ���ش���");
                        tmr2.cancel();
                    }
                }
                else if(taskInfo.isStopped()){
                    refreshTable();
                    tmr2.cancel();
                    System.out.println("�ļ� ["+taskInfo.getFileName()+"] ��ͣ����");
                }
                else{//���û�������������ٶ�    	      	  
                    refreshTable();
                } 
            }  	
            else{ //������remove������ִ��
                tmr2.cancel();
            }
        }	
    }
}
	
