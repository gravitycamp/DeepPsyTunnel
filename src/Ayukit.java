/* OpenProcessing Tweak of *@*http://www.openprocessing.org/sketch/70780*@* */ //<>// //<>// //<>//
/* !do not delete the line above, required for linking your tweak if you upload again */
import java.util.*;
import java.lang.reflect.*;
import java.awt.geom.*;
import java.awt.image.*;
import processing.video.*;
import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.*;
import KinectPV2.KJoint;
import KinectPV2.*;
import java.util.*;

public class Ayukit extends PApplet {

    int width;
    int height;
    String position = "Tunnel";
    Tunnel tunnel;
    Minim minim;
    
    KinectPV2 kinect;
    int [] depthZero;

    //Ayukit data
    PImage Ayukit; 
    PImage AyukitKen; 
    PImage Logo;
    PImage Ken;
    PImage KenThrow;
    double AY_depth = 0;
    double AY_Y = 0;
    boolean AY_OnLeftWall = true;
    boolean AyukitFound = false;
    boolean KenAllive = true;
    boolean KenThrowingAyukit = false;
    boolean DrawKenMove = true;
    int AyukitFrame = 0;
    int KenAyukitFrame = 0;
    AudioPlayer  Ayukit_Sound;
    AudioPlayer  VS_Sound;
    AudioPlayer  Stage_Sound;
    AudioPlayer  KenVoice;
    int R = (int)random(255);
    int G = (int)random(255);
    int B = (int)random(255);
    int scale = 1;  
    int dR = 1;
    int dG = 1;
    int dB = 1;
    
    float kenMove = 0;
    float kenDelta = (float).4;
    int AyukitKenHeight=0;
    
    PGraphics LeftWall;
    PGraphics RightWall;
    PGraphics Roof; 
    float timeElapsed;
    float KenKillTime;
    float TimeStartAyukitKen;

    public Ayukit(Tunnel t, int w, int h, String p) {
        width = w;
        height = h;
        tunnel = t;
        position = p;
        minim = tunnel.minim;
    }

    public void settings() {
        size(width, height);
    }

    public void setup() {
        Ayukit = loadImage("C:/TunnelGit2/src/data/Aukit_BK.jpg");
        Ayukit.resize(5,5);
        AyukitKen = loadImage("C:/TunnelGit2/src/data/Aukit_BK.jpg");
        AyukitKen.resize(5,5);
        Logo = loadImage("C:/TunnelGit2/src/data/logo.jpg");
        Ken = loadImage("C:/TunnelGit2/src/data/Ken.jpg");
        KenThrow = loadImage("C:/TunnelGit2/src/data/KenThrow1.jpg");

        Ken.resize((int)((.6*32*scale)/Ken.height*Ken.width),(int)(.6*32*scale));
        Ken=flipImage(Ken);
        KenThrow.resize((int)((.6*32*scale)/KenThrow.height*KenThrow.width),(int)(.6*32*scale));
        KenThrow=flipImage(KenThrow);
        
        Ayukit_Sound = minim.loadFile("C:/TunnelGit2/src/data/Ayukit.mp3");
        KenVoice = minim.loadFile("C:/TunnelGit2/src/data/kenVoice.mp3"); 
        VS_Sound = minim.loadFile("C:/TunnelGit2/src/data/StreetFightervs.mp3");
        Stage_Sound = minim.loadFile("C:/TunnelGit2/src/data/RyuStage.mp3");
        VS_Sound.play();

        background(0);
        image(flipImage(Logo.get()), 0, 0, 150*scale, 32*scale);
        image(Logo, 0, (32+24)*scale, 150*scale, 32*scale);
        timeElapsed = millis();

        LeftWall= createGraphics(150*scale, 32*scale);
        RightWall= createGraphics(150*scale, 32*scale);
        Roof= createGraphics(150*scale, 24*scale);
     //   StartTime = millis(); //<>//
    }


