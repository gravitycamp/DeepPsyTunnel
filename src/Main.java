import processing.core.*;
import java.util.*;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.Runtime;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import KinectPV2.KJoint;
import KinectPV2.*;


class Main {

    //static String playlistPath = "/Users/skryl/Dropbox/dev/projects/gravity/tunnel/src/data/playlist.txt";
    static String playlistPath = "C:/TunnelGit2/src/data/playlist.txt";
    static Tunnel tunnel;
    static ArrayList<HashMap<String, String>> queue = new ArrayList();
    static int queueIndex = 0;
    static int duration = 100000;
    static int elapsedTime = 0;
    static Wire wire = new Wire();
    static Timer playTimer = new Timer();
    static Timer RestartTimer = new Timer();
    public static Kinect kinect;

    public static void setKinect(Kinect k) {
      kinect = k;
    }
    
    public static void main(String... args) {
       
        wire.SetupCom();
        loadPlaylist();

      //  tunnel.exec("C:/TunnelGit2/src/Restart.bat");      
        TimerTask exitApp = new TimerTask() {
        public void run() {
            //open("rundll32 SHELL32.DLL,ShellExec_RunDLL " + "C:/TunnelGit2/src/Restart.bat");
            //tunnel.launch("C:/TunnelGit2/src/Restart.bat");
            try {
              Runtime.getRuntime().exec("cmd.exe /c start C:\\TunnelGit2\\src\\Restart.bat");
            } catch(IOException ie) {ie.printStackTrace();}
            
            System.exit(0);
            }
        };
        
        RestartTimer.schedule(exitApp, new Date(System.currentTimeMillis()+30*60*1000)); //restart every 30 minutes
        
        while(true) {
          int playSeconds = 1000* Integer.parseInt(queue.get(queueIndex).get("Time"));
          long millis = System.currentTimeMillis();
          try{
              loadNextInQueue();
              Thread.sleep(playSeconds - millis % 1000);
          } catch(InterruptedException e) {
              System.out.println("got interrupted!");
          }
        }
    }

    public static void loadNextInQueue() {
        try { tunnel.kill(); } catch(Exception e) {}
        tunnel = new Tunnel(queue.get(queueIndex), wire);
        System.out.println(queueIndex);
        PApplet.runSketch(new String[]{"Tunnel"}, tunnel);
 

        elapsedTime = 0;
        queueIndex = ++queueIndex % queue.size();
    }

    public static void loadPlaylist() {
        try (Stream<String> stream = Files.lines(Paths.get(playlistPath))) {
            Object[] lines = stream.toArray();

            for (Object line: lines) {
                HashMap<String, String> mapping = new HashMap();

                String[] sketches = ((String) line).split(",");
                mapping.put("Time", sketches[0].trim());
                if (sketches.length == 2) {
                   mapping.put("Tunnel", sketches[1].trim());
                } else if (sketches.length == 3) {
                    mapping.put("Wall", sketches[1].trim());
                    mapping.put("Ceil", sketches[2].trim());
                } else {
                    mapping.put("LWall", sketches[1].trim());
                    mapping.put("Ceil", sketches[2].trim());
                    mapping.put("RWall",  sketches[3].trim());

                }
                queue.add(mapping);
            }
        } catch (IOException e) {
        }
    }
    
    
}