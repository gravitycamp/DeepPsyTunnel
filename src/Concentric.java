import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.*;

class Concentric extends PApplet {

  int width;
  int height;
  String position = "Tunnel";
  Tunnel tunnel;

  // Audio Support
  AudioInput audio;
  Minim minim;
  BeatDetect beat;

  // Instance Variables
  float theta = 0;
  float thetaInc = (float)0.08;
  int amp = 6;
  float s = 5;
  float r = 255;
  int rInc = 30; 
  int num = 40;
  PVector mouse;
  float dmouse;
  PVector center;


  public Concentric(Tunnel t, int w, int h, String p) {
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
    smooth();
    background(0);
    center = new PVector(width/2, height/2);
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
      } else {
      }
     background(0);
        translate(width/2, height/2);
        
        if (mousePressed == true) {
          s = amp*sin(theta) + amp;
          r = 255*sin(theta+ PI/3) + 255;
          theta += thetaInc;
        } 
        
         mouse = new PVector(trackX, trackY);
         dmouse = mouse.dist(center);
        
        float t = map(trackX, 0, width, 0, TWO_PI);
        float u = map(trackY, 0, height, 0, TWO_PI);
        int a = (int)(map(trackX + trackY, 0, width, 0, 255));
        int b = (int)(map(trackY, 0, width, 0, 255));
        int c = (int)(map(trackY, 0, width, 0, 255));
        
        int col = color(a, b, c);
        noFill();
        stroke(col, r);
        strokeWeight(s * tunnel.getAudioAverage()/10);
        strokeCap(SQUARE);
        
         for (int i = 0; i < num; i++){
          float radius = i*rInc;
          float angle = i*i*PI/10;;
         pushMatrix();
        rotate(u);
        arc(0, 0, radius, radius, angle, angle + t);
        popMatrix();
         }
    }
  }
  
  // Additional Classes
}