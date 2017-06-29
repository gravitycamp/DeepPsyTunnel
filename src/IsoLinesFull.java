import processing.core.*;

class IsoLinesFull extends PApplet {

  int[] values;
  int levels = 10;
  float noiseScale = (float).001;

  int width;
  int height;
    String position = "Tunnel";
  Tunnel tunnel;
  
  public IsoLinesFull(Tunnel t, int w, int h, String p) {
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
    values = new int[width * height];
    background(0);
    noiseDetail(6, (float).5);
  }
   
  public void draw(){
    synchronized (Tunnel.class) {
    float offset = frameCount * (float).005;
    
    // first pass: compute values
    for(int y = 0; y < height; y++) {
      for(int x = 0; x < width; x++) {
        int i = y * width + x;
        values[i] = (int) (levels * noise(
          x * noiseScale,
          y * noiseScale,
          offset));
      }
    }
    }
    // second pass: check neighborhood
    loadPixels();
    int c = color(random(255), random(255), random(255), random(255));
    for(int y = 1; y < height; y++) {
  
      for(int x = 1; x < width; x++) {
        int i = y * width + x;
        int center = values[i];
        if(
          center != values[i - 1] ||
          center != values[i - width])
          pixels[i] = c;
      }
    }
    updatePixels();
    
    fill(0, 5);
    rect(0, 0, width, height);
  }
}