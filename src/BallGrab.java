import processing.core.*;

public class BallGrab extends PApplet {

    int width;
    int height;
    String position = "Tunnel";
    Tunnel tunnel;

      boolean ballGrabbed;
  
     Ball balls[] = new Ball[10];

    public BallGrab(Tunnel t, int w, int h, String p) {
        width  = w;
        height = h;
        tunnel = t;
        position = p;
    }

    public void settings() {
        size(width, height);
    }
  

  
  public void setup() {
    colorMode(HSB);
    ballGrabbed = false;
    background(255);
    // Generate balls with random attributes and starting position
    for(int i = 0; i < balls.length; i++){
      balls[i] = new Ball(random(width/4, 3*width/4), random(height/8, height/2), random(25,50), 1, color(random(255), 255, 255));
    }
  }
  
  public void draw() {  //draw function loops 
    try{
      synchronized(Tunnel.class) {
        Main.kinect.update();
        noStroke();
        // Motion blur effect
        fill(0,100);
        rect(0, 0, width, height);
        stroke(0);
        // Show each ball in reverse order
        for(int i = 0; i < balls.length; i++){
          balls[balls.length-1-i].show();
        }
        if((!Main.kinect.LeftHandOpen || !Main.kinect.RightHandOpen) && !ballGrabbed){  //if either hand is closed and ball isn't grabbed.
          for(int i = 0; i < balls.length; i++){
            if(balls[i].isHandOver()){
              balls[i].setGrabbed(true);
              ballGrabbed = true;
              break;
            }
          }
        }
        if(Main.kinect.LeftHandOpen && Main.kinect.RightHandOpen){
          ballGrabbed = false;
          for(int i = 0; i < balls.length; i++){
            balls[i].setGrabbed(false);
          }
        }
      }
    }
    catch(Exception e){}
  }
  
 
  //// When mouse is pressed, grab the first ball on top if multiple balls are under the cursor
  //public void mousePressed(){
  //  if(!ballGrabbed){
  //    for(int i = 0; i < balls.length; i++){
  //      if(balls[i].isMouseOver()){
  //        balls[i].setGrabbed(true);
  //        ballGrabbed = true;
  //        break;
  //      }
  //    }
  //  }
  //}
  
  //// When mouse if released, clear ballGrabbed boolean for all balls
  //public void mouseReleased(){
  //  ballGrabbed = false;
  //  for(int i = 0; i < balls.length; i++){
  //    balls[i].setGrabbed(false);
  //  }
  //}
  
  public class Ball{
    
    // Ball attributes
    private float x;
    private float y;
    private float size;
    private float mass;
    private int ballColor;
  
    // Physical constants
    private float gravity;
    private float springFactor;
    private float dampFactor;
    private float airDensity;
    private float dragCoeff;
  
    // Real-time physics properties
    private float x_speed;
    private float y_speed;
    private float x_force;
    private float y_force;
  
    // Different update logic when ball is currently grabbed
    private boolean grabbed;
  
    public Ball(float x, float y, float size, float mass, int ballColor){
      this.x = x;
      this.y = y;
      this.size = size;
      this.mass = mass;
      this.ballColor = ballColor;
      initPhysics();
    }
  
    private void initPhysics(){
      gravity = (float)0.5;
      springFactor = (float)1.5;
      dampFactor = (float)0.25;
      airDensity = (float)1.2922;
      dragCoeff = (float)0.005;
      x_speed = 0;
      y_speed = 0;
      x_force = 0;
      y_force = 0;
      grabbed = false;
    }
  
    public boolean isGrabbed(){
      return grabbed;
    }
  
    public void setGrabbed(boolean grabbed){
      this.grabbed = grabbed;
    }
  
    public void show(){
      updateBall();
  
      fill(ballColor);
      pushMatrix();
        translate(x,y);
        rotate(atan(y_speed/x_speed));
        scale(map(constrain(abs(mag(x_speed, y_speed)), 0, 20), 0, 20, 1, 2),1);
        float audioAverage = (tunnel.getAudioAverage() == 0) ? 1 : tunnel.getAudioAverage();
        ellipse(0, 0, size/audioAverage, size/audioAverage);
      popMatrix();
    }
  
    private void updateBall(){
      // Update ball attributes
      if(grabbed){
        x_speed = mouseX-pmouseX;
        y_speed = mouseY-pmouseY;
        x = mouseX;
        y = mouseY;
        x_force = 0;
        y_force = 0;
      } else {
        // Calculate forces applied on ball and integrate new position and speed
        calculateForces();
        eulerIntegration();
      }
    }
  
    private void calculateForces(){
      x_force = 0;
      y_force = 0;
  
      y_force += mass*gravity;
      applySpringForces();
      applyDragForces();
    }
  
    // Forces for the virtual springs along the 4 walls of the scene to keep the balls inbound
    private void applySpringForces(){
      if(y + size/2 >= height){
        float delta = (y + size/2)-height;
        y_force += -springFactor*delta - dampFactor*y_speed;
      }
      if(y - size/2 <= 0){
        float delta = (y - size/2);
        y_force += -springFactor*delta - dampFactor*y_speed;
      }
      if(x + size/2 >= width){
        float delta = (x + size/2)-width;
        x_force += -springFactor*delta - dampFactor*x_speed;
      }
      if(x - size/2 <= 0){
        float delta = (x - size/2);
        x_force += -springFactor*delta - dampFactor*x_speed;
      }
    }
  
    // Air friction
    private void applyDragForces(){
      float area = sq((float)(0.01*size))*PI;
      if(x_speed != 0) x_force += 0.5*airDensity*-sq(x_speed)*dragCoeff*(x_speed/abs(x_speed))*area;
      if(y_speed != 0) y_force += 0.5*airDensity*-sq(y_speed)*dragCoeff*(y_speed/abs(y_speed))*area;
    }
  
    private void eulerIntegration(){
      float x_accel, y_accel;
      x_accel = x_force/mass;
      y_accel = y_force/mass;
      
      x_speed += x_accel;
      y_speed += y_accel;
      x += x_speed;
      y += y_speed;
    }
    // Mouse is considered to be over the ball if it is within its unscaled bounding box
    public boolean isHandOver(){
      float handX =Main.kinect.RightHandDepthRatio*width;
      float handY =Main.kinect.RightHandRaisedRatio*height;
      return (handX <= x+size/2 && handX >= x-size/2 && handY <= y+size/2 && handY >= y-size/2);
    }
  }

}