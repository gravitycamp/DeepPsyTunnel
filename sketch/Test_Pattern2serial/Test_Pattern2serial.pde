/*  OctoWS2811 movie2serial.pde - Transmit video data to 1 or more
      Teensy 3.0 boards running OctoWS2811 VideoDisplay.ino
    http://www.pjrc.com/teensy/td_libs_OctoWS2811.html
    Copyright (c) 2013 Paul Stoffregen, PJRC.COM, LLC

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/

// To configure this program, edit the following sections:
//
//  1: change myMovie to open a video file of your choice    ;-)
//
//  2: edit the serialConfigure() lines in setup() for your
//     serial device names (Mac, Linux) or COM ports (Windows)
//
//  3: if your LED strips have unusual color configuration,
//     edit colorWiring().  Nearly all strips have GRB wiring,
//     so normally you can leave this as-is.
//
//  4: if playing 50 or 60 Hz progressive video (or faster),
//     edit framerate in movieEvent().

import processing.video.*;
import processing.serial.*;
import java.awt.Rectangle;
import KinectPV2.KJoint;
import KinectPV2.*;

float framerate=30;

KinectPV2 kinect;
int [] depthZero;


float gamma = 1.7;
int FrameCounter = 0;
int PatternIndex = 0;
int PatternMode = 0;
int frameWidth = 150;
int frameHeight = 88;

int numPorts=0;  // the number of serial ports in use
int maxPorts=11; // maximum number of serial ports

Serial[] ledSerial = new Serial[maxPorts];     // each port's actual Serial port
Rectangle[] ledArea = new Rectangle[maxPorts]; // the area of the movie each port gets, in % (0-100)
boolean[] ledLayout = new boolean[maxPorts];   // layout of rows, true = even is left->right
PImage[] ledImage = new PImage[maxPorts];      // image sent to each port
int[] gammatable = new int[256];
int errorCount=0;


double PatternTime = 0;
double PatternStart = 0;


double LastFrameTime = 0;
double frameTime = 0;
int FrameCounterX = 0;
int FrameCounterY = 0;

class Panel {
  int X_Length;
  int Y_Length;  
  int P_index;
  PImage Panel_frame = new PImage();
  Panel(int X, int Y)  //constructor to set X and Y and allocate PImage for pixel array
  {
    X_Length = X;
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


void setup() {
  String[] list = Serial.list();
  delay(4000);
  depthZero    = new int[ KinectPV2.WIDTHDepth * KinectPV2.HEIGHTDepth];
  kinect = new KinectPV2(this);
  kinect.enableDepthImg(true);
  kinect.enableSkeleton3DMap(true);
  kinect.init();

  println("Serial Ports List:");
  println(list);
  serialConfigure("COM1");   // Right-side #1 (Master)
  serialConfigure("COM2");   // Right-side #2
  serialConfigure("COM3");   // Right-side #3
  serialConfigure("COM4");   // Right-side #4
  serialConfigure("COM5");   // Right-roof #5 (roof-right)
  serialConfigure("COM6");   // Right-side #6 (roof-center)
  serialConfigure("COM7");   // Left-side  #1
  serialConfigure("COM8");   // Left-side  #2
  serialConfigure("COM9");   // Left-side  #3
  serialConfigure("COM10");  // Left-side  #4
  serialConfigure("COM11");  // Left-side  #5 (roof-left)

  if (errorCount > 0) exit();
  for (int i=0; i < 256; i++) {
    gammatable[i] = (int)(pow((float)i / 255.0, gamma) * 255.0 + 0.5);
  }
  size(480, 400);  // create the window
}

 
// movieEvent runs for each new frame of movie data
void draw() {
  while(true)
  {
    // read the movie's next frame
    //m.read();
    int R=0,G=0,B=0;
    double RightHandRaisedRatio = 0;
    double depth_RightHand_Ratio = 0;
    PImage frame = createImage(tunnel_length, wall_height*2 + roof_length*3 , RGB);
    frame.loadPixels();
    
    ArrayList<KSkeleton> skeletonArray =  kinect.getSkeleton3d();
    int [] DepthRaw = kinect.getRawDepthData();
  
    //individual JOINTS
    for (int i = 0; i < skeletonArray.size(); i++) {
      KSkeleton skeleton = (KSkeleton) skeletonArray.get(i);
        if (skeleton.isTracked()) {
        KJoint[] joints = skeleton.getJoints();
  
        PVector RightWristP = joints[KinectPV2.JointType_WristRight].getPosition();
        PVector RightKneeP = joints[KinectPV2.JointType_KneeRight].getPosition();
        PVector HeadP = joints[KinectPV2.JointType_Head].getPosition();   
        double depth = joints[KinectPV2.JointType_WristRight].getZ();
        depth_RightHand_Ratio = (depth-2)/5; //4 is as deep as you can go!
        RightHandRaisedRatio =  (RightWristP.y-RightKneeP.y*.85)/(HeadP.y - RightKneeP.y);
        //println(RightWristP);
       
       // println(depth);
      }
    }
   
    if(false)
    {
    //  PImage frame = createImage(frameWidth, frameHeight, RGB);
    //  PatternIndex = 6;
      for(int y = 0; y < frameHeight; y++){
        for(int x = 0; x < frameWidth; x++)   
        {
          {
            switch(PatternIndex)
            {
            case(0):
              R = 127;
              G = x;
              B = int(y/2);
              break;
           case(1):
              R = x;
              G = 2*y;
              B = 0;
            break;
            case(2):
              R = 127;
              G = x;
              B = 0;
            break;
            case(3):
              R = 127;
              G = FrameCounterX;
              B = FrameCounterY;
            break;
            case(4):
              R = FrameCounterX;
              G = x;
              B = FrameCounterY*2;
            break;
            case(5): //animation  x= 0:150, y = 0:88
              R = 127;
    
              if(x == FrameCounterX) //horizontal index that counts between 0 and 150 every frame
                G = 127;
              else
                G = 0;
              B = 0;
              break;
            case(6): //animation  x= 0:150, y = 0:88
              R = 0;
    
              if(y == FrameCounterY) //verticle index that counts between 0 and 88 every frame
                G = 255;
              else
                G = 0;
              B = 0;
              break;
              case(7):
              R = 0;
              G = (int)(y*(255/88));
              B = 0;
              break;
          }
        frame.pixels[x+frameWidth*y] = color(R,G,B);  // input RGB value for each pixel
        }
      }
    }
    if(FrameCounterX == frameWidth)  //change pattern every 30 seconds
    {
      FrameCounterX = 0;
      FrameCounterY = 0;
      PatternIndex++; //animation pattern
      println("pattern" + PatternIndex);
    }
  
    FrameCounterX++; // used to change the pattern over time (animation)
    FrameCounterY++; // used to change the pattern over time (animation)
    FrameCounterY = FrameCounterY % frameHeight;
  
    PatternIndex = PatternIndex % 7; //5 patterns only
  
    frame.updatePixels();
    
 //   PImage frame2 = loadImage("C:/Kinect/Arduino/libraries/OctoWS2811/examples/VideoDisplay/Processing/movie2serial/Bleed1.png");
  /* frame2.updatePixels();
    PImage img = createImage(230, 230, ARGB);
  for(int i = 0; i < img.pixels.length; i++) {
    float a = map(i, 0, img.pixels.length, 255, 0);
    img.pixels[i] = color(0, 153, 204, a); 
  }
  
  */
  //background(0);
  // image(img, 90, 80);
    surface.setSize(frameWidth, frameHeight);
    image(frame, 0,0); 
  }
  else
  {
    //object oriented animation similar to (6)
    surface.setSize( T.Right_Wall.X_Length, T.Right_Wall.Y_Length+T.Left_Wall.Y_Length+T.Right_Roof.Y_Length+T.Left_Roof.Y_Length+T.Top.Y_Length);
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
            T.Left_Wall.Panel_frame.pixels[T.Right_Wall.X_Length*T.Right_Wall.Y_Length -1- (x+T.Right_Wall.X_Length*y)] = color(R,G,B);  //inverted index for left wall
          }
        }
        break;
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
            T.Left_Roof.Panel_frame.pixels[x+T.Right_Roof.X_Length*y] = color(R,G,B);  
            T.Top.Panel_frame.pixels[x+T.Top.X_Length*y] = color(R,G,B);  
          }
        }  
      }
    }
    T.Right_Wall.Panel_frame.updatePixels();
    T.Right_Roof.Panel_frame.updatePixels();
    T.Top.Panel_frame.updatePixels();
    T.Left_Roof.Panel_frame.updatePixels();
    T.Left_Wall.Panel_frame.updatePixels();
    
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
    frame.copy(T.Right_Wall.Panel_frame,0,0,
               T.Right_Wall.X_Length,T.Right_Wall.Y_Length,
               0,destination_offset_Y,
               T.Right_Wall.X_Length,T.Right_Wall.Y_Length);         
    
    surface.setSize(frame.width, frame.height);                 
    image(frame, 0, 0);
    /*
    image(T.Right_Wall.Panel_frame, 0, 0);
    image(T.Right_Roof.Panel_frame, 0, T.Right_Wall.Y_Length);
    image(T.Top.Panel_frame, 0, T.Right_Wall.Y_Length + T.  Right_Roof.Y_Length);
    image(T.Left_Roof.Panel_frame, 0, T.Right_Wall.Y_Length + T.Right_Roof.Y_Length+T.Top.Y_Length);
    image(T.Left_Wall.Panel_frame, 0, T.Right_Wall.Y_Length + T.Right_Roof.Y_Length+ T.Left_Roof.Y_Length+ T.Top.Y_Length);
    */
  }
  
    frameTime = millis();  
    while(frameTime-LastFrameTime<1000/framerate)
    {
      delay(1);
      frameTime = millis();
    }
