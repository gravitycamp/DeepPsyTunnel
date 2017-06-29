/* OpenProcessing Tweak of *@*http://www.openprocessing.org/sketch/70780*@* */ //<>// //<>//
/* !do not delete the line above, required for linking your tweak if you upload again */
import java.util.*;
import java.lang.reflect.*;
import java.awt.geom.*;
import java.awt.image.*;
import processing.video.*;
import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.*;
import java.util.*;

public class AudioInputKinect extends PApplet {

    int width;
    int height;
    String position = "Tunnel";
    Tunnel tunnel;
    FFT fft;
    Minim minim;
    //AudioInput in;

    PImage dot;
    PImage colors;
    float[] fftFilter;
    float spin = (float)0.003;
    float radiansPerBucket = radians(2);
    float decay = (float)0.96;
    float opacity = 20;
    float minSize = (float)0.1;
    float sizeScale = (float)0.4;
    float textScale = 0;
    PGraphics LeftWall;
    PGraphics RightWall;
    PGraphics Roof;
    double StartTime=0;
    double patternTime = 0;
    int R = (int)random(255);
    int G = (int)random(255);
    int B = (int)random(255);
    int scale = 4;  
    int counter = 0;
    int dR = 1;
    int dG = 1;
    int dB = 1;
    

    public AudioInputKinect(Tunnel t, int w, int h, String p) {
        width = w;
        height = h;
        tunnel = t;
        position = p;
        fft = tunnel.fft;
     }

    public void settings() {
        size(width, height);
    }

    public void setup() {
        //dot = loadImage("src/data/dot.png");
        //colors = loadImage("src/data/colors.png");
        dot = loadImage("C:/TunnelGit2/src/data/dot.png");
        colors = loadImage("C:/TunnelGit2/src/data/colors.png");
        fftFilter = new float[fft.specSize()];

        LeftWall= createGraphics(150*scale, 32*scale);
        RightWall= createGraphics(150*scale, 32*scale);
        Roof= createGraphics(150*scale, 24*scale);
        StartTime = millis();
    }


  float trackX = 0;
  float trackY = 0;
  float trackZ = 0;
  
    public void draw() {
      try{
        synchronized(Tunnel.class) {
            Main.kinect.update();
            background(0);
            LeftWall.beginDraw();
            LeftWall.background(0);
            RightWall.beginDraw();
            RightWall.background(0);
            
   
            Roof.beginDraw();
            Roof.background(0,70,140);
            if (millis()-StartTime <=5*1000)
            {
              Equilizer(LeftWall);  
              Equilizer(RightWall); 
            }
            else if (millis()-StartTime < 10*1000)
            {
              FlyingBalls(LeftWall);  
              FlyingBalls(RightWall);  
              SmoothRGB();
              Roof.background(R,G,B);
            }
            else if (millis()-StartTime < 100*1000)
            {
              LightControl(LeftWall, RightWall);  
              SmoothRGB();
              Roof.background(R,G,B);
            }
            else
            { 
              Equilizer(LeftWall);  
              Equilizer(RightWall); 
              Roof.line(counter,0, counter, 24);
              counter++;
              counter%=150;
            }
            
            LeftWall.endDraw();
            RightWall.endDraw();
            Roof.endDraw();
            image(RightWall, 0, 0);
            image(Roof, 0, 32*scale);
            image(flipImage(LeftWall.get()), 0, (32+24)*scale);

            //FlyingBalls();
            //LightControl();
        }
      }
      catch(Exception e){}
    }  
    
        private PImage flipImage(PImage image)
    {
        BufferedImage img = (BufferedImage) image.getNative();
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -img.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return new PImage(op.filter(img, null));
    }
    

    public void Equilizer(PGraphics Image) {
        rectMode(CORNER);
        
        for(int i = 0; i < 70; i+=scale)
        {
          
          stroke(135,135,135);
          fill(135,135,135);
          float amp = 5*fft.getBand(i);
          if(amp*.8 > height)
            amp = (float)(height*.8);
          Image.rect( i+Image.width/2, 0, scale, amp);
          Image.rect( Image.width - (i+Image.width/2), 0, -1*scale, amp );
          ellipseMode(CENTER);
          Image.ellipse((float)(.2*Image.width),(float)(.5*Image.height), amp, amp);
          Image.ellipse((float)(.8*Image.width),(float)(.5*Image.height), amp, amp);
          
        }
    }

    public void FlyingBalls(PGraphics Image){
      for (int i = 0; i < fftFilter.length; i++) {
        fftFilter[i] = max(fftFilter[i] * decay, log(1 + fft.getBand(i)) * (float)(1 + i * 0.01));
      }
      
      for (int i = 0; i < fftFilter.length; i += 3) {   
        tint(colors.get((int)map(i, 0, fftFilter.length-1, 0, colors.width-1), colors.height/2));
        blendMode(ADD);
     
        float size = Image.height * (minSize + sizeScale * fftFilter[i]);
        PVector center = new PVector((float)(Image.width * (fftFilter[i] * 0.2)), 0);
        center.rotate(millis() * spin + i * radiansPerBucket);
        center.add(new PVector((float)(Image.width * 0.5), (float)(Image.height * 0.5)));
     
        Image.image(dot, center.x - size/2, center.y - size/2, size, size);
        fill(colors.get((int)map(i, 0, fftFilter.length-1, 0, colors.width-1), colors.height/2));
        ellipseMode(CORNER);
        //ellipse(center.x - size/2, center.y - size/2, size/15, size/15);
        //ellipse(center.x + size/2, center.y - size/2, size/15, size/15);
        //ellipse(center.x - size/2, center.y + size/2, size/15, size/15);
        //ellipse(center.x + size/2, center.y + size/2, size/15, size/15);
        //ellipse(center.x + size/2, center.y + size/2, size/15, size/15);
      }
    }
    public void SinglePixel(PImage Image)
    {

    }
    
    public void LightControl(PGraphics ImageL,PGraphics ImageR){
      fill(255, 0, 0);
      textSize(30*scale); 
      ImageL.text("INTERACTIVE", width, height);
      
   
        
        //we now have ratios, now draw
        
        ImageL.ellipseMode(RADIUS);
        ImageR.ellipseMode(RADIUS);
        ImageL.fill( random(255), random(255), random(255), random(255)); 
        ImageL.ellipse(width*Main.kinect.RightHandDepthRatio, ImageL.height*(1-Main.kinect.RightHandRaisedRatio), 3*fft.getBand(0), 3*fft.getBand(0));
        ImageR.fill( random(150), random(255), random(255), random(255)); 
        ImageR.ellipse(width*Main.kinect.RightHandDepthRatio, ImageR.height*(float)(1-Main.kinect.LeftHandRaisedRatio), 3*fft.getBand(0), 3*fft.getBand(0));      
      
    }
    void SmoothRGB(){
    R+=dR;
    G+=dG;    
    B+=dB;
  
  if ((R <= 5) || (R >= 250))  // if out of bounds
    dR = - dR; // swap direction  
  if ((G <= 5) || (G >= 250))  // if out of bounds
    dG = - dG; // swap direction  
  if ((B <= 5) || (B >= 250))  // if out of bounds
    dB = - dB; // swap direction  
    }
}