/**
 * Created by shaileshvajpayee on 3/10/17.
 */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class Logger
{
    public BufferedWriter log;
    public String fileName;
    public boolean verbose;

    public Logger(String s)
    {
        fileName = s;
        verbose = true;
    }

    void setVerbose(boolean flag)
    {
        verbose = flag;
    }

    public void logMessage(String s)
    {
        try
        {
            if(log == null)
                log = new BufferedWriter(new FileWriter(new File(fileName)));
            s = (new Date()).toString() +" : " + s + "\n";
            log.write(s);
            if(verbose)
                System.out.println(s);
            log.flush();
        }
        catch(IOException ioexception)
        {
            System.out.println(ioexception);
        }
    }
}