//    println(1000/(frameTime-LastFrameTime));
    LastFrameTime = millis();
    
    SendSerial SendThreads[] = new SendSerial[numPorts];
    for (int j = 0; j < numPorts; j++) {
      SendThreads[j] = new SendSerial();
    }
    
    for (int i=0; i < numPorts; i++) {    
      // copy a portion of the movie's image to the LED image
      int xoffset = percentage(frame.width, ledArea[i].x);
      int yoffset = percentage(frame.height, ledArea[i].y);
      int xwidth =  percentage(frame.width, ledArea[i].width);
      int yheight = percentage(frame.height, ledArea[i].height);
      ledImage[i].copy(frame, xoffset, i*ledImage[i].height, xwidth, ledImage[i].height, 0, 0, ledImage[i].width, ledImage[i].height);
      /*
      println(xoffset);
      println(yoffset);
      println(xwidth);
      println(yheight);
      println(ledImage[i].width);
      println(ledImage[i].height);
        */              
      // convert the LED image to raw data
      byte[] ledData =  new byte[(ledImage[i].width * ledImage[i].height * 3) + 3];
      image2data(ledImage[i], ledData, ledLayout[i]);
      ledData[0] = '*';  // others sync to the master board
      int usec = 0;// 5900*(numPorts-i);
      ledData[1] = (byte)(usec);   // request the frame sync pulse
      ledData[2] = (byte)(usec >> 8); // at 75% of the frame time
      
      // send the raw data to the LEDs  :-)
      SendThreads[i].SetData(i, ledData);
      SendThreads[i].start();
    }
    //wait for all threads to finish
    delay(13);   
    double startThreads = millis();  
    for (int i=0; i < numPorts; i++) {   
      try{
        SendThreads[i].join();
  //    println(millis() - startThreads);
      } catch (InterruptedException e) {}
    }
  }
}

