package com.hal9000.puzzleapp;

import android.content.Context;
import android.content.res.Configuration;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import static java.lang.Math.*;

public class ImagePieceTouchListener implements View.OnTouchListener {

    private float xDelta;
    private float yDelta;
    private int viewWidth;
    private int viewHeight;
    private int parentViewHeight;
    private int parentViewWidth;
    private int startMarginLeft;
    private int startMarginTop;
    private RelativeLayout.LayoutParams lParams;
    private PuzzlePiece piece;
    PuzzlePieceAnimationListener puzzlePieceAnimationListener;
    private Context context;

    public ImagePieceTouchListener(Context context) {
        puzzlePieceAnimationListener = new PuzzlePieceAnimationListener();
        this.context = context;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        piece = (PuzzlePiece) view;
        if (!piece.canMove) {
            return true;    // exit if piece is already at correct position
        }

        // get raw touch coordinates (before they had been adjusted for the containing window and views)
        float x = motionEvent.getRawX();    // distance from left side of the screen
        float y = motionEvent.getRawY();    // distance from top of the screen
        lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();  // to get distance from edges of view to edges of parent layout (margins)

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                piece.bringToFront();
            // save initial position raw coords adjusted for distance or view to edge of their container view
                xDelta = x - lParams.leftMargin;    // coord X relative to top left corner of image
                yDelta = y - lParams.topMargin;
                startMarginLeft = lParams.leftMargin;
                startMarginTop = lParams.topMargin;

                parentViewHeight = ((View)view.getParent()).getHeight();    // used to prevent picture shrinking at bottom edge of parent view
                parentViewWidth = ((View)view.getParent()).getWidth();    // used to prevent picture shrinking at right edge of parent view

                viewWidth = view.getWidth();    // save original view width
                viewHeight = view.getHeight();    // save original view height

                break;

                case MotionEvent.ACTION_MOVE:
            // update view position within container view while compensating for rawness of x and y
                lParams.leftMargin = (int) (x - xDelta);    // because margins are calculated from edges of view (puzzle piece)
                if (lParams.leftMargin < 0) lParams.leftMargin = 0; // prevent moving picture past left edge
                lParams.topMargin = (int) (y - yDelta);
                if (lParams.topMargin < 0) lParams.topMargin = 0; // prevent moving picture past top edge

                // allow picture to move past the right edge
                // lParams.rightMargin = (int) (screenWidth - (x - xDelta - viewWidth));

                // prevent picture from shrinking when trying to move past right edge
                if (lParams.leftMargin + viewWidth > parentViewWidth)
                    lParams.leftMargin = parentViewWidth - viewWidth;

                // prevent picture from shrinking when trying to move past bottom edge
                if (lParams.topMargin + viewHeight > parentViewHeight)
                    lParams.topMargin = parentViewHeight - viewHeight;

                view.setLayoutParams(lParams);
                break;

            case MotionEvent.ACTION_UP:
            // after user selects position for puzzle piece, check it piece is at correct puzzle image position and either lock it there or animate to starting position
                // get absolute puzzle piece position
                int[] outLocationPiece = new int[2];
                view.getLocationOnScreen(outLocationPiece);

                // get absolute puzzle pieces' destination container position (where pieces are combined for final image)
                int[] outLocationImgContainer = new int[2];
                ((View)view.getParent().getParent()).findViewById(R.id.galleryImage).getLocationOnScreen(outLocationImgContainer);

                // get absolute puzzle pieces' container position
                int[] outLocationPuzzlePiecesContainer = new int[2];
                ((View)view.getParent()).getLocationOnScreen(outLocationPuzzlePiecesContainer);

                View galleryImageView = ((View)view.getParent().getParent()).findViewById(R.id.galleryImage);
                int puzzleImageContainerHeight = galleryImageView.getHeight();
                int puzzleImageContainerWidth = galleryImageView.getWidth();
                galleryImageView = null;    // in case this class persists after activity gets destroyed

                final double toleranceX = view.getWidth() * 0.2;
                final double toleranceY = view.getHeight() * 0.2;

                int xDiff = abs(piece.xCoord + outLocationImgContainer[0] - outLocationPiece[0]);
                int yDiff = abs(piece.yCoord + outLocationImgContainer[1] - outLocationPiece[1]);
                if (xDiff <= toleranceX && yDiff <= toleranceY) {
                    lParams.leftMargin = piece.xCoord + outLocationImgContainer[0] - outLocationPuzzlePiecesContainer[0];
                    lParams.topMargin = piece.yCoord + outLocationImgContainer[1] - outLocationPuzzlePiecesContainer[1];
                    piece.setLayoutParams(lParams);
                    piece.canMove = false;
                    sendViewToBack(piece);
                }
                else if ( (outLocationPiece[1] < outLocationImgContainer[1] + puzzleImageContainerHeight &&
                        context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ||
                        (outLocationPiece[0] < outLocationImgContainer[0] + puzzleImageContainerWidth &&
                        context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) ) {
                    // send to starting position if ended up on puzzleImageContainer
                    TranslateAnimation animation = new TranslateAnimation(0, startMarginLeft - lParams.leftMargin,
                            0, startMarginTop - lParams.topMargin);
                    animation.setDuration(500);
                    animation.setFillAfter(false);
                    animation.setAnimationListener(puzzlePieceAnimationListener);

                    piece.startAnimation(animation);
                }
                break;
        }
        return true;
    }

    private void sendViewToBack(final View view) {
        final ViewGroup parent = (ViewGroup)view.getParent();
        if (parent != null) {
            parent.removeView(view);
            parent.addView(view, 0);
        }
    }

    private class PuzzlePieceAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            piece.canMove = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) { }

        @Override
        public void onAnimationEnd(Animation animation) {
            piece.clearAnimation(); // prevents one-time image flash after reaching animation destination
            lParams.leftMargin = startMarginLeft;
            lParams.topMargin = startMarginTop;
            piece.setLayoutParams(lParams);
            piece.canMove = true;
            //Log.d("scroll", "animation END");
        }
    }
}

/*
                Log.d("ImagePieceTouchListener", "x = " + x);
                Log.d("ImagePieceTouchListener", "xDelta = " + xDelta);
                Log.d("ImagePieceTouchListener", "lParams.leftMargin = " + lParams.leftMargin);

                Log.d("ImagePieceTouchListener", "MotionEvent.ACTION_DOWN");
                Log.d("ImagePieceTouchListener", "view.getHeight() = " + view.getHeight());
                Log.d("ImagePieceTouchListener", "xDelta = " + xDelta);
                Log.d("ImagePieceTouchListener", "yDelta = " + yDelta);
                Log.d("ImagePieceTouchListener", "lParams.leftMargin = " + lParams.leftMargin);
                Log.d("ImagePieceTouchListener", "lParams.topMargin = " + lParams.topMargin);
                Log.d("ImagePieceTouchListener", "lParams.rightMargin = " + lParams.rightMargin);
                Log.d("ImagePieceTouchListener", "x = " + x);
                Log.d("ImagePieceTouchListener", "y = " + y);

                Log.d("ImagePieceTouchListener", "MotionEvent.ACTION_UP");
                Log.d("ImagePieceTouchListener", "piece location X = " + outLocationPiece[0]);
                Log.d("ImagePieceTouchListener", "piece location Y = " + outLocationPiece[1]);
                Log.d("ImagePieceTouchListener", "container location X = " + outLocationImgContainer[0]);
                Log.d("ImagePieceTouchListener", "container location Y = " + outLocationImgContainer[1]);
 */