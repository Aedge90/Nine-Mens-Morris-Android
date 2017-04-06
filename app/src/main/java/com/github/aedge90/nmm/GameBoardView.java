package com.github.aedge90.nmm;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import android.view.View;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.Gravity;
import android.support.v4.view.ViewCompat;

import android.view.animation.AnticipateInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class GameBoardView {

    private volatile boolean uiupdated = false;
    private Lock lock;
    private Condition uiupdate;
    private ImageView[][] fieldView;
    private LinkedList<ImageView> piecesSpaceViewsBlack;
    private LinkedList<ImageView> piecesSpaceViewsWhite;
    private GameModeActivity c; 
    private GridLayout fieldLayout;
    private FrameLayout piecesSpaceLayout;
    private ImageView[] millSectors;

    private BitmapManager bitmapManager;

    public static int ANIM_DURATION = 1250;

    GameBoardView(GameModeActivity c , GridLayout fieldLayout) {
        this.c = c;
        this.fieldLayout = fieldLayout;

        millSectors = new ImageView[3];

        bitmapManager = new BitmapManager(c);

        fieldView = new ImageView[GameBoard.LENGTH][GameBoard.LENGTH];

        uiupdated = true;
        lock = new ReentrantLock();
        uiupdate = lock.newCondition();

        int remainingPixels = c.screenWidth % GameBoard.LENGTH;
        for (int y = 0; y < GameBoard.LENGTH; y++) {
            for (int x = 0; x < GameBoard.LENGTH; x++) {
                ImageView sector = createEmptySector(x, y);
                fieldLayout.addView(sector);
                fieldView[y][x] = sector;
            }
        }

        int setCount = 0;
        if (c.options.millVariant == Options.MillVariant.MILL5) {
            setCount = 5;
        } else if (c.options.millVariant == Options.MillVariant.MILL7) {
            setCount = 7;
        } else if (c.options.millVariant == Options.MillVariant.MILL9) {
            setCount = 9;
        }
        piecesSpaceViewsBlack = new LinkedList<>();
        piecesSpaceViewsWhite = new LinkedList<>();
        piecesSpaceLayout = (FrameLayout) c.findViewById(R.id.player_pieces_space);
        for(int i = 0; i < setCount; i++) {
            piecesSpaceViewsBlack.push(createSectorInPiecesSpace(Options.Color.BLACK, i * ((c.screenWidth-(c.screenWidth/GameBoard.LENGTH))/2)/setCount));
            piecesSpaceViewsWhite.push(createSectorInPiecesSpace(Options.Color.WHITE, i * ((c.screenWidth-(c.screenWidth/GameBoard.LENGTH))/2)/setCount));
            //adding the views in this order ensures there is no piece hidden by another one while setting
            piecesSpaceLayout.addView(piecesSpaceViewsBlack.peek());
            piecesSpaceLayout.addView(piecesSpaceViewsWhite.peek());
        }

        // set width not to screenwidth so the gridlayout size matches its content size
        // this way the background image of the gridview will always be aligned independent of screen resolution
        ((FrameLayout) fieldLayout.getParent()).updateViewLayout(fieldLayout,
                new FrameLayout.LayoutParams(c.screenWidth - remainingPixels, c.screenWidth - remainingPixels));

        // piecesSpaceLayout contains the fieldlayout. This way we dont need setClipChildren on the Parent View, as the animated pieces
        // never leave the piecesSpaceLayout. Here we make sure that there is extra space below the gameboard for the pieces
        ((LinearLayout) piecesSpaceLayout.getParent()).updateViewLayout(piecesSpaceLayout,
                new LinearLayout.LayoutParams(c.screenWidth - remainingPixels, c.screenWidth - remainingPixels + c.screenWidth/GameBoard.LENGTH));
    }
    
    public ImageView getPos(Position pos){
        return fieldView[pos.getY()][pos.getX()]; 
    }


    protected void waitforAnimation() throws InterruptedException{
        lock.lock();
        try {
            while(!uiupdated) { //necessary to avoid lost wakeup
                uiupdate.await();
            }
        } finally { 
            uiupdated = false;
            lock.unlock();
        }
    }
    
    private void signalUIupdate(){
        lock.lock();
        uiupdated = true;
        uiupdate.signal();
        lock.unlock();
    }

    public void runMoveAnimation (ImageView animSector, ImageView destSector, ViewPropertyAnimatorListener listen) {

        //while jumping this is important so the moving pieces does not move underneath the others
        animSector.bringToFront();

        ViewCompat.animate(animSector)
                .translationX( destSector.getLeft() - animSector.getLeft() )
                .translationY( destSector.getTop() - animSector.getTop() )
                .setDuration(ANIM_DURATION)
                .withLayer()                    //enables hardware acceleration for this animation
                .setListener(listen)
                .start();

    }

    public void makeSetMove(final Move move, final Options.Color color, final GameModeActivity.OnFieldClickListener destListener) throws InterruptedException {

        c.runOnUiThread(new Runnable() {
            public void run() {

                final ImageView animSector;
                if(color.equals(Options.Color.BLACK)) {
                    animSector = piecesSpaceViewsBlack.pop();
                }else{
                    animSector = piecesSpaceViewsWhite.pop();
                }

                final ImageView destSector = fieldView[move.getDest().getY()][move.getDest().getX()];
                final ImageView newDestSector = createSector(color, move.getDest().getX(), move.getDest().getY());

                ViewPropertyAnimatorListener listen = new ViewPropertyAnimatorListener(){

                    @Override
                    public void onAnimationStart(View view) {
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        piecesSpaceLayout.removeView(animSector);
                        fieldLayout.addView(newDestSector);
                        signalUIupdate();
                        newDestSector.setOnClickListener(destListener);
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                    }

                };

                runMoveAnimation(animSector, destSector, listen);

                fieldLayout.removeView(destSector);

                fieldView[move.getDest().getY()][move.getDest().getX()] = newDestSector;

            }
        });

    }

    public void makeMove(final Move move, final Options.Color color, final GameModeActivity.OnFieldClickListener srcListener,
                         final GameModeActivity.OnFieldClickListener destListener) throws InterruptedException{

        c.runOnUiThread(new Runnable() {
            public void run() {

                final ImageView animSector = fieldView[move.getSrc().getY()][move.getSrc().getX()];
                final ImageView destSector = fieldView[move.getDest().getY()][move.getDest().getX()];
                final ImageView newSrcSector = createSector(Options.Color.NOTHING, move.getSrc().getX(), move.getSrc().getY());
                final ImageView newDestSector = createSector(color, move.getDest().getX(), move.getDest().getY());

                ViewPropertyAnimatorListener listen = new ViewPropertyAnimatorListener(){

                    @Override
                    public void onAnimationStart(View view) {
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        fieldLayout.removeView(animSector);
                        fieldLayout.addView(newDestSector);
                        fieldLayout.addView(newSrcSector);
                        signalUIupdate();
                        newSrcSector.setOnClickListener(srcListener);
                        newDestSector.setOnClickListener(destListener);
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                    }
                    
                };

                runMoveAnimation(animSector, destSector, listen);

                fieldLayout.removeView(destSector);
                
                fieldView[move.getSrc().getY()][move.getSrc().getX()] = newSrcSector;
                fieldView[move.getDest().getY()][move.getDest().getX()] = newDestSector;
            }
        });

    }


    private void runKillAnimation(ImageView animSector, ViewPropertyAnimatorListener listen) {

        ViewCompat.animate(animSector)
                .scaleX(0)
                .scaleY(0)
                .setInterpolator(new AnticipateInterpolator())
                .setDuration(ANIM_DURATION/2)
                .withLayer()                    //enables hardware acceleration for this animation
                .setListener(listen)
                .start();

    }


    public void animateKill(final Position killPos, final GameModeActivity.OnFieldClickListener killPosListener){

        c.runOnUiThread(new Runnable() {
            public void run() {

                final ImageView animSector = fieldView[killPos.getY()][killPos.getX()];
                final ImageView newKillSector = createSector(Options.Color.NOTHING, killPos.getX(), killPos.getY());

                ViewPropertyAnimatorListener listen = new ViewPropertyAnimatorListener() {

                    @Override
                    public void onAnimationStart(View view) {
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        fieldLayout.removeView(animSector);
                        fieldLayout.addView(newKillSector);
                        signalUIupdate();
                        newKillSector.setOnClickListener(killPosListener);
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                    }

                };

                runKillAnimation(animSector, listen);

                fieldView[killPos.getY()][killPos.getX()] = newKillSector;

            }
        });

    }

    protected ImageView createSector(Options.Color color, int x, int y) {

        ImageView imageView = createEmptySector(x,y);

        imageView.setImageBitmap(bitmapManager.getBitmap(color));
        imageView.setScaleType(ImageView.ScaleType.CENTER);

        return imageView;
    }

    protected ImageView createEmptySector(int x, int y) {

        ImageView imageView = new ImageView(c);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(y), GridLayout.spec(x));
        params.width = c.screenWidth / GameBoard.LENGTH;
        params.height = c.screenWidth / GameBoard.LENGTH;
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(false);

        return imageView;
    }

    private ImageView createSectorInPiecesSpace(Options.Color color, int margin) {

        final ImageView imageView = new ImageView(c);
        FrameLayout.LayoutParams params;
        int width = c.screenWidth / GameBoard.LENGTH;
        int height = c.screenWidth / GameBoard.LENGTH;
        if(color.equals(Options.Color.BLACK)) {
            params = new FrameLayout.LayoutParams(width, height, Gravity.RIGHT | Gravity.BOTTOM);
            params.setMargins(0, 0, margin, 0);
        }else{
            params = new FrameLayout.LayoutParams(width, height, Gravity.LEFT | Gravity.BOTTOM);
            params.setMargins(margin, 0, 0, 0);
        }
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(false);

        imageView.setImageBitmap(bitmapManager.getBitmap(color));
        imageView.setScaleType(ImageView.ScaleType.CENTER);

        return imageView;
    }
    
    void paintMillOnUIThread(final Position[] mill) throws InterruptedException{
        c.runOnUiThread(new Runnable() {
            public void run() {
                paintMill(mill);
                signalUIupdate();
            }
        });
    }

    private void paintMill(final Position[] mill){
        for (int i = 0; i < 3; i++) {
            millSectors[i] = createSector(Options.Color.RED, mill[i].getX(), mill[i].getY());
            fieldLayout.addView(millSectors[i]);
        }
    }
    
    void unpaintMillOnUIThread() throws InterruptedException{
        c.runOnUiThread(new Runnable() {
            public void run() {
                unpaintMill();
                signalUIupdate();
            }
        });
    }

    private void unpaintMill() {
        fieldLayout.removeView(millSectors[0]);
        fieldLayout.removeView(millSectors[1]);
        fieldLayout.removeView(millSectors[2]);
    }

    public String toString() {
        String print = "";
        print += "    0 1 2 3 4 5 6\n------------------\n";
        for (int y = 0; y < fieldView.length; y++) {
            print += y + " | ";
            for (int x = 0; x < fieldView[y].length; x++) {
                print += getPos(new Position(x, y)) !=null ? "N " : "  ";
            }
            print += '\n';
        }
        return print;

    }

}