public class SendSerial extends Thread{
  int index;
  byte ledDataSendData[];
  
  public void SetData(int i, byte ledData[]) {
  index = i;
  ledDataSendData = ledData;
}
  
  public void run(){
    ledSerial[index].write(ledDataSendData);
  }
}
  

// image2data converts an image to OctoWS2811's raw data format.
// The number of vertical pixels in the image must be a multiple
// of 8.  The data array must be the proper size for the image.
void image2data(PImage image, byte[] data, boolean layout) {
  int offset = 3;
  int x, y, xbegin, xend, xinc, mask;
  int linesPerPin = image.height / 8;
  int pixel[] = new int[8];
  
  for (y = 0; y < linesPerPin; y++) {
    if ((y & 1) == (layout ? 0 : 1)) {
      // even numbered rows are left to right
      xbegin = 0;
      xend = image.width;
      xinc = 1;
    } else {
      // odd numbered rows are right to left
      xbegin = image.width - 1;
      xend = -1;
      xinc = -1;
    }
    for (x = xbegin; x != xend; x += xinc) {
      for (int i=0; i < 8; i++) {
        // fetch 8 pixels from the image, 1 for each pin
        int j = i;
        if (j == 0)
          {j =2;}
        else if (j == 2)
          {j =0;}
        else if (j == 4)
          {j =6;}
        else if (j == 6)
          {j =4;}
        int Loc = x + (y + linesPerPin * j) * image.width;
        pixel[i] = image.pixels[Loc];
        pixel[i] = colorWiring(pixel[i]);
      }
      // convert 8 pixels to 24 bytes
      for (mask = 0x800000; mask != 0; mask >>= 1) {
        byte b = 0;
        for (int i=0; i < 8; i++) {
          if ((pixel[i] & mask) != 0) b |= (1 << i);
        }
        data[offset++] = b;
      }
    }
  } 
}

