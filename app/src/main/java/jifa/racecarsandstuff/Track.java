package jifa.racecarsandstuff;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

public class Track {
    private Bitmap image;
    public Bitmap colourImage;
    private Rect scaleRect;
    public float translateX, translateY;
    private Bitmap graphics;
    public Bitmap colourGraphics;
    private ArrayList<ArrayList<Rect>> graphicSpaces;
    public ArrayList<ArrayList<Integer>> startCoords;
    public int scale;

    public Track(View view, int[][] points, int scale, Options options){
        // define an array list for graphic spaces
        graphicSpaces = new ArrayList<>();
        startCoords = new ArrayList<>();
        this.scale = scale;

        // define a string to hold the string representation of the track
        String [][] track = new String[50][50];

        // fill the entire track with grass
        drawTrackSection(track, 0, 0, track.length, track.length, "grass");

        // create lines of tracks from the points defined
        formTrack(track, points, true);
        // create track edges (just the straights)
        formStraightEdges(track);
        // create the track corner edges
        formCornerEdges(track);
        // create a ring of tires surrounding the world
        formWorldBorders(track);
        // form all of the tires
        formTires(track, options.trackFlag);
        // draw the start line
        formStart(track, points);
        // form all oil spills
        if(options.oil) {
            formOil(track);
        }
        // form all track cracks
        if (options.cracks) {
            formCracks(track);
        }

        // entirely assumes the array will be square
        int count = track.length;

        // define options in which to draw the track with such that it does not blur when scaled
        BitmapFactory.Options paintOptions = new BitmapFactory.Options();
        paintOptions.inDither = false;
        paintOptions.inScaled = false;

        // load the graphics with the options
        graphics = BitmapFactory.decodeResource(view.getResources(), R.drawable.graphics, paintOptions);
        colourGraphics = BitmapFactory.decodeResource(view.getResources(), R.drawable.colour_graphics, paintOptions);

        // Define graphic spaces
        for(int y = 0; y < 9; y++){
            graphicSpaces.add(new ArrayList<Rect>());
            for(int x = 0; x < 9; x++){
                graphicSpaces.get(y).add(new Rect((x*10), (y*10), (x*10)+10, (y*10)+10));
            }
        }

        // set the width and height of the individual graphic space sizes
        int indWidth = graphics.getWidth() / 8;
        int indHeight = graphics.getHeight() / 8;

        // define a scale rect in which to print the track size
        scaleRect = new Rect(0, 0, indWidth*count, indHeight*count);

        // define two image canvases in which the graphics can be drawn to
        image = Bitmap.createBitmap(indWidth*count, indHeight*count, Bitmap.Config.RGB_565);
        Canvas imageCanv = new Canvas(image);
        colourImage = Bitmap.createBitmap(indWidth*count, indHeight*count, Bitmap.Config.RGB_565);
        Canvas colourCanv = new Canvas(colourImage);

        // define paint options that will allow the graphics to be drawn correctly to the canvases
        Paint paint = new Paint();
        paint.setFilterBitmap(false);
        paint.setAntiAlias(false);
        paint.setDither(false);

        // cycle for the array size in x and y
        for(int x = 0; x < count; x++){
            for(int y = 0; y < count; y++) {
                // define the space in which to print the texture
                Rect rect = new Rect(indWidth * y, indHeight * x, indWidth*y + indWidth, indHeight * x + indHeight);
                // get the texture needed and draw it to the canvas
                getTexture(track[x][y], imageCanv, colourCanv, rect, paint);
            }
        }
    }

    public void formOil(String[][]track){
        Random rand = new Random();
        // cycle for the size of the array in x and y
        for(int i = 0; i < track.length; i++){
            for(int j = 0; j < track.length; j++){
                // if the array at the following four places is "track"
                if(track[i][j].equals("track") && track[i+1][j+1].equals("track") &&
                        track[i][j+1].equals("track") && track[i+1][j].equals("track")){
                    // a one in 300 chance that oil will be placed on the track
                    if (rand.nextInt(300) == 0){
                        track[i][j] = "1_oil";
                        track[i][j+1] = "2_oil";
                        track[i+1][j] = "3_oil";
                        track[i+1][j+1] = "4_oil";
                    }
                }
            }
        }
    }

