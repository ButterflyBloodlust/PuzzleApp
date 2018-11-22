package com.hal9000.puzzleapp;

import android.view.ViewTreeObserver;

import java.util.ArrayList;

public abstract class CustomOnGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {

    ArrayList<PuzzlePiece> arrayList;

    public CustomOnGlobalLayoutListener(ArrayList<PuzzlePiece> arrayList) {
        this.arrayList = arrayList;
    }
}
