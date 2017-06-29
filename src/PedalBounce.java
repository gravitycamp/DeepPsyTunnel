import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.*;

class PedalBounce extends PApplet {

  int width;
  int height;
    String position = "Tunnel";
  Tunnel tunnel;

  // Audio Support
  AudioInput audio;
  Minim minim;
  BeatDetect beat;

  // Instance Variables

  public PedalBounce(Tunnel t, int w, int h, String p) {
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
    noStroke();

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
      trackX = trackY;
      beat.detect(audio.mix);
      float mag = 1;
      if(beat.isOnset()) {
        mag += .25;
      } else {
        mag -= .1;
      }
      background(0);
      translate(width/2, height/2);
      for (float q=30; q>=0; q-=0.1) {
        float r = (6*q+frameCount)%180;
        float f = map(r, 0, 180, 255, 0);
        fill((int)(255-f*sin(radians(r*trackY))), (int)255*sin(radians(r* trackY)), (int)f/2*sin(radians(r*trackY)));
        for (int i=0; i<360; i+=60) {
          float x = sin(radians(i))*r;
          float y = cos(radians(i))*r;
          float l = sin(radians(((trackX) /70 )*r-11*frameCount))*r/2*sin(radians(r));
          float s = 3*sin(radians(r));

          pushMatrix();
          translate(x, -100);
          rotate(radians(-i));
          if (s > 0) ellipse(l*2, 0, s, s);
          popMatrix();
        }
      }
      for (float q=30; q>=0; q-=0.1) {
        float r = (6*q+frameCount)%180;
        float f = map(r*tunnel.getAudioAverage(), 0, 180, 255, 0);
        fill((int)(255-f*sin(radians(r*trackY*mag))), (int)255*sin(radians(r* trackY*mag)), (int)f/2*sin(radians(r*trackY*mag)));
        for (int i=0; i<360; i+=60) {
          float x = sin(radians(i))*r;
          float y = cos(radians(i))*r;
          float l = sin(radians((mag*tunnel.getAudioAverage()/15)*r-11*frameCount))*r/2*sin(radians(r));
          float s = 3*sin(radians(r));

          pushMatrix();
          translate(x, y);
          rotate(radians(-i));
          if (s > 0) ellipse(l*7, 0, s, s);
          popMatrix();
        }
      }
       for (float q=30; q>=0; q-=0.1) {
        float r = (6*q+frameCount)%180;
        float f = map(r, 0, 180, 255, 0);
        fill((int)(255-f*sin(radians(r*trackY))), (int)255*sin(radians(r* trackY)), (int)f/2*sin(radians(r*trackY)));
        for (int i=0; i<360; i+=60) {
          float x = sin(radians(i))*r;
          float y = cos(radians(i))*r;
          float l = sin(radians(((trackX) /70 )*r-11*frameCount))*r/2*sin(radians(r));
          float s = 3*sin(radians(r));

          pushMatrix();
          translate(x, 100);
          rotate(radians(-i));
          if (s > 0) ellipse(l*2, 0, s, s);
          popMatrix();
        }
      }
    }
  }

  // Additional Classes
}