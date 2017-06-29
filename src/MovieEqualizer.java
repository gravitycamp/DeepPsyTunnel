import processing.core.*;
import ddf.minim.*;
import ddf.minim.signals.*;
import ddf.minim.analysis.*;

class MovieEqualizer extends PApplet {
    Visualizer classicVi;

    PImage fade;
    float rWidth, rHeight;
    Minim minim;
    AudioInput in;
    FFT fft;

    int width;
    int height;
    String position = "Tunnel";
    Tunnel tunnel;


    public MovieEqualizer(Tunnel t, int w, int h, String p) {
        width = w;
        height = h;
        tunnel = t;
        position = p;
    }


    public void settings() {
        size(width, height);
    }

    public void setup() {
        //program setup
       // frameRate(60);

        rectMode(CORNERS);
        background(0);
        //background fader parameters
        fade = get(0, 0, width, height);// parameter for making the background fade
        rWidth = width * (float) 0.99;
        rHeight = height * (float) 0.99;
        classicVi = new Visualizer(); // initializes the class visualizer
    }

    public void draw() {
      try{
        synchronized (Tunnel.class) {
            classicVi.drawEQ();

        }
 } catch (Exception e) {
        }

    }


    class Visualizer {

        PImage fade;
        float rWidth, rHeight;
        int hVal;
        int w;

        public Visualizer() {
            hVal = 0;
            rectMode(CORNERS);
            fade = get(0, 0, width, height); //<>//
            rWidth = width * (float) 0.99; //<>//
            rHeight = height * (float) 0.99;
        }
 //<>//
        void drawEQ() { //<>//
            //creates the values needed to make the 3D effect
            background(0); //<>//
            tint(255, 255, 255, 254); //<>//
            image(fade, (width - rWidth) / 2, (height - rHeight) / 2, rWidth, rHeight); //<>//
            noTint(); //<>//
 //<>//
            // rainbow Effect parameters //<>//
            smooth(); //<>// //<>//
            colorMode(HSB);// sets color mode value //<>//
            fill(hVal, 255, 255);//cycles through hue and brightness to expose a greater color palete //<>//
            stroke(hVal, 255, 225);// sets the stroke to cycle through the whole color spectrum
            colorMode(RGB);//sets color mode back to Red green and blue //<>//
            //fill(EQColorR,EQColorG,EQColorB);
 //<>//
            //for loop for creating the audio bars
            w=width/fft.avgSize();
            strokeWeight(w);//the width of line will be equally spacing
            strokeCap(SQUARE);
            int numBars  = fft.avgSize();
            int barWidth = width / numBars ;
            
             for(int i=0; i<fft.avgSize();i++)
             {
                
               line((i*w)+(w),height,(i*w)+(w),height-fft.getAvg(i)*5);
              // ellipse((i*w)+(w/2),height-fft.getAvg(i)*10,40,40);
             }
             
            fade=get(0,0,width,height);
            stroke(255);
            for(int i=0; i<fft.avgSize();i++){
             line((i*w)+(w),height,(i*w)+(w),height-fft.getAvg(i)*5);
            }
 
            

//            for (int i = 0; i < numBars; i += 1)// specSize is changing the range of analysis
//            {
//                int start = i*barWidth;
//                rect(start, height, start + barWidth, height - fft.getAvg(i) * 50);// draws a rect and alters its height based on the translated frequency
//                rect(start, 0,      start + barWidth, 0      + fft.getAvg(i) * 50);
//            }
//            fade = get(0, 0, width, height);
            hVal += 1;

            if (hVal > 255) {
                hVal = 0;
            }
        }


    }
}