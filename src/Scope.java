import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.*;

class Scope extends PApplet {

  int width;
  int height;
    String position = "Tunnel";
  Tunnel tunnel;

  // Audio Support
  AudioInput audio;
  Minim minim;
  BeatDetect beat;

  // Instance Variables
  final int NB_MAX = 6;
  int NB = (int)random(2, NB_MAX);
  int RADIUS = 700;
  PVector prevPoint1 = new PVector(0, 0);
  PVector prevPoint2 = new PVector(0, 0);
  float R;
  float G;
  float B;
  float Rspeed;
  float Gspeed;
  float Bspeed;
  float count = 0;

  public Scope(Tunnel t, int w, int h, String p) {
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
    RADIUS = height;
    // Additional Setup
    background(0);
    strokeWeight(2);
    generateColors();
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
  
  public void generateColors()
  {
    R = random(255);
    G = random(255);
    B = random(255);
    Rspeed = (random(1) > .5 ? 1 : -1) * random((float).8, (float)1.5);
    Gspeed = (random(1) > .5 ? 1 : -1) * random((float).8, (float)1.5);
    Bspeed = (random(1) > .5 ? 1 : -1) * random((float).8, (float)1.5);
  }
  
  public void draw() {
    synchronized (Tunnel.class) {
      track();
      ellipse(trackX, trackY, tunnel.getAudioAverage() / 4, tunnel.getAudioAverage() / 4);
      RADIUS = (int)tunnel.getAudioAverage() * 6 ;
      beat.detect(audio.mix);
      
      smooth();
      float thickness = trackY / 30;
      if ( thickness < 1) {
        strokeWeight(1);
      } else {
        if (thickness < 20)
          strokeWeight(thickness);
        else 
          strokeWeight(random(4));
          
      }

      fill(120, 10);
      noStroke();

      translate(width/2, height/2);
      float thetaD = map(trackX*2, 0, width, (float)-.05, (float).05);
      float theta = random(TWO_PI / NB);

      Rspeed = ((R += Rspeed) > 255 || (R < 0)) ? -Rspeed : Rspeed;
      Gspeed = ((G += Gspeed) > 255 || (G < 0)) ? -Gspeed : Gspeed;
      Bspeed = ((B += Bspeed) > 255 || (B < 0)) ? -Bspeed : Bspeed;
      int myColor = color(R, G, B);
      
      if(beat.isOnset()) {
        myColor = 0;
        strokeWeight(tunnel.getAudioAverage()*10);
      }
      
      fill(myColor, 70);
      stroke(myColor, 70);
      float angle = trackX * random(TWO_PI);

      float radius = trackX * 3* random(8);
      if (radius < 4) {
        radius = random(8);
      }

      float tmpX = prevPoint1.x + radius * cos(angle);
      float tmpY = prevPoint1.y + radius * sin(angle);

      //adding the mouse rotation
      float x = tmpX * cos(thetaD) - tmpY * sin(thetaD);
      float y = tmpY * cos(thetaD) + tmpX * sin(thetaD);


      if (x * x + y * y > RADIUS * RADIUS)
      {
        x = RADIUS * cos(atan2(prevPoint1.y, prevPoint1.x));
        y = RADIUS * sin(atan2(prevPoint1.y, prevPoint1.x));
      }

      for (int i = 0; i < NB; i++)
      {
        rotate(TWO_PI / NB);
        //    point(x, y);
        //    point(x, -y);
        line(prevPoint1.x, prevPoint1.y, x, y);
        line(prevPoint2.x, prevPoint2.y, x, -y);
        //    ellipse(x, y, radius, radius);
        //    ellipse(x, -y, radius, radius);
      }
      prevPoint1 = new PVector(x, y);
      prevPoint2 = new PVector(x, -y);
    }
   
  }

  // Additional Classes
}