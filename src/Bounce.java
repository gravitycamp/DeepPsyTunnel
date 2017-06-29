import java.util.*;
import processing.core.*;

class Bounce extends PApplet {

    final float dt = 1 / (float) 30.0;
    final float dt2 = dt * dt;
    final float damping = (float) 0.1;
    final float repulsionMaxDist = (float) 5.5;
    final float repulsionStiffness = 40;
    final float mouseMaxDist = 50;
    final float mouseStiffness = 20;
    final float particleRadius = 1;
    final float randomAcceleration = 0;

    final int nbParticles = 500;
    final int gridSize = 10;

    Particle[] particles;
    GridCell[] grid;
    boolean letterReset = false;
    int gridW, gridH;
    final float rmd2 = repulsionMaxDist * repulsionMaxDist;
    final float mmd2 = mouseMaxDist * mouseMaxDist;

    String banner = "GRAVITY";
    int bannerIdx = 0;

    int width;
    int height;
    String position = "Tunnel";
    Tunnel tunnel;

    public Bounce(Tunnel t, int w, int h, String p) {
        tunnel = t;
        position = p;
        width  = w;
        height = h;
    }


    class Particle {
        float x, y, vx, vy, ax, ay, fx, fy;

        Particle() {
            x = y = vx = vy = ax = ay = fx = fy = (float) 0.0;
            x = random(0, width);
            y = random(0, height);
        }

        void update() {
            fx += random(-randomAcceleration, randomAcceleration);
            fy += random(-randomAcceleration, randomAcceleration);
            x += vx * dt + 0.5 * ax * dt2;
            y += vy * dt + 0.5 * ay * dt2;
            ax = fx;
            ay = fy;
            vx += 0.5 * ax * dt;
            vy += 0.5 * ay * dt;

            if (x < particleRadius) {
                x = 2 * particleRadius - x;
                vx = -vx;
            } else if (x > width - particleRadius) {
                x = 2 * (width - particleRadius) - x;
                vx = -vx;
            }
            if (y < particleRadius) {
                y = 2 * particleRadius - y;
                vy = -vy;
            } else if (y > height - particleRadius) {
                y = 2 * (height - particleRadius) - y;
                vy = -vy;
            }
            stroke(random(255), random(255), random(255), tunnel.getAudioAverage()*30);
        }
    }

    class GridCell {
        boolean tested;
        Vector contents;

        GridCell() {
            contents = new Vector();
        }
    }

    public void settings() {
        size(width, height);
    }

    public void setup() {
       frameRate(60);  // highest possible

        particles = new Particle[nbParticles];
        for (int i = 0; i < particles.length; i++)
            particles[i] = new Particle();

        gridW = ceil(width / (float) gridSize);
        gridH = ceil(height / (float) gridSize);
        grid = new GridCell[gridW * gridH];
        for (int i = 0; i < grid.length; i++)
            grid[i] = new GridCell();

        noFill();
        stroke(color(255));
    }

    void computeSprings() {
        for (int i = 0; i < grid.length; i++) { // resetting the grid
            grid[i].contents.clear();
            grid[i].tested = false;
        }

        for (int i = 0; i < particles.length; i++) { // filling the grid with the indexes of the particles
            Particle p = particles[i];
            grid[floor(p.y) / gridSize * gridW + floor(p.x) / gridSize].contents.add(p);
        }

        for (int y = 0; y < gridH; y++) {
            for (int x = 0; x < gridW; x++) { // for each cell of the grid
                GridCell gc = grid[y * gridW + x];
                gc.tested = true;
                for (int i = 0; i < gc.contents.size(); i++) { // each particle in that cell
                    Particle pA = (Particle) gc.contents.get(i);
                    for (int j = i + 1; j < gc.contents.size(); j++) { // others particles in that cell
                        // not putting that code in a separate function as it is called a lot
                        Particle pB = (Particle) gc.contents.get(j);
                        float dx = pB.x - pA.x;
                        float dy = pB.y - pA.y;
                        float d = dx * dx + dy * dy;
                        if (d > rmd2 || d == 0) continue;
                        d = sqrt(d);
                        float f = repulsionStiffness * (d - repulsionMaxDist) / d;
                        pA.fx += f * dx;
                        pA.fy += f * dy;
                        pB.fx -= f * dx;
                        pB.fy -= f * dy;
                        pA.fx -= pA.vx * damping;
                        pA.fy -= pA.vy * damping;
                        pB.fx -= pB.vx * damping;
                        pB.fy -= pB.vy * damping;
                    }

                    for (int y2 = y; y2 <= min(y + 1, gridH - 1); y2++) //y2=max(0, y-1)
                    {
                        for (int x2 = max(0, x - 1); x2 <= min(x + 1, gridW - 1); x2++) { // neighbooring cells
                            GridCell gc2 = grid[y2 * gridW + x2];
                            if (gc2.tested) continue;
                            for (int j = 0; j < gc2.contents.size(); j++) { // each particle in that cell
                                // same code as up there, moving it in a function makes me lose 10 fps
                                Particle pB = (Particle) gc2.contents.get(j);
                                float dx = pB.x - pA.x;
                                float dy = pB.y - pA.y;
                                float d = dx * dx + dy * dy;
                                if (d > rmd2 || d == 0) continue;
                                d = sqrt(d);
                                float f = repulsionStiffness * (d - repulsionMaxDist) / d;
                                pA.fx += f * dx;
                                pA.fy += f * dy;
                                pB.fx -= f * dx;
                                pB.fy -= f * dy;
                                pA.fx -= pA.vx * damping;
                                pA.fy -= pA.vy * damping;
                                pB.fx -= pB.vx * damping;
                                pB.fy -= pB.vy * damping;
                            }
                        }
                    }
                }
            }
        }
    }

