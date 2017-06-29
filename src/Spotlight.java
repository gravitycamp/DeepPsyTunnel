import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.*;

class Spotlight extends PApplet {
  int width;
  int height;
  String position = "Tunnel";
  
  Tunnel tunnel;

  // Audio Support
  AudioInput audio;
  Minim minim;
  BeatDetect beat;
  int baseColor = (int)color(0,0,255);

  // Instance Variables

  public Spotlight(Tunnel t, int w, int h, String p) {
      width = w;
      height = h;
      tunnel = t;
      audio = tunnel.in;
      position = p;
  }

  public void settings()
  {
      size(width, height);
  }

  public void setup() {
    minim = new Minim(this);
    beat = new BeatDetect();

    // Additional Setup
    smooth();
    background(0);
  }

  float trackX = 0;
  float trackY = 0;
  float trackZ = 0;

  public void track() {
   if(Main.kinect != null) {
       Main.kinect.update();
       switch (position) {
         case "Tunnel":
         case "Wall":
         case "Ceil":
           trackX = (float)width * (Main.kinect.RightHandDepthRatio + Main.kinect.LeftHandDepthRatio)/2;
           trackY = (float)height * Main.kinect.HandDistance;
           trackZ = 0; 
           break;
         case "RWall":
           trackX = (float)width * Main.kinect.RightHandDepthRatio;
           trackY = (float)height * Main.kinect.RightHandRaisedRatio;
           trackZ = 0;
           break;
         case "LWall":
           trackX = (float)width * Main.kinect.LeftHandDepthRatio;
           trackY = (float)height * Main.kinect.LeftHandRaisedRatio;
           trackZ = 0;
           break;
       }
   } else {
       trackX = mouseX;
       trackY = mouseY;
   }
  }

  public void draw() {
    synchronized (Tunnel.class) {
      track();
      beat.detect(audio.mix);
      if(beat.isOnset()) {
        switch((int)random(1,3)) {
          case 1:
            baseColor = (int)color(0,0,255);
            break;
          case 2: 
             baseColor = (int)color(0,255,0);
            break;
          case 3:
            baseColor = (int)color(255,0,0);
            break;
        }
      }


        int c1 =  (int)random(0,255);
        int c2 =  (int)random(0,255);
        int c3 =  (int)random(0,255);
        int s = (int)random(0,tunnel.getAudioAverage());
        int t = (int)random(0,100);

        fill(0,0,0,50);
        noStroke();

        rect(0,0,800,800);
        fill((int)blendColor(baseColor, color(random(255),random(255),random(255)), ADD),t);
        ellipse(trackX,trackY,s,s);
    }
  }

  // Additional Classes
}