    public void draw() {
      try{
        synchronized(Tunnel.class) {
          Main.kinect.update();
          if((millis()-timeElapsed)>3500)
          {
            Stage_Sound.play();            
            LeftWall.beginDraw();
            LeftWall.background(0);
            RightWall.beginDraw();
            RightWall.background(0);
            Roof.beginDraw();
                        
            SmoothRGB();
            Roof.background(R,G,B);            
            
            if(!AyukitFound && KenAllive &&(Main.kinect.LeftWristDepth/Main.kinect.HeadDepth < .9) && (Main.kinect.RightWristDepth/Main.kinect.HeadDepth < .9))    //create an ayukit at right hand y = center of ayukit
            {
              //println(HeadP.x);
              AyukitFound = true;
              AY_depth = Main.kinect.RightHandDepthRatio;
              AY_Y = 1-Main.kinect.RightHandRaisedRatio;
              if(Main.kinect.Head.x < 100) //head is on left or right side (need to update center of screen = width/2 
                AY_OnLeftWall = true;
              Ayukit_Sound.rewind();
              Ayukit_Sound.play(); 
              AyukitFrame = 0;
            }
            
            //test data
            if(!AyukitFound && (millis()-timeElapsed)>4500 && (millis()-timeElapsed)<5000)
            {
              AY_depth = 0.5;
              AY_Y = .5;
              AY_OnLeftWall = true;   
              AyukitFound = true;
              Ayukit_Sound.rewind();
              Ayukit_Sound.play(); 
              AyukitFrame = 0;
            }
           
            if(!AyukitFound && (millis()-timeElapsed)>14500 && (millis()-timeElapsed)<15000)
            {
              AY_depth = 0.3;
              AY_Y = 0.5;
              AY_OnLeftWall = true;   
              AyukitFound = true;
              Ayukit_Sound.rewind();
              Ayukit_Sound.play();
              AyukitFrame = 0;
            }
            //test data       
           
            //let Ken Randomly throw Ayukit
            if(!KenThrowingAyukit && KenAllive && (1==(int)random(80)))
            {
              KenThrowingAyukit = true;
              AyukitKenHeight = (int)(Ken.height+(int)kenMove-AyukitKen.height*1.9);  //set once at height calibrated at his arms
              TimeStartAyukitKen = millis();
              Ayukit_Sound.rewind();
              Ayukit_Sound.play();
              KenAyukitFrame= 0;
            }
            if(KenThrowingAyukit)
            {
              if(millis()- TimeStartAyukitKen <1000) //display Ken throwing Ayukit
              {
                DrawKenMove = false;
                RightWall.copy(KenThrow, 0,0, KenThrow.width, KenThrow.height+(int)kenMove, 0, (int)kenMove, KenThrow.width, KenThrow.height+(int)kenMove);
              }
              else
                DrawKenMove = true;
              //draw ayukit moving
              RightWall.copy(AyukitKen, 0,0, AyukitKen.width, AyukitKen.height,Ken.width+KenAyukitFrame, AyukitKenHeight, AyukitKen.width, AyukitKen.height);              
              KenAyukitFrame+=2;
              if(KenAyukitFrame >RightWall.width)
                KenThrowingAyukit = false;
            }

            
            if(DrawKenMove && KenAllive) //draw Ken moving
            {
              kenMove +=kenDelta;
              if((kenMove> 0.4*32*scale) || (kenMove<0))
                kenDelta = - kenDelta;
              RightWall.copy(Ken, 0,0, Ken.width, Ken.height+(int)kenMove, 0, (int)kenMove, Ken.width, Ken.height+(int)kenMove);      
            }
            else if(!KenAllive)  //stop moving him and blink him as dead
            {
              //blink
              if(1==((int)(millis()-KenKillTime)/400) %2)
                RightWall.copy(Ken, 0,0, Ken.width, Ken.height+(int)kenMove, 0, (int)kenMove, Ken.width, Ken.height+(int)kenMove);
              //resume after 4 seconds  
              if(millis()-KenKillTime > 4*1000)
                KenAllive = true;
            }
            
            //draw player Ayukit
            if(AyukitFound)
            {
              RightWall.copy(Ayukit, 0,0, Ayukit.width, Ayukit.height, (int)(RightWall.width*AY_depth-AyukitFrame), (int)(RightWall.height*AY_Y), Ayukit.width, Ayukit.height);
              //println("X  = "+ (RightWall.width*AY_depth-AyukitFrame));
              AyukitFrame+=2;
              if(RightWall.width*AY_depth-AyukitFrame <0)
                AyukitFound = false;
            }          
            
            //check to see if player Ayukit hit Ken
            if(AyukitFound && KenAllive && (RightWall.width*AY_depth-AyukitFrame) < Ken.width*.8 && (Ken.height+(int)kenMove > (RightWall.height*AY_Y)) && ((int)kenMove<RightWall.height*AY_Y-Ayukit.height))
            {
              KenAllive = false;
              KenVoice.play();
              KenVoice.cue(12500);    
              KenKillTime = millis();
            }
          //   RightWall.copy(AyukitKen, 0,0, AyukitKen.width, AyukitKen.height,Ken.width+KenAyukitFrame, AyukitKenHeight, AyukitKen.width, AyukitKen.height);         
            //check to see if Aukits hit each other
            if(AyukitFound && KenThrowingAyukit)  //if both players throwing Ayukit
            {
              //println("player x: " +(AyukitKen.height+AyukitKenHeight) + " > " + (RightWall.height*AY_Y));
              //println("player x: " +AyukitKenHeight + " < " + (RightWall.height*AY_Y+Ayukit.height));
              if(((RightWall.width*AY_depth-AyukitFrame) < Ken.width+KenAyukitFrame+AyukitKen.width/2) 
                && (Ayukit.height+AyukitKenHeight  >(RightWall.height*AY_Y)) 
                && (AyukitKenHeight < RightWall.height*AY_Y+Ayukit.height))  
              {
                AyukitFound = false;
                KenThrowingAyukit = false;
              }
            }
            
            
            //finalize draw
            LeftWall.endDraw();
            RightWall.endDraw();
            Roof.endDraw();
            image(RightWall, 0, 0);
            image(Roof, 0, 32*scale);
            image(flipImage(LeftWall.get()), 0, (32+24)*scale);
          }
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