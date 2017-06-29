import KinectPV2.KJoint;
import KinectPV2.*;
import java.awt.Rectangle;
import g4p_controls.*;
import processing.sound.*;

KinectPV2 kinect;
int [] depthZero;

PImage img;
public int frameWidth = 150;
public int frameHeight = 88;
int FrameDiag = round(sqrt(pow(frameHeight,2) + pow(frameWidth,2)));
double LastFrameTime = 0;
double frameTime = 0;
int FrameCounterX = 0;
int FrameCounterY = 0;
int FrameCounterDiag = 0;
int PatternIndex = 0;

int framerate = 30;
double PatternTime = 0;
double PatternStart = 0;

int FrameCounterWall_Height = 0;
int FrameCounterRoof_Height = 0;

boolean UsePlanePatterns = false;
boolean alternating = true;

//Ayukit data
PImage Ayukit; 
double AY_depth = 0;
double AY_Y = 0;
boolean AY_OnLeftWall = true;
boolean AyukitFound = false; 
SoundFile Ayukit_Sound;
SoundFile Drum_Bass;


class Panel {
  int X_Length;
  int Y_Length;  
  PImage Panel_frame = new PImage();
  Panel(int X, int Y)  //constructor to set X and Y and allocate PImage for pixel array
  {
    X_Length = X; //<>//
    Y_Length = Y;
    Panel_frame = createImage(X_Length, Y_Length, RGB);
    Panel_frame.loadPixels();
  }
}

class Tunnel {
  //constructor that maps dimensions onto each tunnel panel
    Panel Left_Wall;
    Panel Right_Wall;
    Panel Left_Roof;
    Panel Right_Roof;
    Panel Top;
  Tunnel(int wall_height,  int side_roof_Length, int top_width, int Tunnel_length) 
  {
    //index is set in the order of the Teensy's layout
    Left_Wall   = new Panel(Tunnel_length, wall_height);
    Right_Wall  = new Panel(Tunnel_length, wall_height);
    Left_Roof   = new Panel(Tunnel_length, side_roof_Length);
    Right_Roof  = new Panel(Tunnel_length, side_roof_Length);
    Top         = new Panel(Tunnel_length, top_width);   
  }
}



//define dimensions of the tunnel and use constructor and create global Tunnel Object
int wall_height = 8*4;
int roof_length = 8;
int tunnel_length = 150;
Tunnel T = new Tunnel(wall_height,roof_length, roof_length, tunnel_length);

//GUI
ArrayList<GTextIconAlignBase> controls = new ArrayList<GTextIconAlignBase>();
GLabel lblText;
GDropList Select;
GCheckbox cbx0;

void setup() {
  size(400, 400);  
  frameRate(framerate);
  //surface.setSize(frameWidth, frameHeight);
  depthZero    = new int[ KinectPV2.WIDTHDepth * KinectPV2.HEIGHTDepth];
  
  kinect = new KinectPV2(this);
  kinect.enableDepthImg(true);
  kinect.enableSkeleton3DMap(true);
  kinect.init();
    
  //GUI
  String[] items;  
  int x=0; 
  int y=88;
  lblText = new GLabel(this, x, y, 80, 38, "Select Pattern");
  items = new String[] { "Alternating", "Pattern 1", "Pattern 2", "Pattern 3", "Pattern 4","Pattern 5","Pattern 6","Pattern 7"};
  Select = new GDropList(this, x, y + 30, 80, 190, 10);
  Select.setItems(items, 0);
  Select.tag = "Pattern_Select";
  Select.setLocalColorScheme(3);  
  lblText.setLocalColorScheme(3);
  cbx0 = new GCheckbox(this, x+80, y + 30, 150, 25, "Use Plain Patterns");
  controls.add(cbx0);
  
  Ayukit = loadImage("Aukit_BK.jpg");
  Ayukit.resize(10,10);
  Ayukit_Sound = new SoundFile(this, "Ayukit.mp3");
  
  //slow down Drum_Bass
  Drum_Bass = new SoundFile(this, "Until The World Ends.mp3");
  
  

  
}

public void handleDropListEvents(GDropList list, GEvent event) {
  if (list == Select)
  {
    int result = list.getSelectedIndex();
    println(result+ " selected");
    if(result != 0) //if not alternate   
    {
      PatternIndex = result; 
      alternating = false;
      if((PatternIndex == 1) && (UsePlanePatterns== false))
        Drum_Bass.play();
    }
    else
    {
      alternating =true;
      PatternIndex = 1;
    }
  }
}


public void handleToggleControlEvents(GToggleControl checkbox, GEvent event) 
{
    UsePlanePatterns = checkbox.isSelected();
}