// translate the 24 bit color from RGB to the actual
// order used by the LED wiring.  GRB is the most common.
int colorWiring(int c) {
  int red = (c & 0xFF0000) >> 16;
  int green = (c & 0x00FF00) >> 8;
  int blue = (c & 0x0000FF);
  red = gammatable[red];
  green = gammatable[green];
  blue = gammatable[blue];
  return (green << 16) | (red << 8) | (blue); // GRB - most common wiring
}

// ask a Teensy board for its LED configuration, and set up the info for it.
void serialConfigure(String portName) {
  if (numPorts >= maxPorts) {
    println("too many serial ports, please increase maxPorts");
  //  errorCount++;
    return;
  }
  try {
    ledSerial[numPorts] = new Serial(this, portName);
    if (ledSerial[numPorts] == null) throw new NullPointerException();
    } catch (Throwable e) {
    delay(2000);
  try {
    ledSerial[numPorts] = new Serial(this, portName);
    if (ledSerial[numPorts] == null) throw new NullPointerException();
   // ledSerial[numPorts].write('?');
  } catch (Throwable f) {
    println("Port " + portName + "non-functional");
 //   errorCount++;
    return;
  }
    }
  delay(50);
  String line = ledSerial[numPorts].readStringUntil(10);
  if (line == null) {
    //println("Port " + portName + " did not provide info, so set to defaul 150x8");
    line = "150,8,0,0,0,0,0,100,100,0,0,0";
    //errorCount++;
    //return;
  }
  String param[] = line.split(",");
  if (param.length != 12) {
    println("Error: port " + portName + " did not respond to LED config query");
   // errorCount++;
    return;
  }
  // only store the info and increase numPorts if Teensy responds properly
  ledImage[numPorts] = new PImage(Integer.parseInt(param[0]), Integer.parseInt(param[1]), RGB);
  ledArea[numPorts] = new Rectangle(Integer.parseInt(param[5]), Integer.parseInt(param[6]), Integer.parseInt(param[7]), Integer.parseInt(param[8]));
  ledLayout[numPorts] = (Integer.parseInt(param[5]) == 0);
  numPorts++;
}
/*
// draw runs every time the screen is redrawn - show the movie...
void draw() {
  // show the original video
  image(myMovie, 0, 80);
  
  // then try to show what was most recently sent to the LEDs
  // by displaying all the images for each port.
  for (int i=0; i < numPorts; i++) {
    // compute the intended size of the entire LED array
    int xsize = percentageInverse(ledImage[i].width, ledArea[i].width);
    int ysize = percentageInverse(ledImage[i].height, ledArea[i].height);
    // computer this image's position within it
    int xloc =  percentage(xsize, ledArea[i].x);
    int yloc =  percentage(ysize, ledArea[i].y);
    // show what should appear on the LEDs
    image(ledImage[i], 240 - xsize / 2 + xloc, 10 + yloc);
  } 
}

// respond to mouse clicks as pause/play
boolean isPlaying = true;
void mousePressed() {
  if (isPlaying) {
    myMovie.pause();
    isPlaying = false;
  } else {
    myMovie.play();
    isPlaying = true;
  }
}
*/
// scale a number by a percentage, from 0 to 100
int percentage(int num, int percent) {
  double mult = percentageFloat(percent);
  double output = num * mult;
  return (int)output;
}

// scale a number by the inverse of a percentage, from 0 to 100
int percentageInverse(int num, int percent) {
  double div = percentageFloat(percent);
  double output = num / div;
  return (int)output;
}

// convert an integer from 0 to 100 to a float percentage
// from 0.0 to 1.0.  Special cases for 1/3, 1/6, 1/7, etc
// are handled automatically to fix integer rounding.
double percentageFloat(int percent) {
  if (percent == 33) return 1.0 / 3.0;
  if (percent == 17) return 1.0 / 6.0;
  if (percent == 14) return 1.0 / 7.0;
  if (percent == 13) return 1.0 / 8.0;
  if (percent == 11) return 1.0 / 9.0;
  if (percent ==  9) return 1.0 / 11.0;
  if (percent ==  8) return 1.0 / 12.0;
  return (double)percent / 100.0;
}