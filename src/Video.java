import processing.video.*;
import processing.core.*;

public class Video extends PApplet {

    int   width;
    int height;
    String position = "Tunnel";
    Movie  movie;

    public Video(Movie m, int w, int h) {
        width  = w;
        height = h;
        movie = m;
    }

    public void settings() {
        size(width, height);
        //fullScreen(2);
    }

  public void setup () {
      background(0);
  }
  

  public void draw () {
    synchronized (Tunnel.class) {
      image(movie, 0, 0);
    }
  }
}