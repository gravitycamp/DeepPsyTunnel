import processing.core.*;
import processing.serial.*;

import java.awt.Rectangle;

public class Wire extends PApplet {

    int numPorts = 0;  // the number of serial ports in use
    int maxPorts = 11; // maximum number of serial ports

    Serial[] ledSerial = new Serial[maxPorts];     // each port's actual Serial port
    Rectangle[] ledArea = new Rectangle[maxPorts]; // the area of the movie each port gets, in % (0-100)
    boolean[] ledLayout = new boolean[maxPorts];   // layout of rows, true = even is left->right
    PImage[] ledImage = new PImage[maxPorts];      // image sent to each port
    int[] gammatable = new int[256];
    float gamma = (float) 1.7;


    public Wire() {
        //String[] list = Serial.list();
        //println(list);

        //delay(4000);
        //serialConfigure("COM1");   // Right-side #1 (Master)
        //serialConfigure("COM2");   // Right-side #2
        //serialConfigure("COM3");   // Right-side #3
        //serialConfigure("COM4");   // Right-side #4
        //serialConfigure("COM5");   // Right-roof #5 (roof-right)
        //serialConfigure("COM6");   // Right-side #6 (roof-center)
        //serialConfigure("COM7");   // Left-side  #1
        //serialConfigure("COM8");   // Left-side  #2
        //serialConfigure("COM9");   // Left-side  #3
        //serialConfigure("COM10");  // Left-side  #4
        //serialConfigure("COM11");  // Left-side  #5 (roof-left)

        //for (int i = 0; i < 256; i++) {
        //    gammatable[i] = (int) (pow((float) i / (float) 255.0, gamma) * 255.0 + 0.5);
        //}
    }
    
    public void SetupCom() {
        String[] list = Serial.list();
        println(list);

        delay(4000);
        serialConfigure("COM1");   // Right-side #1 (Master)
        serialConfigure("COM2");   // Right-side #2
        serialConfigure("COM3");   // Right-side #3
        serialConfigure("COM4");   // Right-side #4
        serialConfigure("COM5");   // Right-roof #5 (roof-right)
        serialConfigure("COM6");   // Right-side #6 (roof-center)
        serialConfigure("COM7");   // Left-side  #1
        serialConfigure("COM8");   // Left-side  #2
        serialConfigure("COM9");   // Left-side  #3
        serialConfigure("COM10");  // Left-side  #4
        serialConfigure("COM11");  // Left-side  #5 (roof-left)

        for (int i = 0; i < 256; i++) {
            gammatable[i] = (int) (pow((float) i / (float) 255.0, gamma) * 255.0 + 0.5);
        }
    }


    public void send(PImage frame) {

        SendSerial SendThreads[] = new SendSerial[numPorts];
        for (int j = 0; j < numPorts; j++) {
            SendThreads[j] = new SendSerial();
        }

        for (int i = 0; i < numPorts; i++) {
            // copy a portion of the movie's image to the LED image
            int xoffset = percentage(frame.width, ledArea[i].x);
            int yoffset = percentage(frame.height, ledArea[i].y);
            int xwidth = percentage(frame.width, ledArea[i].width);
            int yheight = percentage(frame.height, ledArea[i].height);
            ledImage[i].copy(frame, xoffset, i * ledImage[i].height, xwidth, ledImage[i].height, 0, 0, ledImage[i].width, ledImage[i].height);
            // convert the LED image to raw data
            byte[] ledData = new byte[(ledImage[i].width * ledImage[i].height * 3) + 3];
            image2data(ledImage[i], ledData, ledLayout[i]);
            ledData[0] = '*';  // others sync to the master board
            int usec = 0;// 5900*(numPorts-i);
            ledData[1] = (byte) (usec);   // request the frame sync pulse
            ledData[2] = (byte) (usec >> 8); // at 75% of the frame time

            // send the raw data to the LEDs  :-)
            SendThreads[i].SetData(i, ledData);
            SendThreads[i].start();
        }

        //wait for all threads to finish
        //
      //  delay(13);
        double startThreads = millis();
        for (int i = 0; i < numPorts; i++) {
            try {
                SendThreads[i].join();
            } catch (InterruptedException e) {}
        }
    }


    public class SendSerial extends Thread {
        int index;
        byte ledDataSendData[];

        public void SetData(int i, byte ledData[]) {
            index = i;
            ledDataSendData = ledData;
        }

        public void run() {
            ledSerial[index].write(ledDataSendData);
        }
    }

