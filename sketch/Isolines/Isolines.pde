/* OpenProcessing Tweak of *@*http://www.openprocessing.org/sketch/4879*@* */
/* !do not delete the line above, required for linking your tweak if you upload again */
int[] values;
int levels = 10;
float noiseScale = .001;

void setup() {
  size(150, 88, P2D);
  values = new int[width * height];
  background(0);
  noiseDetail(6, .5);
}

void draw() {
  float offset = frameCount * .005;
  
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