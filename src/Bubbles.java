import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.*;

class Bubbles extends PApplet {

  int width;
  int height;
    String position = "Tunnel";
  Tunnel tunnel;

  // Audio Support
  AudioInput audio;
  Minim minim;
  BeatDetect beat;

  // Instance Variables
  int num=200;
  SpreadC[]sc=new SpreadC[num];

  public Bubbles(Tunnel t, int w, int h, String p) {
      width = w;
      height = h;
      tunnel = t;
        position = p;
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
    for (int i=0; i<num; i++) {
      sc[i]=new SpreadC(trackX, trackY, random(-1, 1), random(-1, 1), random(1, 30),
      random(255), random(255), random(255));
    }
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

      background(0);
      for (int i=0; i<num; i++) {
        sc[i].update();
        sc[i].display();
      }
    }
  }

  // Additional Classes

  class SpreadC {
    float X, Y, amX, amY, es, R, G, B;
    SpreadC(float nX, float nY, float nAmX, float nAmY, float nEs,
    float nR, float nG, float nB) {
      X=nX;
      Y=nY;
      amX=nAmX;
      amY=nAmY;
      es=nEs;
      R=nR;
      G=nG;
      B=nB;
    }

    void update() {
      X=X+amX;
      Y=Y+amY;
      es-=0.1;
      if (es<0) {
        X=trackX;
        Y=trackY;
        amX=random(-1, 1);
        amY=random(-1, 1);
        es=random(1, 50);
        R=random(255);
        G=random(255);
        B=random(255);
      }
    }

    void display() {
      if(beat.isOnset()) {
       strokeWeight(tunnel.getAudioAverage());
      } 
      stroke(random(255),random(255),random(255));
      point(X, Y);
      noStroke();
      fill(R, G, B, 100);
      ellipse(X, Y, es, es);
      fill(255, 20);
      text(X, X+10, Y+10);
      text(Y, X+10, Y+20);
    }
  }
}