    public void formCracks(String[][]track){
        Random rand = new Random();
        // cycle for the size of the array on x and y
        for(int i = 0; i < track.length; i++){
            for(int j = 0; j < track.length; j++){
                // if the index of the track is "track"
                if(track[i][j].equals("track")){
                    // one in 50 chance of cracks being set
                    if(rand.nextInt(50) == 0) {
                        track[i][j] = "crack";
                    }
                }
            }
        }
    }

    public void formStart(String[][] track, int[][]points){
        // draw a section of track as the start line
        drawTrackSection(track, points[0][0], points[0][1], points[0][0] + points[0][2], points[0][1] + 1, "start");
        // draw the starting positions of the track spaces
        for(int y = 0; y < 16; y+=4){
            for(int x = 0; x < 6; x+= 3) {
                // if the x is on the right side of the track shuffle the y axis down
                if (x == 3){y += 2;}
                // add the coordinates of the starting position to the array
                startCoords.add(new ArrayList<Integer>());
                startCoords.get(startCoords.size()-1).add(points[0][1] + 2 + y);
                startCoords.get(startCoords.size()-1).add(points[0][0] + x);
                track[points[0][1] + 2 + y][points[0][0] + x] = "sp000";
                track[points[0][1] + 3 + y][points[0][0] + x] = "sp180";
                track[points[0][1] + 2 + y][points[0][0] + 1 + x] = "sp090";
                track[points[0][1] + 3 + y][points[0][0] + 1 + x] = "sp270";
                // shuffle the y index is back
                if (x == 3){y -= 2;}
            }
        }
    }

    public void formTires(String[][] track, int trackFlag){
        if (trackFlag == 0) {
            drawTrackSection(track, 2, 2, 10, 4, "tires");
            drawTrackSection(track, 10, 3, 15, 4, "tires");
            drawTrackSection(track, 2, 4, 4, 6, "tires");
            drawTrackSection(track, 3, 6, 4, 8, "tires");
            drawTrackSection(track, 11, 11, 39, 12, "tires");
            drawTrackSection(track, 35, 12, 39, 13, "tires");
            drawTrackSection(track, 23, 20, 49, 21, "tires");
            drawTrackSection(track, 22, 21, 23, 27, "tires");
            drawTrackSection(track, 23, 21, 25, 24, "tires");
            drawTrackSection(track, 23, 24, 24, 25, "tires");
            drawTrackSection(track, 25, 21, 26, 22, "tires");
            drawTrackSection(track, 35, 28, 37, 39, "tires");
            drawTrackSection(track, 34, 36, 35, 38, "tires");
            drawTrackSection(track, 33, 37, 34, 38, "tires");
            drawTrackSection(track, 11, 38, 35, 39, "tires");
            drawTrackSection(track, 12, 37, 17, 38, "tires");
            drawTrackSection(track, 12, 36, 14, 37, "tires");
            drawTrackSection(track, 12, 35, 13, 36, "tires");
            drawTrackSection(track, 11, 12, 12, 38, "tires");
            drawTrackSection(track, 12, 12, 14, 14, "tires");
            drawTrackSection(track, 12, 14, 13, 20, "tires");
        }
        else if(trackFlag == 1){
            drawTrackSection(track, 21, 11, 39, 24, "tires");
            drawTrackSection(track, 23, 13, 37, 22, "grass");

            drawTrackSection(track, 11, 31, 14, 39, "tires");
        }
        else if (trackFlag == 2){
            drawTrackSection(track, 20, 21, 21, 23, "tires");
            drawTrackSection(track, 21, 22, 22, 34, "tires");
            drawTrackSection(track, 10, 20, 20, 23, "tires");
            drawTrackSection(track, 10, 16, 12, 20, "tires");
            drawTrackSection(track, 29, 1, 30, 25, "tires");
            drawTrackSection(track, 30, 20, 31, 26, "tires");
            drawTrackSection(track, 31, 22, 32, 25, "tires");
            drawTrackSection(track, 39, 12, 40, 30, "tires");
            drawTrackSection(track, 40, 11, 41, 19, "tires");
            drawTrackSection(track, 41, 12, 42, 15, "tires");
            drawTrackSection(track, 22, 31, 23, 34, "tires");
            drawTrackSection(track, 23, 32, 24, 35, "tires");
            drawTrackSection(track, 24, 33, 25, 36, "tires");
            drawTrackSection(track, 25, 33, 26, 36, "tires");
            drawTrackSection(track, 26, 34, 27, 37, "tires");
            drawTrackSection(track, 27, 35, 38, 37, "tires");
            drawTrackSection(track, 36, 34, 39, 35, "tires");
            drawTrackSection(track, 39, 29, 40, 35, "tires");
            drawTrackSection(track, 38, 33, 39, 36, "tires");
        }
    }