void draw() {
  background(255);
  int R=0,G=0,B=0;
  double RightHandRaisedRatio = 0;
  double depth_RightHand_Ratio = 0;
  PImage frame = createImage(tunnel_length, wall_height*2 + roof_length*3 , RGB);
  frame.loadPixels();
  
 
  
  ArrayList<KSkeleton> skeletonArray =  kinect.getSkeleton3d();
  int [] DepthRaw = kinect.getRawDepthData();
  //individual JOINTS
  PVector RightWristP;
  PVector LeftWristP;
  PVector RightKneeP;
  PVector HeadP = null;
  double RightWristdepth = 0;
  double LeftWristdepth = 0;
  double HeadDepth = 0;
  
  
  for (int i = 0; i < skeletonArray.size(); i++) {
    KSkeleton skeleton = (KSkeleton) skeletonArray.get(i);
    if (skeleton.isTracked()) {
      KJoint[] joints = skeleton.getJoints();
      RightWristP = joints[KinectPV2.JointType_WristRight].getPosition();
      LeftWristP = joints[KinectPV2.JointType_WristLeft].getPosition();
      RightKneeP = joints[KinectPV2.JointType_KneeRight].getPosition();
      HeadP = joints[KinectPV2.JointType_Head].getPosition();   
      RightWristdepth = joints[KinectPV2.JointType_WristRight].getZ();
      LeftWristdepth = joints[KinectPV2.JointType_WristLeft].getZ();
      HeadDepth = joints[KinectPV2.JointType_Head].getZ(); 
      //Ratio calculation and calibration
      depth_RightHand_Ratio = RightWristdepth/4; //4 is as deep as you can go!
      RightHandRaisedRatio =  (RightWristP.y-RightKneeP.y*.85)/(HeadP.y - RightKneeP.y);
    }
  }
//  PatternIndex = 2;
  //basic patterns that do not use Tunnel object
  if(UsePlanePatterns)
  {
    for(int y = 0; y < frameHeight; y++){
      for(int x = 0; x < frameWidth; x++)   
      {        
        switch(PatternIndex)
        {
          case(1):
          PatternTime = 1000;
          R = 127;
          G = x;
          B = 2*y;
          break;
          case(2):
          PatternTime = 1000;
          R = x;
          G = 2*y;
          B = 0;
          break;
          case(3):
          PatternTime = 1000;
          R = 127;
          G = x;
          B = 0;
          break;
          case(4):
          PatternTime = 1000;
          R = 127;
          G = FrameCounterX;
          B = FrameCounterY;
          break;
          case(5):
          PatternTime = 1000;
          R = FrameCounterX;
          G = x;
          B = FrameCounterY*2;
          break;
          case(6): //animation  x= 0:150, y = 0:88
          PatternTime = 1000*frameWidth/framerate;
          R = 150;
          if(x == FrameCounterX) //horizontal index that counts between 0 and 150 every frame
            G = 150;
          else
            G = 0;
          B = 0;
          break;
          case(7): //animation  x= 0:150, y = 0:88
          PatternTime = 1000*frameHeight/framerate;
          R = 255;
          if(y == FrameCounterY) //verticle index that counts between 0 and 88 every frame
            G = 255;
          else
            G = 0;
          B = 0;
          break;
          case(8): //diagnal
          PatternTime =  1000000/framerate * FrameDiag;
          R = 255;
          if((round(y*FrameDiag/frameHeight) == FrameCounterDiag) && (round(x*FrameDiag/frameWidth) == FrameCounterDiag)) 
            G = 255;
          else
            G = 0;
          B = 0;
          break;
          case(9): //diagnal line commet
          int LineLength = 25;
          PatternTime =  1000000/framerate * FrameDiag;
          int scaledY = round(y*FrameDiag/frameHeight);
          int scaledX = round(x*FrameDiag/frameWidth);
          R = 255;
          G = 0;
          for(int lineIndex = 0; lineIndex <LineLength;lineIndex++)
          {
            if((scaledY == (FrameCounterDiag-lineIndex)) && (scaledX == (FrameCounterDiag-lineIndex)))
              G = 255-10*lineIndex;
          }
          B = 0;
          break;
      }
      frame.pixels[x+frameWidth*y] = color(R,G,B);  // input RGB value for each pixel
      }
    }
  }
  else
  {
    switch(PatternIndex) //<>//
    {
      case(1): //Y and Z tracking
      //object oriented animation similar to (6)
      
      Drum_Bass.rate(1.8-(float)RightHandRaisedRatio);
      Drum_Bass.amp((float)depth_RightHand_Ratio);
      for(int p=0; p<5; p++){  //5 panels
        switch(p)
        {
          case 0: case 4: //walls will be symetrical
          for(int y = 0; y < T.Right_Wall.Y_Length; y++){ //use longest panel (should change this to while loop
            for(int x = 0; x < T.Right_Wall.X_Length; x++)   //use wall x (should change this to while loop
            {
              PatternTime = 1000*frameHeight/framerate;
              R = 255;
              //if(y == FrameCounterWall_Height) //verticle index that counts between 0 and 88 every frame
              if(y== (int)(T.Right_Wall.Y_Length*RightHandRaisedRatio))  //change that Y pixel which is related to the right hand raised ratio
                G = 255;
              else
                G = 0;
              B = 0;
              T.Right_Wall.Panel_frame.pixels[x+T.Right_Wall.X_Length*y] = color(R,G,B);  // input RGB value for each pixel
              T.Left_Wall.Panel_frame.pixels[x+T.Left_Wall.X_Length*(T.Left_Wall.Y_Length -y-1)] = color(R,G,B);  //inverted index for left wall
            }
          }
          case 1: case 2: case 3: //roofs
          for(int y = 0; y < T.Right_Roof.Y_Length; y++){ 
            for(int x = 0; x < T.Right_Roof.X_Length; x++)   
            {
              R = 255;
              //if(y == FrameCounterWall_Height) //verticle index that counts between 0 and 88 every frame
              if(x== (int)(T.Right_Roof.X_Length * depth_RightHand_Ratio)) //change that X pixel which is related to the right hand depth ratio (maps Z to X)
                G = 255;
              else
                G = 0;
              B = 0;
              T.Right_Roof.Panel_frame.pixels[x+T.Right_Roof.X_Length*y] = color(R,G,B);  
              T.Left_Roof.Panel_frame.pixels[x+T.Left_Roof.X_Length*y] = color(R,G,B);  
              T.Top.Panel_frame.pixels[x+T.Top.X_Length*y] = color(R,G,B);  
            }
          }  
        }
      }
      break;
      case(2): //ayukit
      {
  //      PatternTime = 1000*frameWidth/framerate;
        //look for ayukit
        /*
        if((LeftWristdepth > HeadDepth+.2) && (RightWristdepth > HeadDepth+.2))    //create an ayukit at right hand y = center of ayukit
        {
          AyukitFound = true;
          AY_depth = RightWristdepth;
          AY_Y = RightHandRaisedRatio;
          if(HeadP.x < 100) //head is on left or right side (need to update center of screen = width/2 
            AY_OnLeftWall = true;
          Ayukit_Sound.play(); 
        }
        */
        //test ayukit
        
        AyukitFound = true;
        AY_depth = 0.5;
        AY_Y = 0.5;
        AY_OnLeftWall = true;
       
        if(AyukitFound)
        {
          for(int p=0; p<5; p++){  //5 panels
            switch(p)
            {          
              case 0: case 4: //walls will be symetrical
              for(int y = 0; y < T.Right_Wall.Y_Length; y++){ //use longest panel (should change this to while loop
                for(int x = 0; x < T.Right_Wall.X_Length; x++)   //use wall x (should change this to while loop
                {
                  R = 255;
                  G = 0;
                  B = 0;
                  for(int I_x = 0; I_x <Ayukit.width; I_x++){
                    for(int I_y = 0; I_y <Ayukit.height; I_y++){
                       //See if any of these pixels are in the overlayed image
                       if((y == (int)(T.Right_Wall.Y_Length * AY_Y +I_y)) && (x== (int)(T.Right_Wall.X_Length * AY_depth + I_x + FrameCounterX)))
                       {
                         if(Ayukit.pixels[I_x+I_y * Ayukit.width] != #000000)
                         {
                           int local_R = (Ayukit.pixels[I_x+I_y * Ayukit.width]>>16) & 0xFF;
                           int local_G = (Ayukit.pixels[I_x+I_y * Ayukit.width]>>8) & 0xFF;
                           int local_B = Ayukit.pixels[I_x+I_y * Ayukit.width] & 0xFF;
                           //make sure its not close to black (smoothing)
                           if ((local_R +local_G +local_B) > 110)
                           {
                             R = local_R;
                             G = local_G;        
                             B = local_B;
                           }   
                         }
                         if(x == T.Right_Wall.X_Length-1)
                         {
                           //AyukitFound =false;
                           FrameCounterX = 0;
                         }
                       }
                    }
                  }
                  if(AY_OnLeftWall)
                  {
                    T.Left_Wall.Panel_frame.pixels[x+T.Left_Wall.X_Length*(T.Left_Wall.Y_Length -y-1)] = color(R,G,B);  //inverted index for left wall
                    T.Right_Wall.Panel_frame.pixels[x+T.Right_Wall.X_Length*y] = color(255,0,0);  // input RGB value for each pixel
                  }
                  else
                  {
                    T.Left_Wall.Panel_frame.pixels[x+T.Left_Wall.X_Length*(T.Left_Wall.Y_Length -y-1)] = color(255,0,0);  //inverted index for left wall
                    T.Right_Wall.Panel_frame.pixels[x+T.Right_Wall.X_Length*y] = color(R,G,B);  // input RGB value for each pixel
                  }
                }
              }
              break;
              case 1: case 2: case 3://walls will be symetrical
              for(int y = 0; y < T.Right_Roof.Y_Length; y++){ 
                for(int x = 0; x < T.Right_Roof.X_Length; x++)   
                {
                  R = 0;
                  G = 255;
                  B = 0;
                  T.Right_Roof.Panel_frame.pixels[x+T.Right_Roof.X_Length*y] = color(R,G,B);  
                  T.Left_Roof.Panel_frame.pixels[x+T.Left_Roof.X_Length*y] = color(R,G,B);  
                  T.Top.Panel_frame.pixels[x+T.Top.X_Length*y] = color(R,G,B);  
                }
              }
            }
          }
        }
      }
      break;
      case(3): //new pattern
      
      break;
    }
  }
  //integrate the 4 panels into 1 pimage to send to LEDs
  int destination_offset_Y = 0;
  frame.copy(T.Right_Wall.Panel_frame,0,0,
             T.Right_Wall.X_Length,T.Right_Wall.Y_Length,
             0,destination_offset_Y,
             T.Right_Wall.X_Length,T.Right_Wall.Y_Length);
  destination_offset_Y = T.Right_Wall.Y_Length;
  frame.copy(T.Right_Roof.Panel_frame,0,0,
             T.Right_Roof.X_Length,T.Right_Roof.Y_Length,
             0,destination_offset_Y,
             T.Right_Roof.X_Length,T.Right_Roof.Y_Length);               
  destination_offset_Y = T.Right_Wall.Y_Length + T.Right_Roof.Y_Length;
  frame.copy(T.Top.Panel_frame,0,0,
             T.Top.X_Length,T.Top.Y_Length,
             0,destination_offset_Y,
             T.Top.X_Length,T.Top.Y_Length);               
  destination_offset_Y = T.Right_Wall.Y_Length + T.Right_Roof.Y_Length + T.Top.Y_Length;
  frame.copy(T.Left_Roof.Panel_frame,0,0,
             T.Left_Roof.X_Length,T.Left_Roof.Y_Length,
             0,destination_offset_Y,
             T.Left_Roof.X_Length,T.Left_Roof.Y_Length);               
  destination_offset_Y = T.Right_Wall.Y_Length + T.Right_Roof.Y_Length + T.Top.Y_Length + T.Left_Roof.Y_Length;
  frame.copy(T.Left_Wall.Panel_frame,0,0,
             T.Left_Wall.X_Length,T.Left_Wall.Y_Length,
             0,destination_offset_Y,
             T.Left_Wall.X_Length,T.Left_Wall.Y_Length);     
  surface.setSize( T.Right_Wall.X_Length+100, T.Right_Wall.Y_Length+T.Left_Wall.Y_Length+T.Right_Roof.Y_Length+T.Left_Roof.Y_Length+T.Top.Y_Length+500);
  frame.updatePixels();                          
  image(frame, 0, 0);
    
  FrameCounterX++; // used to change the pattern over time (animation) 
  FrameCounterX %= frameWidth;
  FrameCounterY++; // used to change the pattern over time (animation)
  FrameCounterY %= frameHeight;
  FrameCounterDiag++;
  FrameCounterDiag %= FrameDiag;
  
  //object oriented counters
  FrameCounterWall_Height++;
  FrameCounterWall_Height %= T.Right_Wall.Y_Length;
  FrameCounterRoof_Height++;
  FrameCounterRoof_Height %= T.Right_Roof.Y_Length;

  if(millis() - PatternStart > PatternTime)  //go to next pattern and reset counters after pattern completes
  {
    if(alternating)
    {
      PatternIndex++;
      if(PatternIndex == 9)
        PatternIndex = 1; //9 patterns only
    }
  //  FrameCounterX = 0;
    FrameCounterY = 0;
    PatternStart = millis();
  }
 
  //control delay between next frame to maintain target framerate
  frameTime = millis();  
  while(frameTime-LastFrameTime<1000/framerate)
  {
    delay(1);
    frameTime = millis();
  }
  LastFrameTime = millis();
  
}