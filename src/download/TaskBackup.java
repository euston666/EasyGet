package download;

import gui.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.swing.JOptionPane;

public class TaskBackup extends Thread {
    private Vector taskVector;
    
    public TaskBackup(MainFrame mainFrame){
        this.taskVector = mainFrame.getTaskVector();
        new Timer().schedule(new BackupTask(), 0, 100);
    }
    
    class BackupTask extends TimerTask{ //软件退出时，两个备份至少有一个是完整的
        public void run(){
            ObjectOutputStream oos1 = null;
            ObjectOutputStream oos2 = null;
            try{
                oos1 = new ObjectOutputStream(new FileOutputStream("."+File.separator+"tasksBackup1"));
                oos1.writeObject(taskVector);
                oos2 = new ObjectOutputStream(new FileOutputStream("."+File.separator+"tasksBackup2"));
                oos2.writeObject(taskVector);
            }
            catch(Exception e){
            }
            finally{
                try{
                    oos1.close();
                    oos2.close();
                }
                catch(Exception e2){
                }
            }
        }
    }
}