    // image2data converts an image to OctoWS2811's raw data format.
    // The number of vertical pixels in the image must be a multiple
    // of 8.  The data array must be the proper size for the image.
    //
    void image2data(PImage image, byte[] data, boolean layout) {

        int offset = 3;
        int x, y, xbegin, xend, xinc, mask;
        int linesPerPin = image.height / 8;
        int pixel[] = new int[8];

        for (y = 0; y < linesPerPin; y++) {
            if ((y & 1) == (layout ? 0 : 1)) {
                // even numbered rows are left to right
                xbegin = 0;
                xend = image.width;
                xinc = 1;
            } else {
                // odd numbered rows are right to left
                xbegin = image.width - 1;
                xend = -1;
                xinc = -1;
            }
            for (x = xbegin; x != xend; x += xinc) {
                for (int i = 0; i < 8; i++) {
                    // fetch 8 pixels from the image, 1 for each pin
                    int j = i;
                    if (j == 0) {
                        j = 2;
                    } else if (j == 2) {
                        j = 0;
                    } else if (j == 4) {
                        j = 6;
                    } else if (j == 6) {
                        j = 4;
                    }
                    int Loc = x + (y + linesPerPin * j) * image.width;
                    pixel[i] = image.pixels[Loc];
                    pixel[i] = colorWiring(pixel[i]);
                }
                // convert 8 pixels to 24 bytes
                for (mask = 0x800000; mask != 0; mask >>= 1) {
                    byte b = 0;
                    for (int i = 0; i < 8; i++) {
                        if ((pixel[i] & mask) != 0) b |= (1 << i);
                    }
                    data[offset++] = b;
                }
            }
        }
    }

    // ask a Teensy board for its LED configuration, and set up the info for it.
    //
    void serialConfigure(String portName) {
        if (numPorts >= maxPorts) {
            println("too many serial ports, please increase maxPorts");
            //  errorCount++;
            return;
        }
        try {
            ledSerial[numPorts] = new Serial(this, portName);
            if (ledSerial[numPorts] == null) throw new NullPointerException();
        } catch (Throwable e) {
            delay(2000);
            try {
                ledSerial[numPorts] = new Serial(this, portName);
                if (ledSerial[numPorts] == null) throw new NullPointerException();
                // ledSerial[numPorts].write('?');
            } catch (Throwable f) {
                println("Port " + portName + "non-functional");
                //   errorCount++;
                return;
            }
        }
        delay(50);
        String line = ledSerial[numPorts].readStringUntil(10);
        if (line == null) {
            //println("Port " + portName + " did not provide info, so set to defaul 150x8");
            line = "150,8,0,0,0,0,0,100,100,0,0,0";
            //errorCount++;
            //return;
        }
        String param[] = line.split(",");
        if (param.length != 12) {
            println("Error: port " + portName + " did not respond to LED config query");
            // errorCount++;
            return;
        }
        // only store the info and increase numPorts if Teensy responds properly
        ledImage[numPorts] = new PImage(Integer.parseInt(param[0]), Integer.parseInt(param[1]), RGB);
        ledArea[numPorts] = new Rectangle(Integer.parseInt(param[5]), Integer.parseInt(param[6]), Integer.parseInt(param[7]), Integer.parseInt(param[8]));
        ledLayout[numPorts] = (Integer.parseInt(param[5]) == 0);
        numPorts++;
    }


    // translate the 24 bit color from RGB to the actual
    // order used by the LED wiring.  GRB is the most common.
    //
    int colorWiring(int c) {
        int red = (c & 0xFF0000) >> 16;
        int green = (c & 0x00FF00) >> 8;
        int blue = (c & 0x0000FF);
        red = gammatable[red];
        green = gammatable[green];
        blue = gammatable[blue];
        return (green << 16) | (red << 8) | (blue); // GRB - most common wiring
    }


    // scale a number by a percentage, from 0 to 100
    //
    int percentage(int num, int percent) {
        double mult = percentageFloat(percent);
        double output = num * mult;
        return (int) output;
    }

    // scale a number by the inverse of a percentage, from 0 to 100
    //
    int percentageInverse(int num, int percent) {
        double div = percentageFloat(percent);
        double output = num / div;
        return (int) output;
    }

    // convert an integer from 0 to 100 to a float percentage
    // from 0.0 to 1.0.  Special cases for 1/3, 1/6, 1/7, etc
    // are handled automatically to fix integer rounding.
    //
    double percentageFloat(int percent) {
        if (percent == 33) return 1.0 / 3.0;
        if (percent == 17) return 1.0 / 6.0;
        if (percent == 14) return 1.0 / 7.0;
        if (percent == 13) return 1.0 / 8.0;
        if (percent == 11) return 1.0 / 9.0;
        if (percent == 9) return 1.0 / 11.0;
        if (percent == 8) return 1.0 / 12.0;
        return (double) percent / 100.0;
    }
}