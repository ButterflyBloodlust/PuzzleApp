package com.hal9000.puzzleapp;

public class PuzzlePiece extends android.support.v7.widget.AppCompatImageView {

    public int ID; // starting position in puzzle pieces list
    public int xCoord;  // relative to full image (before chopping)
    public int yCoord;  // relative to full image (before chopping)
    public int pieceWidth;
    public int pieceHeight;
    public boolean canMove = true;
    private ViewPictureActivity context;

    public PuzzlePiece(ViewPictureActivity context) {
        super(context);
        this.context = context;
    }

    public void pieceSolved() { context.setPieceSolved(ID); }
}