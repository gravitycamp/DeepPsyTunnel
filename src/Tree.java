import processing.core.*;

public class Tree extends PApplet {

    float curlx = 0;
    float curly = 0;
    float f = sqrt(2)/2.f;
    float deley = 10;
    float growth = 0;
    float growthTarget = 1;

    int   width;
    int height;
    String position = "Tunnel";
    Tunnel tunnel;

    int dir = 0;
    int mouseX = 0;
    int mouseY = 0;
    int center = 0;


    public Tree(Tunnel t, int w, int h, String p) {
        width  = w;
        height = h;
        tunnel = t;
        position = p;

        center = width  / 2;
        mouseX = width  / 2;
        mouseY = height / 2;
    }

    public void settings() {
        size(width, height);
    }


    public void setup()
    {
      //  frameRate(10);
        smooth();
    }


    public void draw()
    {
        synchronized (Tunnel.class) {
            if (tunnel.getAudioAverage() > 5) {
                dir = 1 - dir;
            }

            if (dir == 0 && mouseX < width) {
                mouseX += 1;
            } else if (dir == 1 && mouseX > 0) {
                mouseX -= 1;
            }

            growthTarget = 1 + abs(mouseX - center) * (float) 0.01;

            synchronized (Tunnel.class) {
                background(0);
                stroke(150);
                tree(mouseX, mouseX, mouseY);
            }
        }
    }

    public void tree(int center, int mouseX, int mouseY) {
        curlx += (radians(360.f / height * mouseX) - curlx) / deley;
        curly += (radians(360.f / height * mouseY) - curly) / deley;
        translate(center, height / 3 * 2);
        branch(height/3.f, 12);
        growth += (growthTarget / 10 - growth + 1.f) / deley;
    }

    public void branch(float len,int num)
    {
        len *= f;
        num -= 1;
        if((len > 1) && (num > 0))
        {
            pushMatrix();
            rotate(curlx);
            line(0,0,0,-len);
            translate(0,-len);
            branch(len,num);
            popMatrix();

            len *= growth;
            pushMatrix();
            rotate(curlx-curly);
            line(0,0,0,-len);
            translate(0,-len);
            branch(len,num);
            popMatrix();
            //len /= growth;
        }
    }

    static public void main(String args[]) {
        PApplet.main(new String[] { "--bgcolor=#FFFFFF", "RecursiveTree" });
    }
}