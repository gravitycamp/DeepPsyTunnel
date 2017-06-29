import java.util.*;
import java.util.ArrayList;
import processing.core.PVector;
import processing.core.PApplet.*;
import KinectPV2.KJoint;
import KinectPV2.*;

// Kinect Controller Class
public class Kinect
{
    KinectPV2 kinect;
    public int [] RawDepth;
    public ArrayList<KSkeleton> skeletonArray;
    public KJoint[] joints;
    // Individual JOINTS
    public PVector RightWrist = new PVector(0,0,0);
    public PVector LeftWrist = new PVector(0,0,0);
    public PVector LeftKnee = new PVector(0,0,0);
    public PVector RightKnee = new PVector(0,0,0);
    public PVector Head = new PVector(0,0,0);
    public float RightWristDepth = 0;
    public float LeftWristDepth = 0;
    public float HeadDepth = 0;
    public float RightHandRaisedRatio = 0;
    public float LeftHandRaisedRatio = 0;
    public float RightHandDepthRatio = 0;
    public float LeftHandDepthRatio =0;
    public float RightHandSideRatio = 0;
    public float LeftHandSideRatio = 0;
    public float HandDistance = 0;
    public boolean RightHandOpen = false;
    public boolean LeftHandOpen = false;

    public Kinect(KinectPV2 k) {
      kinect = k;
      kinect.enableDepthImg(true);
      kinect.enableSkeleton3DMap(true);
      kinect.init();
    }
    
    public KinectPV2 pv2() {
      return kinect;
    }

    public void update() {
        skeletonArray = kinect.getSkeleton3d();
        RawDepth = kinect.getRawDepthData();
        for (int i = 0; i < skeletonArray.size(); i++) {
          KSkeleton skeleton = (KSkeleton) skeletonArray.get(i);
          if (skeleton.isTracked()) {
            joints = skeleton.getJoints();
            RightWrist = joints[KinectPV2.JointType_WristRight].getPosition();
            LeftWrist = joints[KinectPV2.JointType_WristLeft].getPosition();
            RightKnee = joints[KinectPV2.JointType_KneeRight].getPosition();
            LeftKnee = joints[KinectPV2.JointType_KneeLeft].getPosition();
            Head = joints[KinectPV2.JointType_Head].getPosition();   
            RightWristDepth = joints[KinectPV2.JointType_WristRight].getZ();
            LeftWristDepth = joints[KinectPV2.JointType_WristLeft].getZ();
            HeadDepth = joints[KinectPV2.JointType_Head].getZ();
            if(KinectPV2.HandState_Open == joints[KinectPV2.JointType_HandLeft].getState())
              LeftHandOpen = true;
            else
              LeftHandOpen =false; 
            if(KinectPV2.HandState_Open == joints[KinectPV2.JointType_HandRight].getState())
              RightHandOpen = true;
            else
              RightHandOpen =false; 
            //Ratio calculation and calibration
            RightHandDepthRatio = RightWristDepth/5; //4 is as deep as you can go!
            LeftHandDepthRatio = LeftWristDepth/5; //4 is as deep as you can go!
            RightHandRaisedRatio = 1 - (float)(RightWrist.y-RightKnee.y*.85)/(Head.y - RightKnee.y);
            LeftHandRaisedRatio = 1 - (float)(LeftWrist.y-LeftKnee.y*.85)/(Head.y - LeftKnee.y);
            RightHandSideRatio = (float)((RightWrist.x+1)/2);
            LeftHandSideRatio = (float)((RightWrist.x)/2);
            HandDistance = (float)(Math.hypot(RightWrist.x-LeftWrist.x, RightWrist.y-LeftWrist.y)/RightHandDepthRatio/3.7);
            
          }
        }
    }
}