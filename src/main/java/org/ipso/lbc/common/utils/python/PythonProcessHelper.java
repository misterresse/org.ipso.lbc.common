package org.ipso.lbc.common.utils.python;

import org.ipso.lbc.common.exception.CmdInvokeFailedException;
import org.ipso.lbc.common.exception.CrossInvokeErrorException;
import org.ipso.lbc.common.exception.OperationTimeoutException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
public class PythonProcessHelper {
    public PythonProcessHelper() {
    }
    private PrintStream printStream;
    private String printStrings;
    private String scriptPath;
    private String paramString="";

    public String getPrintStrings() {
        return printStrings;
    }

    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public void setTimeOut(Integer second){
        this.ms = second * 1000;

    }
    public void setTimeOutMs(Integer ms){
        this.ms = ms;

    }
    public void addParameters(String... parameters){
        for (int i = 0; i < parameters.length; i++) {
            paramString+=" "+parameters[i];
        }
    }

    public String getCommandLine(){
        return "python " + scriptPath+paramString;
    }


    private Process pr;
    private Timer timer = new Timer();
    private Boolean forceExit ;

    private Integer ms = 1 * 60 * 60 * 1000;
    public void process() throws CmdInvokeFailedException,CrossInvokeErrorException {
        forceExit = false;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                pr.destroy();
                forceExit = true ;
            }
        }, ms);

        printStrings = "";
        try {
            Boolean nothingPrinted;//indicate that whether the target command line cannot be invoke
            Boolean pythonProcessError;//if the python script prints 'ERROR', set true
            pr = Runtime.getRuntime().exec(getCommandLine());
            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while (true) {
                if((line = in.readLine()) != null){
                    printStrings += line;
                } else {
                    if (forceExit){
                        timer.cancel();
                        throw new OperationTimeoutException("Python script hasn't return before time out: " + ms /1000 + "s.");
                    }
                    break;
                }
            }


            in.close();
            pr.waitFor();
            pythonProcessError = printStrings.toUpperCase().matches("(.*)ERR(.*)");

            nothingPrinted = printStrings.equals("");

            if (nothingPrinted){
                throw new CmdInvokeFailedException("Perhaps failed to invoke command line: "+getCommandLine()+", because [nothing is printed].");
            }
            if (pythonProcessError){
                throw new CrossInvokeErrorException("ERROR occurs when executing command line: "+getCommandLine()+".");
            }
            whenSuccess(printStrings);

        } catch (IOException e) {
            whenFailed(printStrings);
            throw new CmdInvokeFailedException(e);
        } catch (InterruptedException e) {
            whenFailed(printStrings);
            throw new CmdInvokeFailedException(e);
        }
    }




    private void whenSuccess(String str){
        // DO NOTHING
    }
    private void whenFailed(String str){
        // DO NOTING
    }
}
