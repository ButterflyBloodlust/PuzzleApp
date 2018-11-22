package com.hal9000.puzzleapp;

import android.content.Context;

public class PuzzlePiece extends android.support.v7.widget.AppCompatImageView {

    public int xCoord;  // relative to full image (before chopping)
    public int yCoord;  // relative to full image (before chopping)
    public int pieceWidth;
    public int pieceHeight;
    public boolean canMove = true;

    public PuzzlePiece(Context context) {
        super(context);
    }
}