package com.hal9000.puzzleapp.ui.viewpictureactivity;

import android.arch.lifecycle.ViewModel;

import com.hal9000.puzzleapp.PuzzlePiece;

import java.util.ArrayList;

public class ViewPictureActivityViewModel extends ViewModel {

    private ArrayList<Boolean> piecesSolved;
    private int imageId;

    public boolean setImagePiecesSolved(int imageId, ArrayList<PuzzlePiece> pieces) {
        if (imageId != this.imageId) {
            this.imageId = imageId;
            piecesSolved = new ArrayList<>(pieces.size());
            for (PuzzlePiece piece : pieces) {
                piecesSolved.add(!piece.canMove);
            }
            return true;
        }
        return false;
    }

    public boolean setPieceSolved(int pos) {
        if (pos <= piecesSolved.size() && pos >= 0) {
            piecesSolved.set(pos, true);
            return true;
        }
        else
            return false;
    }

    public boolean isPieceSolved(int pos) {
        if (pos <= piecesSolved.size() && pos >= 0) {
            return piecesSolved.get(pos);
        }
        else
            return false;
    }
}
