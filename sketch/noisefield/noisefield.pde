/* OpenProcessing Tweak of *@*http://www.openprocessing.org/sketch/3897*@* */
/* !do not delete the line above, required for linking your tweak if you upload again */
int NUM_PARTICLES = 50;
ParticleSystem p;
void setup()
{
  smooth();
  size(150,88);
  background(0);
  p = new ParticleSystem();
}

void draw()
{
  println(frameCount);
  noStroke();
  fill(0,5);
  rect(0,0,width,height);
  p.update();
  p.render();
}