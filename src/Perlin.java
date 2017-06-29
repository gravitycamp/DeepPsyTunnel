import processing.core.*;
import java.util.*;
import ddf.minim.*;
import ddf.minim.analysis.*;


public class Perlin extends PApplet {

    int width;
    int height;
    String position = "Tunnel";
    Tunnel tunnel;

    AudioInput audio;
    Minim minim;
    BeatDetect beat;

    public Perlin(Tunnel t, int w, int h, String p) {
        width = w;
        height = h;
        tunnel = t;
        position = p;
        audio = tunnel.in;
    }

    public void settings() {
        size(width, height);
    }

    ParticleSystem particleSystem;
    int symmetry = 6;
    int stepSize = 2;
    boolean blur = true;
    boolean zMotion = true;

    //-----------------Setup
    public void setup() {
        minim = new Minim(this);
        beat = new BeatDetect();
        background(0);
        rectMode(CORNERS);
        noFill();
        smooth();
        particleSystem = new ParticleSystem(5);
    }

    float trackX = 0;
    float trackY = 0;
    float trackZ = 0;

    public void track() {
     if(Main.kinect != null) {
         Main.kinect.update();
         trackX = (float)width * Main.kinect.RightHandDepthRatio;
         trackY = (float)height * Main.kinect.RightHandRaisedRatio;
         trackZ = 0;
     } else {
         trackX = mouseX;
         trackY = mouseY;
     }
    }


    //-----------------Main Loop
    public void draw() {
        synchronized(Tunnel.class) {
            track();
            beat.detect(audio.mix);
            if(beat.isOnset()) {
              blur = true;
            } else {
              blur = false;
            }

            symmetry = (int)(trackX/30);
            if (symmetry < 3) {
              symmetry = 3;
            }


            fill(0, 32);
            rect(0, 0, width, height);
            particleSystem.update();

            for (int i = 0; i < symmetry; i++) {
                pushMatrix();
                translate(width / 2, height / 2);
                rotate(i * TWO_PI / symmetry);
                particleSystem.render();
                popMatrix();
            }

            if (blur) {
                convolutionMask4(0xffffffff);
            }

        }
    }

//-----------------Interactions

    public void mousePressed() {
        for (int i = 0; i < 2; i++) {
            particleSystem.particles.add(new Particle());
        }
    }

    public void keyPressed() {
        if (key == ' ') {
            saveFrame("perlinKaleidoscopeActive-####.png");
        }
        if (key == 'b') {
            blur = !blur;
        }
        if (key == 'z') {
            zMotion = !zMotion;
        }
        if (key == CODED) {
            if (keyCode == UP) {
                background(0);
                symmetry++;
            }
            if (keyCode == DOWN) {
                background(0);
                symmetry--;
            }
        }
    }

//-----------------Defined Functions

    void convolutionMask4(int maskVal) {
        loadPixels();
        int[] pixelArray = pixels;
        for (int i = 1; i < height - 1; i++) {
            int yStart = i * width + 1;
            int yFinish = i * width + (width - 1);
            for (int j = yStart; j < yFinish; j++) {
                pixelArray[j] = ((pixelArray[j - width] & maskVal) + (pixelArray[j + width] & maskVal) + (pixelArray[j - 1] & maskVal) + (pixelArray[j + 1] & maskVal)) >> 2;
            }
        }
        updatePixels();
    }


    //-----------------Defined Classes
    class ParticleSystem {
        ArrayList particles;
        float z = random(17);

        ParticleSystem(int initialNumber) {
            particles = new ArrayList(initialNumber);
            for (int i = 0; i < initialNumber; i++) {
                particles.add(new Particle());
            }
            for (int i = 0; i < particles.size() - 1; i++) {
                Particle particle = (Particle) particles.get(i);
                particle.position.z = z;
            }
        }

        void update() {
            for (int i = 0; i < particles.size() - 1; i++) {
                Particle particle = (Particle) particles.get(i);
                if (particle.lifetime > 0) {
                    particle.update();
                } else {
                    //remove particle
                    particles.remove(i);
                }
            }
        }

        void render() {
            for (int i = 0; i < particles.size() - 1; i++) {
                Particle particle = (Particle) particles.get(i);
                particle.render();
            }
        }
    }


    class Particle {
        PVector position, velocity;
        int particleColor = blendColor(color(0,0,255), color(random(255),random(255),random(255)), ADD);
        int lifetime = floor(random(15000) + 20000);

        Particle() {
            position = new PVector(random(width) / 4, random(height) / 4);
            velocity = new PVector();
        }

        void update() {
            float f = (float) 0.01;
            velocity.x = stepSize * cos(TWO_PI * noise(f * position.x, f * position.y, f * position.z));
            velocity.y = stepSize * sin(TWO_PI * noise(f * position.x, f * position.y, f * position.z));
            if (zMotion) {
                position.z += stepSize;
            }

            position.add(velocity);
            lifetime--;

            //deal with edge cases
            if (position.x < 0) {
                position.x += width / 2;
            }

            if (position.x > width / 2) {
                position.x -= width / 2;
            }

            if (position.y < 0) {
                position.y += height / 2;
            }

            if (position.y > height / 2) {
                position.y -= height / 2;
            }
        }

        void render() {
            stroke(particleColor, 155);
            float weight = trackY/30;
            if (weight < 5) {
              weight = 5;
            }
            strokeWeight(weight);
            line(position.x, position.y, position.x + velocity.x, position.y + velocity.y);
        }
    }
}
