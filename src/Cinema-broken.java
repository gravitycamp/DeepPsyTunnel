import processing.video.*;
import processing.core.*;
import ddf.minim.analysis.*;

public class Cinema extends PApplet {

    int width;
    int height;
    String position = "Tunnel";
    Tunnel tunnel;
    Movie movie;
    FFT fft;
    float[] fftFilter;
    float spin = (float)0.003;
    float radiansPerBucket = radians(2);
    float decay = (float)0.96;
    float opacity = 20;
    float minSize = (float)0.1;
    float sizeScale = (float)0.4;
    float textScale = 0;
    PImage colors;
    PImage dot;
    
    float md;
    float mt;

    public Cinema(Tunnel t, int w, int h, String p) {
        width = w;
        height = h;
        tunnel = t;
        position = p;
        fft = tunnel.fft;
    }

    public void settings() {
        size(width, height);
    }

    public void setup() {
        movie = new Movie(this, "C:/TunnelGit2/src/data/Cinema.mp4");
        dot = loadImage("C:/TunnelGit2/src/data/dot.png");
        colors = loadImage("C:/TunnelGit2/src/data/colors.png");
        //movie = new Movie(this, "F:/Tunnel2/src/data/Cinema.mp4");
        //dot = loadImage("F:/Tunnel2/src/data/dot.png");
        //colors = loadImage("F:/Tunnel2/src/data/colors.png");
        movie.loop();
        
        fftFilter = new float[fft.specSize()];        
        //movie = new Movie(this, "/Users/skryl/Dropbox/dev/projects/gravity/tunnel/src/data/Cinema.mp4");


        PApplet sketch = new Video(movie, 400, 400);
        String[] args = {"PlayVideo",};
        PApplet.runSketch(args, sketch);

    }


    public void draw() {
     try{
      synchronized(Tunnel.class) {
            background(0);
            md = movie.duration();
            mt = movie.time();
            FlyingBalls();
            
        }
    }
        catch(Exception e){}
    }

  public void FlyingBalls(){
      for (int i = 0; i < fftFilter.length; i++) {
        fftFilter[i] = max(fftFilter[i] * decay, log(1 + fft.getBand(i)) * (float)(1 + i * 0.01));
      }
      
      for (int i = 0; i < fftFilter.length; i += 3) {   
      //  color rgb = colors.get((map(i, 0, fftFilter.length-1, 0, colors.width-1)), colors.height/2);
        tint(colors.get((int)map(i, 0, fftFilter.length-1, 0, colors.width-1), colors.height/2));
        blendMode(ADD);
     
        float size = height * (minSize + sizeScale * fftFilter[i]);
        PVector center = new PVector((float)(width * (fftFilter[i] * 0.2)), 0);
        center.rotate(millis() * spin + i * radiansPerBucket);
        center.add(new PVector((float)(width * 0.5), (float)(height * 0.5)));
     
        image(dot, center.x - size/2, center.y - size/2, size, size);
        fill(colors.get((int)map(i, 0, fftFilter.length-1, 0, colors.width-1), colors.height/2));
        ellipseMode(CORNER);
        //ellipse(center.x - size/2, center.y - size/2, size/15, size/15);
        //ellipse(center.x + size/2, center.y - size/2, size/15, size/15);
        //ellipse(center.x - size/2, center.y + size/2, size/15, size/15);
        //ellipse(center.x + size/2, center.y + size/2, size/15, size/15);
        //ellipse(center.x + size/2, center.y + size/2, size/15, size/15);
      }
    }
    public void movieEvent(Movie movie) {
        movie.read();
    }

}