    void mouseRepulsion() {  // using the grid even for that !
        int xmin = max(0, floor((trackX - mouseMaxDist) / gridSize));
        int xmax = min(gridW, ceil((trackX + mouseMaxDist) / gridSize));
        int ymin = max(0, floor((trackY - mouseMaxDist) / gridSize));
        int ymax = min(gridH, ceil((trackY + mouseMaxDist) / gridSize));

        for (int x = xmin; x < xmax; x++) {
            for (int y = ymin; y < ymax; y++) {
                GridCell gc = grid[y * gridW + x];
                for (int i = 0; i < gc.contents.size(); i++) {
                    Particle p = (Particle) gc.contents.get(i);
                    float dx = trackX - p.x;
                    float dy = trackY - p.y;
                    float d = dx * dx + dy * dy;
                    if (d < mmd2) {
                        d = sqrt(d);
                        float f = mouseStiffness * (d - mouseMaxDist) / d;
                        p.fx += f * dx;
                        p.fy += f * dy;
                    }
                }
            }
        }
    }

    void removeLetter(char c) {
        //PImage letter = loadImage("src/data/chars/" + (int) c + ".png");
        PImage letter = loadImage("C:/TunnelGit2/src/data/chars/" + (int) c + ".png");
        letter.resize(width, height);
        letter.loadPixels();
        for (int i = 0; i < particles.length; i++) {
            Particle p = particles[i];
            if (letterReset) {
                p.x = random(0, width);
                p.y = random(0, height);
            }
            if ((letter.pixels[floor(p.y) * width + floor(p.x)] & 0xFF) != 0) { // If the particle is inside the letter boundaries
                // Take a random direction and move the particle
                float a = random(0, 2 * PI);
                float rx = cos(a) * 10;
                float ry = sin(a) * 10;
                for (int j = 0; j < 5 && (letter.pixels[floor(p.y) * width + floor(p.x)] & 0xFF) != 0; j++) {
                    p.x = constrain(p.x + rx, 0, width - 1);
                    p.y = constrain(p.y + ry, 0, height - 1);
                }
                // If it doesn't work, move it to a complete random position
                for (int j = 0; j < 5 && (letter.pixels[floor(p.y) * width + floor(p.x)] & 0xFF) != 0; j++) {
                    p.x = random(0, width);
                    p.y = random(0, height);
                }
            }
        }
    }


float trackX = 0;
  float trackY = 0;
  float trackZ = 0;

  public void track() {
   if(Main.kinect != null) {
       Main.kinect.update();
       switch (position) {
         case "Tunnel":
         case "Wall":
         case "Ceil":
           trackX = (float)width * (Main.kinect.RightHandDepthRatio + Main.kinect.LeftHandDepthRatio)/2;
           trackY = (float)height * Main.kinect.HandDistance;
           trackZ = 0; 
           break;
         case "RWall":
           trackX = (float)width * Main.kinect.RightHandDepthRatio;
           trackY = (float)height * Main.kinect.RightHandRaisedRatio;
           trackZ = 0;
           break;
         case "LWall":
           trackX = (float)width * Main.kinect.LeftHandDepthRatio;
           trackY = (float)height * Main.kinect.LeftHandRaisedRatio;
           trackZ = 0;
           break;
       }
   } else {
       trackX = mouseX;
       trackY = mouseY;
   }
  }

    public void draw() {
        synchronized(Tunnel.class) {
          track();

          background(color(0));
          for (int i = 0; i < particles.length; i++)
              particles[i].fx = particles[i].fy = 0;

          long startTime = System.nanoTime();
          mouseRepulsion();
          computeSprings();
          for (int i = 0; i < particles.length; i++)
              particles[i].update();

          for (int i = 0; i < particles.length; i++)
              point(particles[i].x, particles[i].y);
          try{
            if (tunnel.getAudioAverage() > 1) {
              removeLetter(banner.charAt(bannerIdx));
              bannerIdx = (bannerIdx + 1) % banner.length();
            }
                  }
          catch(Exception e){}
            

        }
    }

    public void keyPressed() {
        if (key == ENTER)
            letterReset = !letterReset;

        if (key == ' ') {
            for (int i = 0; i < particles.length; i++)
                particles[i] = new Particle();
        }

        if ((key >= 'a' && key <= 'z')
                || (key >= 'A' && key <= 'Z')
                || (key >= '0' && key <= '9'))
            removeLetter(key);
    }
}