import processing.core.*;
import java.util.*;

public class Life extends PApplet {

    ArrayList<Cell> cells = new ArrayList<Cell>();
    int resY = 150;
    int resX;

    int[][] indicies = new int[8][2];

    int   width;
    int height;
    String position = "Tunnel";
    Tunnel tunnel;


    public Life(Tunnel t, int w, int h, String p) {
        width  = w;
        height = h;
        tunnel = t;
        position = p;
    }

    public void settings() {
        size(width, height);
    }


    public void setup() {

     //   frameRate(30);

        resX = (int) (resY * width / (float) height);

        for (int i = 0; i < resY; i++) {
            for (int j = 0; j < resX; j++) {
                cells.add(new Cell(i, j));
            }
        }

    }

    public void draw() {
        synchronized (Tunnel.class) {

            background(0);
            noStroke();
            for (Cell c : cells) {

                updateIndicies(indicies, c.row, c.column, resX, resY);
                if (mousePressed) {
                    c.setState(mouseX, mouseY, (float) width / resY);
                }
                c.setNewStat(cells, indicies, resX);
            }
            for (Cell c : cells) {
                c.update();
                c.draw((float) width / resY);
            }

        }
    }

    void updateIndicies(int[][] indices, int row, int column, int resX, int resY) {
        indices[0][0] = column - 1;
        indices[0][1] = row - 1;

        indices[1][0] = column;
        indices[1][1] = row - 1;

        indices[2][0] = column + 1;
        indices[2][1] = row - 1;

        indices[3][0] = column - 1;
        indices[3][1] = row;

        indices[4][0] = column + 1;
        indices[4][1] = row;

        indices[5][0] = column - 1;
        indices[5][1] = row + 1;

        indices[6][0] = column;
        indices[6][1] = row + 1;

        indices[7][0] = column + 1;
        indices[7][1] = row + 1;

        for (int i = 0; i < indices.length; i++) {

            if (indices[i][1] < 0) {
                indices[i][1] = resY - 1;
            } else if (indices[i][1] >= resY) {
                indices[i][1] = 0;
            }

            if (indices[i][0] < 0) {
                indices[i][0] = resX - 1;
            } else if (indices[i][0] >= resX) {
                indices[i][0] = 0;
            }
        }
    }

    class Cell {

        int row;
        int column;
        boolean alive;
        boolean pAlive;

        Cell(int row, int column) {
            this.row = row;
            this.column = column;
            setRandomState(20);
        }

        void setRandomState(float propability) {
            alive = random(100) < propability ? true : false;
            pAlive = alive;
        }

        boolean isAlive() {
            return alive;
        }

        boolean isDead() {
            return alive;
        }

        boolean wasAlive() {
            return pAlive;
        }

        boolean wasDead() {
            return pAlive;
        }

        void update() {
            pAlive = alive;
            //isAlive = isAlive;
        }


        void setState(float mx, float my, float s) {

            float x = s * row;
            float y = s * column;

            if (mx > x && mx < x + s && my > y && my < s + y) {
                alive = true;
            }
        }



        void setNewStat(ArrayList<Cell> cells, int[][] rowsAndColumns, int resX) {


            int aliveNeighborsNum = getAliveNum(cells, rowsAndColumns, resX);

            if (wasDead() &&  aliveNeighborsNum == 3) {
                alive = true;
            } else if (wasAlive() && aliveNeighborsNum == 2 || aliveNeighborsNum == 3) {
                alive = true;
            } else if (wasAlive() && aliveNeighborsNum < 2) {
                alive = false;
            } else if (wasAlive() && aliveNeighborsNum > 3) {
                alive = false;
            }
        }



        int getAliveNum(ArrayList<Cell> cells, int[][] rowsAndColumns, int resX) {
            int sum = 0;
            for (int i = 0; i < rowsAndColumns.length; i++) {

                int index = rowsAndColumns[i][1] * resX + rowsAndColumns[i][0];

                if (cells.get(index).wasAlive()) {
                    sum++;
                }
            }

            return sum;
        }


        public void draw(float s) {
            if (isAlive()) {
                fill(random(0,255),random(0,255),random(0,255));
                rect(s * row, s * column, s, s);
            }
        }
    }

    public void keyPressed() {
        for (Cell c : cells) {
            c.setRandomState(random(5, 95));
        }
    }

}