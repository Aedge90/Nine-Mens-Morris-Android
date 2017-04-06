package com.github.aedge90.nmm;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapManager {

    private GameModeActivity c;

    private final Bitmap blackPieceBitmap;
    private final Bitmap whitePieceBitmap;
    private final Bitmap greenPieceBitmap;
    private final Bitmap redPieceBitmap;

    public BitmapManager(GameModeActivity c) {

        this.c = c;

        int size = c.screenWidth / GameBoard.LENGTH;
        float scaleFactor = 0.95f;
        blackPieceBitmap = createScaledBitmap(R.drawable.piece_black, (int) (size * scaleFactor));
        whitePieceBitmap = createScaledBitmap(R.drawable.piece_white, (int) (size * scaleFactor));
        greenPieceBitmap = createScaledBitmap(R.drawable.piece_green, (int) (size * scaleFactor));
        redPieceBitmap = createScaledBitmap(R.drawable.piece_red, (int) (size * scaleFactor));
    }

    private Bitmap createScaledBitmap(int resId, int size){
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(c.getResources(), resId), size, size, true);
    }

    public Bitmap getBitmap(Options.Color color){
        if (color.equals(Options.Color.BLACK)) {
            return blackPieceBitmap;
        } else if (color.equals(Options.Color.WHITE)) {
            return whitePieceBitmap;
        } else if (color.equals(Options.Color.RED)) {
            return redPieceBitmap;
        }else if (color.equals(Options.Color.GREEN)) {
            return greenPieceBitmap;
        }
        return null;
    }
}

