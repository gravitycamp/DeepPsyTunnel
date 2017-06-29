import processing.core.*;

public class Gravity extends PApplet {

    float textScale;
    int   width;
    int height;
    String position = "Tunnel";
    Tunnel tunnel;


    public Gravity(Tunnel t, int w, int h, String p) {
        width  = w;
        height = h;
        tunnel = t;
        position = p;
        textScale = 0;
    }

    public void settings() {
        size(width, height);
    }


    public void setup()
    {
        background(0);
        fill(255, 255, 255);
        textAlign(CENTER, CENTER);
        textSize(height);
    }


    public void draw() {
        synchronized (Tunnel.class) {

            if (tunnel.getAudioAverage() > 1) {
                fill(random(0, 255), random(0, 255), random(0, 255));
            }

            background(0);
            pushMatrix();
            translate(width / 2, height / 2);
            scale((float) 0.1 + sin(textScale), 1);
            text("<<GRAVITY>>", 0, -5);
            popMatrix();

            textScale += 0.02;
        }

    }
}