    public void formWorldBorders(String[][]track){
        // draw lines of tires around the entire track
        drawTrackSection(track, 0, 0, track.length, 1, "tires");
        drawTrackSection(track, track.length-1, 0, track.length, track.length, "tires");
        drawTrackSection(track, 0, track.length-1, track.length, track.length, "tires");
        drawTrackSection(track, 0, 0, 1, track.length, "tires");
    }

    public void formTrack(String[][]track, int[][]p, boolean link){
        // for the length of the points list
        for(int i = 0; i < p.length - 1; i++){
            // draw a track line from a point to the next
            drawTrackLine(track, p[i][0], p[i][1], p[i+1][0], p[i+1][1], p[i][2]);
        }
        // if the track should be linked, draw the last line from the end to the start point
        if(link)
            drawTrackLine(track, p[p.length - 1][0], p[p.length - 1][1], p[0][0], p[0][1], p[p.length - 1][2]);
    }

    public void formCornerEdges(String[][]track){
        for(int i = 0; i < track.length - 2; i++){
            for(int j = 0; j < track.length - 2; j++){
                //      00, 01, 10, 11
                boolean zz, zo, oz, oo;
                zz = track[j][i].equals("track");
                zo = track[j][i+1].equals("track");
                oz = track[j+1][i].equals("track");
                oo = track[j+1][i+1].equals("track");

                if (zz) {
                    if (zo) {
                        if (!oz && oo){
                            // insert a small corner rotated 270 degrees
                            track[j + 1][i] = "cs270";
                        } else if (oz && !oo){
                            // insert a small corner rotated 180 degrees
                            track[j + 1][i + 1] = "cs180";
                        }
                    } else {
                        if (!oz && !oo){
                            // insert a big corner rotated 180 degrees
                            track[j + 1][i + 1] = "cb180";
                        } else if (oz && oo){
                            // insert a small corner rotated 90 degrees
                            track[j][i + 1] = "cs090";
                        }
                    }
                } else {
                    if (zo) {
                        if (!oz && !oo){
                            // insert a big corner rotated 270 degrees
                            track[j + 1][i] = "cb270";
                        } else if (oz && oo){
                            // insert a small corner rotated 0 degrees
                            track[j][i] = "cs000";
                        }
                    } else {
                        if (oz && !oo){
                            // insert a big corner rotated 90 degrees
                            track[j][i + 1] = "cb090";
                        } else if (!oz && oo){
                            // insert the big corner rotated 0 degrees
                            track[j][i] = "cb000";
                        }
                    }
                }
            }
        }
    }

    public void formStraightEdges(String[][]track){
        for(int i = 0; i < track.length - 2; i++){
            for(int j = 0; j < track.length - 2; j++){
                //      00, 01, 10, 11
                boolean zz, zo, oz, oo;
                zz = track[j][i].equals("track");
                zo = track[j][i+1].equals("track");
                oz = track[j+1][i].equals("track");
                oo = track[j+1][i+1].equals("track");

                if(zz){
                    if(zo){
                        if(!oz && !oo){
                            // insert two edges rotated 270 degrees
                            track[j+1][i] = "ed270";
                            track[j+1][i+1] = "ed270";
                        }
                    } else {
                        if (oz && !oo){
                            // insert two edges rotated 180 degrees
                            track[j][i+1] = "ed180";
                            track[j+1][i+1] = "ed180";
                        }
                    }
                } else {
                    if(oz){
                        if(!zo && oo){
                            // insert two edges rotated 90 degrees
                            track[j][i] = "ed090";
                            track[j][i+1] = "ed090";
                        }
                    } else if(zo && oo){
                        // insert two edges rotated 0 degrees
                        track[j][i] = "ed000";
                        track[j+1][i] = "ed000";
                    }
                }
            }
        }
    }

