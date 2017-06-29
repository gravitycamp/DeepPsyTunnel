import processing.core.*;

class Tangle extends PApplet {

    int numCols = 32;
    int numRows = 24;
    float dx, dy;
    float speed = (float) 0.2;

    Row[] rows;

    int[] palette = new int[numCols + 1];

    int width;
    int height;
    String position = "Tunnel";
    Tunnel tunnel;


    public Tangle(Tunnel t, int w, int h, String p) {
        width = w;
        height = h;
        tunnel = t;
        position = p;
    }

    public void settings()
    {
        size(width, height);
    }

    public void setup() {
        colorMode(HSB, 100, 100, 100);
       // frameRate(30);

        for (int i = 0; i < numCols; i++) {
            palette[i] = color(random(37, 47), 50, 75);
        }

        for (int i = (numCols / 2); i >= 0; i--) {
            palette[(int) random(0, numCols - 1)] = color(random(3, 7), random(0,255), random(0, 255));
        }

        generate();
    }

    void generate() {
        rows = new Row[numRows];
        for (int rowNum = 0; rowNum < numRows; rowNum++) {
            rows[rowNum] = new Row(numCols);
            int colNum = 0;
            while (colNum < numCols) {
                if (random(1) < .45) {
                    rows[rowNum].cells[colNum] = new Vertical(palette[colNum]);
                } else {
                    boolean which = false;
                    if (random(1) < .5) which = true;
                    if (colNum != (numCols - 1)) {
                        rows[rowNum].cells[colNum] = new Crossing(palette[colNum], palette[colNum + 1], which);
                        int hold = palette[colNum];
                        palette[colNum] = palette[colNum + 1];
                        palette[colNum + 1] = hold;
                    } else {
                        rows[rowNum].cells[colNum] = new Vertical(palette[colNum]);
                    }
                    colNum++;
                }
                colNum++;
            }
        }
        rows[numRows - 1].start();
    }

    public void draw() {
        synchronized (Tunnel.class) {

            background(10, 18, 10);

            dx = (float) width / (float) numCols;
            dy = (float) height / (float) numRows;
            strokeWeight(dx * (float) 0.25);
            strokeJoin(ROUND);
            strokeCap(ROUND);

            for (int i = numRows - 1; i >= 0; i--) {
                rows[i].display(i * dy, dx, dy);
                if (rows[i].update(speed)) {
                    if (i > 0) rows[i - 1].start();
                }
            }

            if (rows[0].finish) {
                generate();
            }

        }
    }

    class Cell {

        void display(float x, float y, float dx, float dy, float progress) {
        }

    }

    class Vertical extends Cell {
        int col;

        Vertical(int c) {
            col = c;
        }

        void display(float x, float y, float dx, float dy, float p) {
            stroke(col);
            line(x, lerp(y + dy, y, p), x, y + dy);
        }
    }

    class Crossing extends Cell {
        int col1, col2;
        boolean positive;

        Crossing(int c1, int c2, boolean p) {
            col1 = c1;
            col2 = c2;
            positive = p;
        }

        void display(float x, float y, float dx, float dy, float p) {
            float p1 = constrain(map(p, 0, (float) 0.25, 0, 1), 0, 1);
            float p2 = constrain(map(p, (float) 0.75, 1, 0, 1), 0, 1);
            if (positive) {
                stroke(col1);
                line(x + dx, y + dy, lerp(x + dx, x, p), lerp(y + dy, y, p));
                stroke(col2);
                if (p1 != 0)
                    line(x, y + dy, lerp(x, x + dx * (float) 0.25, p1), lerp(y + dy, y + dy * (float) 0.75, p1));
                if (p2 != 0)
                    line(x + dx * (float) 0.75, y + dy * (float) 0.25, lerp(x + dx * (float) 0.75, x + dx, p2), lerp(y + dy * (float) 0.25, y, p2));
            } else {
                stroke(col2);
                line(x, y + dy, lerp(x, x + dx, p), lerp(y + dy, y, p));
                stroke(col1);
                if (p2 != 0)
                    line(x + dx * (float) 0.25, y + dy * (float) 0.25, lerp(x + dx * (float) 0.25, x, p2), lerp(y + dy * (float) 0.25, y, p2));
                if (p1 != 0)
                    line(x + dx, y + dy, lerp(x + dx, x + dx * (float) 0.75, p1), lerp(y + dy, y + dy * (float) 0.75, p1));
            }
        }
    }

    class Row {
        boolean animated, finish;
        float progress;
        Cell[] cells;

        Row(int numCols) {
            animated = false;
            progress = 0;
            cells = new Cell[numCols];
        }

        void start() {
            animated = true;
        }

        void display(float y, float dx, float dy) {
            if (!finish && !animated) return;
            for (int i = 0; i < cells.length; i++) {
                if (cells[i] != null) cells[i].display((float) (i + 0.5) * dx, y, dx, dy, progress);
            }
        }

        boolean update(float p) {
            if (finish) return false;
            if (animated) {
                progress += p;
                if (progress >= 1) {
                    animated = false;
                    finish = true;
                    return true;
                }
            }
            return false;
        }
    }

}