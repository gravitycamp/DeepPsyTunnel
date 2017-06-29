import processing.opengl.*;
import KinectPV2.KJoint;
import KinectPV2.*;
import processing.sound.*;

KinectPV2 kinect;
int [] depthZero;
double RightHandRaisedRatio = 1;
double depth_RightHand_Ratio = 0;
 
int rainNum = 150;
ArrayList rain = new ArrayList();
ArrayList splash = new ArrayList();
float current;
float reseed = random(0,.2);
 
void setup()
{
  size(750,150,P3D);
  colorMode(HSB,100);
  background(0);
  SoundFile RainThunder = new SoundFile(this, "rain_thunder.MP3");
  RainThunder.play();
 
  rain.add(new Rain());
  current = millis();
  depthZero = new int[ KinectPV2.WIDTHDepth * KinectPV2.HEIGHTDepth];
  
  kinect = new KinectPV2(this);
  kinect.enableDepthImg(true);
  kinect.enableSkeleton3DMap(true);
  kinect.init();
}
 
void draw()
{
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
  blur(50);
   
  if ((millis()-current)/1000>reseed&&rain.size()<150)
  {
    rain.add(new Rain());
    float reseed = random(0,.2);
    current = millis();
  }
   
  for (int i=0 ; i<rain.size() ; i++)
  {
    Rain rainT = (Rain) rain.get(i);
    rainT.calculate();
    rainT.draw();
    
    if ((rainT.position.x>width*depth_RightHand_Ratio*.9) && (rainT.position.x<width*depth_RightHand_Ratio*1.1))
    {
      if (rainT.position.y>height*RightHandRaisedRatio)
      {
         
        for (int k = 0 ; k<random(5,10) ; k++)
        {
          splash.add(new Splash(rainT.position.x,(float)(height*RightHandRaisedRatio)));
        }
         
        rain.remove(i);
        float rand = random(0,100);
        if (rand>10&&rain.size()<150)
        rain.add(new Rain());
      }
    }
    if (rainT.position.y>height)
      {
         
        for (int k = 0 ; k<random(5,10) ; k++)
        {
          splash.add(new Splash(rainT.position.x,(float)(height)));
        }
         
        rain.remove(i);
        float rand = random(0,100);
        if (rand>10&&rain.size()<150)
        rain.add(new Rain());
      }
  }
   
  for (int i=0 ; i<splash.size() ; i++)
  {
    Splash spl = (Splash) splash.get(i);
    spl.calculate();
    spl.draw();
    //println("position " + spl.position.y);
    //println("depth " + height*depth_RightHand_Ratio);
    if ((spl.position.x>height*depth_RightHand_Ratio*.9) && (spl.position.x<height*depth_RightHand_Ratio*1.1))
    {
      if (spl.position.y>height*RightHandRaisedRatio)
        splash.remove(i);
    }
    else if (spl.position.y>height)
        splash.remove(i);
  }
}
 
void blur(float trans)
{
  noStroke();
  fill(0,trans);
  rect(0,0,width,height);
}