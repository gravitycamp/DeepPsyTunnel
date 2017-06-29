import processing.core.*;

public class Blobs extends PApplet {

    int renk = 100;
    int x;
    int y;

    int width;
    int height;
    String position = "Tunnel";
    Tunnel tunnel;

    public Blobs(Tunnel t, int w, int h, String p) {
        width  = w;
        height = h;
        tunnel = t;
        position = p;
    }

    public void settings() {
        size(width, height);
    }


    public void setup() {
        colorMode(HSB, 100);
        background(100);
        smooth();
        noStroke();
        noCursor();
    }


    public void draw() {
        synchronized(Tunnel.class) {

            if (renk < 100) {
                renk = renk + 1;
            } else {
                renk = 0;
            }

            float x = random(0, width);
            float y = random(0, height);

            float audioAverage = tunnel.getAudioAverage() * 5;
            ellipse(x, y, audioAverage, audioAverage);

            if (tunnel.getAudioAverage() > 10) {
                background(0);
            }

            noStroke();
            fill(renk, 100, 100);

        }
    }

}