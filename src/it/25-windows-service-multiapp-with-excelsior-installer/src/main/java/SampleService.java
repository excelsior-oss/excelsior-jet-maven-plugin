import java.util.*;
import java.io.*;
import java.text.*;
import javax.swing.*;
import java.awt.event.*;

public class SampleService extends com.excelsior.service.WinService {
    private Object pauseLock = new Object();
    private static DateFormat formatter = null;
    private volatile boolean paused = false;

    static {
        formatter = new SimpleDateFormat("HH:mm:ss dd MMM yyyy", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
    }

    public void run() {
        logInfoEvent("run() method called, service started");

        for (; ; ) {
            if (paused) {

                // locking service run thread

                synchronized (pauseLock) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException t) {
                    }
                }
            }

            // working...

            logInfoEvent("service is working");
            logInfoEvent("current date: " + formatter.format(new Date()));

            try {
                Thread.sleep(15000);
            } catch (InterruptedException t) {
            }
        }
    }

    public boolean init() {
        // things to do before service run

        logInfoEvent("init() method called, initializing service...");

        for (String arg: getArgs()) {
            logInfoEvent(arg);
        }

        // set timeouts...

        setInitTimeout(1000);
        setPauseTimeout(1000);
        setResumeTimeout(1000);
        setStopTimeout(1000);

        logInfoEvent("service initialized");

        return true;
    }

    public void shutdown() {
        // things to do before service shutdown
    }

    public void stop() {
        // things to do before service stop

        logInfoEvent("stop() method called, service is stopped");
    }

    public void pause() {
        // things to do before service pause

        logInfoEvent("pause() method called, service is paused");
        paused = true;
    }

    public void resume() {
        // things to do before service resume

        logInfoEvent("resume() method called, service is resumed");
        paused = false;

        synchronized (pauseLock) {

            // unlocking service run thread
            pauseLock.notify();
        }
    }


    public static void main(String args[]) {
        JFrame frame = new JFrame("HelloSwing");
        frame.setSize(50, 50);
        JLabel label = new JLabel("Hello, Swing!", 0);
        frame.add(label);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowevent) {
                System.exit(0);
            }
        });
        frame.show();
    }

}