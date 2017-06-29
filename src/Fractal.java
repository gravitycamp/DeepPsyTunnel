import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.*;

class Fractal extends PApplet {

  int width;
  int height;
    String position = "Tunnel";
  Tunnel tunnel;

  // Audio Support
  AudioInput audio;
  Minim minim;
  BeatDetect beat;


  // Instance Variables
  int num = 6; // size of matrix
  int numf; // ( size of matrix ) factorial

  float power = 4; // power in coloring algorithm
  float angle = TWO_PI; // angle in spread of branches
  float hueVal = 0; // hue value of connecting lines
  boolean spots = true; // draw dots on nodes, yes or no

  public Fractal(Tunnel t, int w, int h, String p) {
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
    colorMode(HSB);
    smooth();

    numf = 1; // calculate numf
    for(int i=1; i<num; i++) {
      numf *= i+1;
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
      background(0);
      track();
      angle = TWO_PI * trackY/width * 2; // mouseX controls angle range of nodes
      power = 1/(float)50.0 * trackX; // mouseY controls perspective of viewing nodes
      hueVal = 0;

      beat.detect(audio.mix);
      if(beat.isOnset()) {
        spots = true;
        angle += 100;
      } else {
        spots = false;
      }
       plotFrac(width/2,height/2,num,PI,TWO_PI); // plot the fractal
    }
  }

  // Classes
  public void plotFrac( float x, float y, int n, float stem, float range ) {
    float r; // distance between nodes
    float t; // angle between nodes

    // use an appropriate algorithm to calculate distance between given nodes
    r = ((float)0.5*power+2) * 10 * pow(n,power) * pow(num,-power);

    // if spots is on and we are on the first value of n, map initial node
    if( spots && n == num ) {
      ellipse(x,y,n+2,n+2);
    }

    // map nodes
    if( n>1 ) {

      for( int i=0; i<n; i++ ) { // step through each node to be created

        // determine/increment color
        stroke(hueVal,255,255,100);
        hueVal += 255.0/numf;

        // calculate angle of node from current "stem" angle, spread along "range" radians
        t = stem + range * (i+(float)0.5)/n - range/2;

        // draw line between nodes
        line( x, y, x+r*cos(t), y+r*sin(t) );

        if( spots ) { // if spots, draw node
          ellipse(x+r*cos(t), y+r*sin(t), n+2, n+2);
        }

        // spread nodes across PI radians
        // plotFrac(x+r*cos(t),y+r*sin(t),n-1,t,PI);

        // spread nodes across "angle" radians
        plotFrac(x+r*cos(t),y+r*sin(t),n-1,t,angle);
      }

    }
  }
}