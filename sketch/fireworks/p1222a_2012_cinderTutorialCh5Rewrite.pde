/* OpenProcessing Tweak of *@*http://www.openprocessing.org/sketch/84094*@* */
/* !do not delete the line above, required for linking your tweak if you upload again */
//Original code and idea by Robert Hodgin (http://roberthodgin.com/)
//Rewritten by Raven Kwok (aka Guo Ruiwen) in Processing
//p1222a_2012_cinderTutorialCh5Rewrite
/*

raystain@gmail.com
flickr.com/ravenkwok
vimeo.com/ravenkwok
weibo.com/ravenkwok
the-moor.blogbus.com

I read through the first five chapters of Cinder tutorial(http://libcinder.org/docs/v0.8.4/hello_cinder_chapter1.html) in the past two days, and rewrote Hodgin's amazing example of particle system in Processing. 
*/

ParticleController pc;
boolean mouseDown;
PVector mouseLoc,mousePLoc,mouseVel;
PImage pattern;
color base = color(0,255,0);
void setup(){
  size(150,88);
  smooth();
  frameRate(30);
  
  stroke(random(255), random(255), random(255));
  ellipseMode(RADIUS);
  
  pattern = loadImage("bunny.jpg");
  
  pc = new ParticleController();
  mouseLoc = new PVector(0,0,0);
  mousePLoc = new PVector(0,0,0);
  mouseVel = new PVector(0,0,0);
}

void draw(){
  background(0);
  if(mouseDown) pc.addParticles(10,mouseLoc,mouseVel);
  pc.repulse();
  pc.update(pattern);
  pc.display();
}

void mouseDragged(){
  mouseMoved();
  color ran = color(random(255), random(255), random(255));
  stroke(blendColor(base, ran, ADD));
}

void mousePressed(){
  mouseDown = true;
  pc.clear();
  base = color(random(255), random(255), random(255));
}

void mouseReleased(){
  mouseDown = false;

}

void mouseMoved(){
  mousePLoc.set(pmouseX,pmouseY,0);
  mouseLoc.set(mouseX,mouseY,0);
  mouseVel = PVector.sub(mouseLoc,mousePLoc);

}