import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.*;
import java.lang.Math.*;

class HLine extends PApplet {

  int width;
  int height;
  String position = "Tunnel";
  int c = color(random(255), random(255), random(255));
  Tunnel tunnel;

  // Audio Support
  AudioInput audio;
  Minim minim;
  BeatDetect beat;

  // Instance Variables
  float lastX = 0;


  public HLine(Tunnel t, int w, int h, String p) {
      width = w;
      height = h;
      tunnel = t;
      p = position;
      audio = tunnel.in;
  }

  public void settings()
  {
      size(width, height);
  }

  public void setup() {
    minim = new Minim(this);
    beat = new BeatDetect();

    // Additional Setup
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
           trackX = (float)(width * Main.kinect.RightHandDepthRatio * (Main.kinect.RightHandDepthRatio * 2));
           trackY = (float)height * Main.kinect.HandDistance;
           println(trackX);
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
      } else {
      }
       background(0);
      
      stroke(blendColor(c, color(random(255),random(255),random(255), 100), ADD));
      line(trackX,0,trackX,height);
      strokeWeight(tunnel.getAudioAverage());
      stroke(blendColor(color(255,0,0), color(random(255),random(255),random(255), 100), ADD));
      //line(0,trackY,width,trackY);
    }
  }
  
  // Additional Classes
}