    public void drawTrackLine(String[][]track, int x,int y,int x2, int y2, int stroke) {
        // http://tech-algorithm.com/articles/drawing-line-using-bresenham-algorithm/
        // get the difference between the two points in x and y
        int w = x2 - x ;
        int h = y2 - y ;
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
        // solve the orientation of the line
        if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
        if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
        if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
        int longest = Math.abs(w) ;
        int shortest = Math.abs(h) ;
        if (!(longest>shortest)) {
            longest = Math.abs(h) ;
            shortest = Math.abs(w) ;
            if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
            dx2 = 0 ;
        }
        int numerator = longest >> 1 ;
        for (int i=0;i<=longest;i++) {
            // draw a section of track dependent on the stroke width
            for(int k = 0; k < stroke; k++){
                for(int l = 0; l < stroke; l++){
                    track[y + k][x + l] = "track";
                }
            }
            numerator += shortest ;
            if (!(numerator<longest)) {
                numerator -= longest ;
                x += dx1 ;
                y += dy1 ;
            } else {
                x += dx2 ;
                y += dy2 ;
            }
        }
    }

    public void drawTrackSection(String[][]track, int x0, int y0, int x1, int y1, String tag){
        // for the start to the end of both x and y sizes passed
        for(int y = y0; y < y1; y++){
            for(int x = x0; x < x1; x++){
                // insert the string passed to the track location
                track[y][x] = tag;
            }
        }
    }

    public void getTexture(String str, Canvas canvas, Canvas colourCanvas, Rect rect, Paint paint){
        int rowIndex = 0, colIndex = 0;
        // if the last character is a digit
        if (Character.isDigit(str.charAt(str.length() - 1))){
            // switch the beginning of the string to determine the row index needed for loading the
            // correct graphics space
            switch(str.substring(0, 2)){
                case "ed": rowIndex = 1; break;
                case "cb": rowIndex = 2; break;
                case "cs": rowIndex = 3; break;
                case "sp": rowIndex = 5; break;
            }
            // parse the rest of the string to an integer and divide it by 90 in order to get the
            // correct column index need to load the correct graphics space
            colIndex += Integer.parseInt(str.substring(2, 5)) / 90;
        } else {
            Random rand = new Random();
            // switch the entire string and set the row index in which the graphic can be found
            // also set a random column index in order to provide the track variation
            switch(str){
                case "grass": rowIndex = 0; colIndex = rand.nextInt(3); break;
                case "track": rowIndex = 4; colIndex = rand.nextInt(3); break;
                case "start": rowIndex = 4; colIndex = 3; break;
                case "tires": rowIndex = 0; colIndex = 3; break;
                case "1_oil": rowIndex = 6; colIndex = 0; break;
                case "2_oil": rowIndex = 6; colIndex = 1; break;
                case "3_oil": rowIndex = 7; colIndex = 0; break;
                case "4_oil": rowIndex = 7; colIndex = 1; break;
                case "crack": rowIndex = 6; colIndex = rand.nextInt(2) + 2; break;
            }
        }
        // draw the graphic found at the graphic space index with the paint defined earlier
        canvas.drawBitmap(graphics, graphicSpaces.get(rowIndex).get(colIndex), rect, paint);
        // draw the colour graphic found in the same location
        colourCanvas.drawBitmap(colourGraphics, graphicSpaces.get(rowIndex).get(colIndex), rect, paint);
    }

    public void update(float translateX, float translateY){
        this.translateX = translateX;
        this.translateY = translateY;
    }

    public void draw(Canvas canvas){
        // draw the track
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.translate(translateX, translateY);
        canvas.scale(scale, scale);
        canvas.drawBitmap(image, null, scaleRect, null);
        canvas.restore();
    }
}