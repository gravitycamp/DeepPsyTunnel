import processing.core.*;

class TextScroll extends PApplet {

  PFont f;
  
  int R = (int)random(255);
  int G = (int)random(255);
  int B = (int)random(255);
  int dR = 1; // starting value
  int dG = 5; // starting value
  int dB = 3; // starting value

  int width;
  int height;
    String position = "Tunnel";
  Tunnel tunnel;
  
  public TextScroll(Tunnel t, int w, int h, String p) {
      width = w;
      height = h;
      tunnel = t;
        position = p;
  }

  public void settings()
  {
      size(width, height);
  }


  public void setup()
  {
    // Create the font
    f = createFont("Futura", height*3/4);
    textFont(f);
  }
  
  public void scrollMessage(String s, float speed)
  {
    int x = (int)( width + (millis() * -speed) % (textWidth(s) + width) );
    text(s, x, height*3/4);  
  }
  
  
   
  public void draw(){
    synchronized (Tunnel.class) {
      background(0);
      R+=dR;
      G+=dG;    
      B+=dB;
      
      if ((R <= 0) || (R >= 255))  // if out of bounds
        dR = - dR; // swap direction  
      if ((G <= 0) || (G >= 255))  // if out of bounds
        dG = - dG; // swap direction  
      if ((B <= 0) || (B >= 255))  // if out of bounds
        dB = - dB; // swap direction  
    
      fill(R,G,B);
      scrollMessage("<< Gravity >>", (float)0.05);
      ellipseMode(CENTER);
      float wh = random(3, 10);
      int whvalue = (int)wh;
      // get random x axis point; need to figure out how to test bounds outside or touching window
      float x = random(0, width);
      int xx = (int)x;
        // get random y axis point; need to figure out how to test bounds outside or touching window
      float y = random(0, height);
      int yy= (int)y;
      fill(255,255,255);
    
      ellipse(xx, yy, whvalue, whvalue);
    }
  }
}