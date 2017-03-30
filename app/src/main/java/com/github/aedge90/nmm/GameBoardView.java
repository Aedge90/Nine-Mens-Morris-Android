package com.github.aedge90.nmm;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import android.util.Log;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.view.Gravity;
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
    ImageView[] millSectors;
    
    GameBoardView(GameModeActivity c , GridLayout fieldLayout) {
        this.c = c;
        this.fieldLayout = fieldLayout;

        millSectors = new ImageView[3];

        fieldView = new ImageView[GameBoard.LENGTH][GameBoard.LENGTH];
        
        uiupdated = true;
        lock = new ReentrantLock();
        uiupdate = lock.newCondition();

        int remainingPixels = c.screenWidth % GameBoard.LENGTH;
        for (int y = 0; y < GameBoard.LENGTH; y++) {
            for (int x = 0; x < GameBoard.LENGTH; x++) {
                ImageView sector = createSector(Options.Color.NOTHING, x, y);
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


    public void setPosOnUIThread(final Position pos, final Options.Color color, final GameModeActivity.OnFieldClickListener posListener) throws InterruptedException{
        
        c.runOnUiThread(new Runnable() {
            public void run() {
                setPos(pos, color, posListener);
                signalUIupdate();
            }
        });

    }

    private void setPos(final Position pos, final Options.Color color, final GameModeActivity.OnFieldClickListener posListener){
        ImageView sector = createSector(color, pos.getX(), pos.getY());
        fieldLayout.removeView(fieldView[pos.getY()][pos.getX()]);
        fieldLayout.addView(sector);
        fieldView[pos.getY()][pos.getX()] = sector;
        sector.setOnClickListener(posListener);
    }

    protected void waitforUIupdate() throws InterruptedException{
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

    public void runMoveAnimation (ImageView animSector, ImageView destSector, AnimatorListener listen) {

        //while jumping this is important so the moving pieces does not move underneath the others
        animSector.bringToFront();

        ObjectAnimator oleft = ObjectAnimator.ofInt(animSector, "left", animSector.getLeft(), destSector.getLeft());
        ObjectAnimator otop = ObjectAnimator.ofInt(animSector, "top", animSector.getTop(), destSector.getTop());
        ObjectAnimator oright = ObjectAnimator.ofInt(animSector, "right", animSector.getRight(), destSector.getRight());
        ObjectAnimator obottom = ObjectAnimator.ofInt(animSector, "bottom",animSector.getBottom(),destSector.getBottom());

        oleft.setDuration(1000);
        oleft.start();
        oright.setDuration(1000);
        oright.start();
        otop.setDuration(1000);
        otop.start();
        obottom.setDuration(1000);
        obottom.start();
        obottom.addListener(listen);

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

                AnimatorListener listen = new AnimatorListener(){

                    @Override
                    public void onAnimationCancel(Animator arg0) {}

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        piecesSpaceLayout.removeView(animSector);
                        fieldLayout.addView(newDestSector);
                        signalUIupdate();
                        newDestSector.setOnClickListener(destListener);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {}

                    @Override
                    public void onAnimationStart(Animator animation) {}

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

                AnimatorListener listen = new AnimatorListener(){
        
                    @Override
                    public void onAnimationCancel(Animator arg0) {}
        
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fieldLayout.removeView(animSector);
                        fieldLayout.addView(newDestSector);
                        fieldLayout.addView(newSrcSector);
                        signalUIupdate();
                        newSrcSector.setOnClickListener(srcListener);
                        newDestSector.setOnClickListener(destListener);
                    }
        
                    @Override
                    public void onAnimationRepeat(Animator animation) {}
        
                    @Override
                    public void onAnimationStart(Animator animation) {}
                    
                };

                runMoveAnimation(animSector, destSector, listen);

                fieldLayout.removeView(destSector);
                
                fieldView[move.getSrc().getY()][move.getSrc().getX()] = newSrcSector;
                fieldView[move.getDest().getY()][move.getDest().getX()] = newDestSector;
            }
        });

    }
    
    protected ImageView createSector(Options.Color color, int x, int y) {

        ImageView sector = new ImageView(c);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(y), GridLayout.spec(x));
        params.width = c.screenWidth / GameBoard.LENGTH;
        params.height = c.screenWidth / GameBoard.LENGTH;
        sector.setLayoutParams(params);
        sector.setAdjustViewBounds(false);

        setBitMapForImageView(sector, color, params.width);

        return sector;
    }

    private ImageView createSectorInPiecesSpace(Options.Color color, int margin) {

        final ImageView sector = new ImageView(c);
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
        sector.setLayoutParams(params);
        sector.setAdjustViewBounds(false);

        setBitMapForImageView(sector, color, width);

        return sector;
    }

    private void setBitMapForImageView(ImageView imageView, Options.Color color, int size){
        Bitmap bmp = null;
        if (color.equals(Options.Color.BLACK)) {
            bmp = BitmapFactory.decodeResource(c.getResources(), R.drawable.piece_black);
        } else if (color.equals(Options.Color.WHITE)) {
            bmp = BitmapFactory.decodeResource(c.getResources(), R.drawable.piece_white);
        } else if (color.equals(Options.Color.RED)) {
            bmp = BitmapFactory.decodeResource(c.getResources(), R.drawable.piece_red);
        }else if (color.equals(Options.Color.GREEN)) {
            bmp = BitmapFactory.decodeResource(c.getResources(), R.drawable.piece_green);
        }else if (color.equals(Options.Color.NOTHING)){
            bmp = BitmapFactory.decodeResource(c.getResources(), R.drawable.nothing);
        }else{
            Log.d("MainActivity", "Error: createSector: Color not found!");
            c.finish();
        }
        float scaleFactor = 1.16f;    //used to fine tune the size of the pieces
        bmp = Bitmap.createScaledBitmap(bmp, (int)(size * scaleFactor), (int)(size * scaleFactor), true);
        imageView.setImageBitmap(bmp);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
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
