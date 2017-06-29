class Particle
{
  PVector position, velocity;

  Particle()
  {
    position = new PVector(random(width),random(height));
    velocity = new PVector(); 
  }
  
  void update()
  {
    velocity.x = 20*(noise(mouseX/10+position.y/100)-0.5);
    velocity.y = 20*(noise(mouseY/10+position.x/100)-0.5);
    position.add(velocity);
    
    if(position.x<0)position.x+=width;
    if(position.x>width)position.x-=width;
    if(position.y<0)position.y+=height;
    if(position.y>height)position.y-=height;
  }

  void render()
  {
    color blended = blendColor(color(0,0,255), color(random(255),random(255),random(255)), ADD);
    stroke(blended);
    line(position.x,position.y,position.x-velocity.x,position.y-velocity.y);
  }
}