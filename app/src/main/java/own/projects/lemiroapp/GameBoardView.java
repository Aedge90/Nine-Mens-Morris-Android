package own.projects.lemiroapp;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class GameBoardView {

    private volatile boolean uiupdated = false;
    private Lock lock;
    private Condition uiupdate;
    private ImageView[][] fieldView;
    private GameModeActivity c; 
    private GridLayout fieldLayout;
    
    GameBoardView(GameModeActivity c , GridLayout fieldLayout) {
        this.c = c;
        this.fieldLayout = fieldLayout;

        fieldView = new ImageView[GameBoard.LENGTH][GameBoard.LENGTH];
        
        uiupdated = false;
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

        // set witdh not to screenwidth so the gridlayout size matches its content size
        // this way the background image of the gridview will always be aligned independent of screen resolution
        ((LinearLayout) fieldLayout.getParent()).updateViewLayout(fieldLayout,
                new LinearLayout.LayoutParams(c.screenWidth - remainingPixels, c.screenWidth - remainingPixels));
    }
    
    public ImageView getPos(Position pos){
        return fieldView[pos.getY()][pos.getX()]; 
    }
    
    public void setPos(final Position pos, final Options.Color color) throws InterruptedException{
        
        c.runOnUiThread(new Runnable() {
            public void run() {
                ImageView sector = createSector(color, pos.getX(), pos.getY());
                fieldLayout.removeView(fieldView[pos.getY()][pos.getX()]);
                fieldLayout.addView(sector);
                fieldView[pos.getY()][pos.getX()] = sector;
                
                signalUIupdate();
            }
        });
        //continues when fieldView has updated selection on screen
        waitforUIupdate();

    }
    
    private void waitforUIupdate() throws InterruptedException{
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
    
    public void makeMove(final Move move, final Options.Color color) throws InterruptedException{
        
        c.runOnUiThread(new Runnable() {
            public void run() {
        
                final ImageView srcSector = fieldView[move.getSrc().getY()][move.getSrc().getX()];
                final ImageView destSector = fieldView[move.getDest().getY()][move.getDest().getX()];
                
                final ImageView newSrcSector = createSector(Options.Color.NOTHING, move.getSrc().getX(), move.getSrc().getY());
                final ImageView newDestSector = createSector(color, move.getDest().getX(), move.getDest().getY());
                final ImageView animSector = createSector(color, move.getSrc().getX(), move.getSrc().getY());;
        
                fieldLayout.addView(animSector);
                
                ObjectAnimator oleft = ObjectAnimator.ofInt(animSector, "left", srcSector.getLeft(), destSector.getLeft());
                ObjectAnimator otop = ObjectAnimator.ofInt(animSector, "top", srcSector.getTop(), destSector.getTop());
                ObjectAnimator oright = ObjectAnimator.ofInt(animSector, "right", srcSector.getRight(), destSector.getRight());
                ObjectAnimator obottom = ObjectAnimator.ofInt(animSector, "bottom", srcSector.getBottom(), destSector.getBottom());
                AnimatorListener listen = new AnimatorListener(){
        
                    @Override
                    public void onAnimationCancel(Animator arg0) {}
        
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fieldLayout.removeView(animSector);
                        fieldLayout.addView(newDestSector);
                        fieldLayout.addView(newSrcSector);
                        signalUIupdate();
                    }
        
                    @Override
                    public void onAnimationRepeat(Animator animation) {}
        
                    @Override
                    public void onAnimationStart(Animator animation) {}
                    
                };
                oleft.setDuration(1000);
                oleft.start();
                oright.setDuration(1000);
                oright.start();
                otop.setDuration(1000);
                otop.start();
                obottom.setDuration(1000);
                obottom.start();
                obottom.addListener(listen);
                
                fieldLayout.removeView(srcSector);
                fieldLayout.removeView(destSector);
                
                fieldView[move.getSrc().getY()][move.getSrc().getX()] = newSrcSector;
                fieldView[move.getDest().getY()][move.getDest().getX()] = newDestSector;
            }
        });
        
        //continues when fieldView has played animation
        waitforUIupdate();

    }
    
    protected ImageView createSector(Options.Color color, int x, int y) {

        ImageView sector = new ImageView(c);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(y), GridLayout.spec(x));
        params.width = c.screenWidth / GameBoard.LENGTH;
        params.height = c.screenWidth / GameBoard.LENGTH;
        sector.setLayoutParams(params);
        sector.setAdjustViewBounds(false);

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
        bmp = Bitmap.createScaledBitmap(bmp, (int)(params.width * scaleFactor), (int)(params.width * scaleFactor), true);
        sector.setImageBitmap(bmp);
        sector.setScaleType(ImageView.ScaleType.CENTER);
        return sector;
    }
    
    void paintMill(final Position[] mill, final ImageView[] millSectors) throws InterruptedException{
        
        c.runOnUiThread(new Runnable() {
            public void run() {
                for (int i = 0; i < 3; i++) {
                    millSectors[i] = createSector(Options.Color.RED, mill[i].getX(), mill[i].getY());
                    fieldLayout.addView(millSectors[i]);
                }
                signalUIupdate();
            }
        });
        //continues when fieldView has painted the mill
        waitforUIupdate();
    }
    
    void unpaintMill(final ImageView[] millSectors) throws InterruptedException{
        c.runOnUiThread(new Runnable() {
            public void run() {
                fieldLayout.removeView(millSectors[0]);
                fieldLayout.removeView(millSectors[1]);
                fieldLayout.removeView(millSectors[2]);
                signalUIupdate();
            }
        });
        //continues when fieldView has unpainted the mill
        waitforUIupdate();
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
