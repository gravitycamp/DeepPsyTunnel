class ParticleSystem
{
  Particle[] particles;
  
  ParticleSystem()
  {
    particles = new Particle[NUM_PARTICLES];
    for(int i = 0; i < NUM_PARTICLES; i++)
    {
      particles[i]= new Particle();
    }
  }
  
  void update()
  {
    for(int i = 0; i < NUM_PARTICLES; i++)
    {
      particles[i].update();
    }
  }
  
  void render()
  {
    for(int i = 0; i < NUM_PARTICLES; i++)
    {
      particles[i].render();
    }
  }
}
