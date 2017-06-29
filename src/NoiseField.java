/* OpenProcessing Tweak of *@*http://www.openprocessing.org/sketch/9299*@* */
/* !do not delete the line above, required for linking your tweak if you upload again */

import processing.core.*;
import java.util.ArrayList;
import ddf.minim.*;
import ddf.minim.analysis.*;


public class NoiseField extends PApplet {

  int width;
  int height;
    String position = "Tunnel";
  Tunnel tunnel;

  AudioInput audio;
  Minim minim;
  BeatDetect beat;

  double thickness = 1.0;

  public NoiseField(Tunnel t, int w, int h, String p) {
      width  = w;
      height = h;
      tunnel = t;
        position = p;
      audio = tunnel.in;
  }

  public void settings() {
      size(width, height);
  }

  int NUM_PARTICLES = 10;
  ParticleSystem p;
  public void setup()
  {
    minim = new Minim(this);
    beat = new BeatDetect();
    smooth();
    background(0);
    p = new ParticleSystem();
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

  public void draw(){
    synchronized (Tunnel.class) {
      track();
      //println(frameCount);
      noStroke();
      fill(0,5);
      rect(0,0,width,height);
      p.update();
      p.render();
    }
  }

  class Particle
  {
    PVector position, velocity;

    Particle()
    {
      position = new PVector(random(width),random(height));
      velocity = new PVector();
    }

    void update()
    {
      velocity.x = (float)(20*(noise(trackX/10+position.y/100)-0.5));  //change to kinect //<>//
      velocity.y = (float)(20*(noise(trackY/10+position.x/100)-0.5));
      position.add(velocity);

      if(position.x<0)position.x+=width;
      if(position.x>width)position.x-=width;
      if(position.y<0)position.y+=height;
      if(position.y>height)position.y-=height;
    }

    void render()
    {
      beat.detect(audio.mix);
      if(beat.isOnset()) {
        thickness += 2;
        strokeWeight((float)thickness);
      } else {
        thickness -= .05;
        if (thickness < 1.0) {
          thickness = 1.0;
        }
        strokeWeight((float)thickness);
      }
      stroke(blendColor(color(0,0,255), color(random(255),random(255),random(255)), ADD));
      line(position.x,position.y,position.x-velocity.x,position.y-velocity.y);
    }
  }

  class ParticleSystem
  {
    Particle[] particles;

    ParticleSystem()
    {
      particles = new Particle[NUM_PARTICLES];
      for(int i = 0; i < NUM_PARTICLES; i++)
      {
        particles[i]= new Particle();
      }
    }

    void update()
    {
      for(int i = 0; i < NUM_PARTICLES; i++)
      {
        particles[i].update();
      }
    }

    void render()
    {
      for(int i = 0; i < NUM_PARTICLES; i++)
      {
        particles[i].render();
      }
    }
  }
}