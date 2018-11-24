package com.hal9000.puzzleapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;

public class ViewPictureActivity extends AppCompatActivity {

    private static final String KEY_PICTURES = "pics";
    private static final String KEY_REQUESTED_POS = "requestedPos";
    private static final int PIECES_NUMBER = 9;
    private static final int ROWS = 3;
    private static final int COLS = 3;
    private static final int SCATTER_ROWS_PORTRAIT = 2;  // this could technically be calculated based on views' parameters and number of pieces
    private static final int SCATTER_COLS_LANDSCAPE = 3;    // needs to be adjusted when changing PIECES_NUMBER

    private ArrayList<Integer> imageIDs;    // in case of adding an option later on to switch puzzle image in without leaving activity
    private int currentPosition = -1;
    private int screenWidth;
    private int screenHeight;
    ArrayList<PuzzlePiece> pieces;
    boolean globalLayoutListenerCalled = false;
    /** Puzzle image params **/
    private int imgWidth;
    private int imgHeight;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_picture);

        initToolbar();

        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        imageIDs = (ArrayList<Integer>) getIntent().getSerializableExtra(KEY_PICTURES);
        currentPosition = getIntent().getIntExtra(KEY_REQUESTED_POS, 0);

        handleUIElements();
    }

    public void handleUIElements() {
        final ImageView bottomBorder = (ImageView) findViewById(R.id.bottomBorder);
        ViewTreeObserver vto = findViewById(R.id.puzzle_container_layout).getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new CustomOnGlobalLayoutListener(pieces) {
            /** inflating puzzle image container seems to update views' position parameters asynchronously.
             *  scatterPieces() uses those params, so it needs to be delayed;
             *  globalLayoutListenerCalled prevents execution of code multiple times in time between updating position params and scatterPieces()
             */
            @Override
            public void onGlobalLayout() {
                if(findViewById(R.id.galleryImage).getHeight() != 0 && globalLayoutListenerCalled)
                {
                    bottomBorder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    scatterPieces();
                }
                else if(!globalLayoutListenerCalled){
                    globalLayoutListenerCalled = true;
                    setPuzzleImageParams();
                    initPuzzleImageBordersAndContainer();
                    pieces = splitPicture();
                }
            }
        });
    }

    private void initToolbar() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toolbar displayActToolbar = findViewById(R.id.simple_toolbar);
            setSupportActionBar(displayActToolbar);
        }
    }
/*
    private void changePicture(int arrayPosition){
        //galleryPicture.setLayoutParams(new LinearLayout.LayoutParams(500,500));
        //galleryPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
        galleryPicture.setImageResource(imageIDs.get(arrayPosition));
    }
*/
    private void setPuzzleImageParams() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imgHeight = (int) (findViewById(R.id.puzzle_container_layout).getHeight() * 0.85 + 0.5d);
            imgWidth = imgHeight;
        }
        else {
            imgWidth = (int) (screenWidth * 0.8 + 0.5d);
            imgHeight = imgWidth;
        }
    }

    private ArrayList<PuzzlePiece> splitPicture() {

        Bitmap picture = BitmapFactory.decodeResource(getResources(), imageIDs.get(currentPosition));

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(picture, imgWidth, imgHeight, true); // casting double to int cuts decimal value, either round or use +0.5d

        ArrayList<PuzzlePiece> pieces = new ArrayList<>(PIECES_NUMBER);

        // Calculate the with and height of the pieces
        int pieceWidth = scaledBitmap.getWidth()/ COLS;
        int pieceHeight = scaledBitmap.getHeight()/ ROWS;

        // Create each bitmap piece and add it to the resulting array
        for (int row = 0, yCoord = 0; row < ROWS; row++, yCoord += pieceHeight) {
            for (int col = 0, xCoord = 0; col < COLS; col++, xCoord += pieceWidth) {
                PuzzlePiece piece = new PuzzlePiece(getApplicationContext());
                piece.setImageBitmap(Bitmap.createBitmap(scaledBitmap, xCoord, yCoord, pieceWidth, pieceHeight));
                piece.xCoord = xCoord;
                piece.yCoord = yCoord;
                piece.pieceWidth = pieceWidth;
                piece.pieceHeight = pieceHeight;
                pieces.add(piece);
            }
        }
        return pieces;
    }

    private void scatterPieces() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            int[] outLocationRightBorder = new int[2];
            findViewById(R.id.rightBorder).getLocationOnScreen(outLocationRightBorder);  // get location of bottom border of puzzle image container

            int[] outLocationPuzzlePiecesContainer = new int[2];
            findViewById(R.id.puzzle_container_layout).getLocationOnScreen(outLocationPuzzlePiecesContainer);

            RelativeLayout layout = findViewById(R.id.puzzle_container_layout);
            int scatterRows = pieces.size() / SCATTER_COLS_LANDSCAPE;  // rounded down, account for possible leftovers later
            Collections.shuffle(pieces);

            int rightBorderEndXRelative = outLocationRightBorder[0] - outLocationPuzzlePiecesContainer[0]
                    + findViewById(R.id.rightBorder).getWidth();
            int distanceRightBorderToRight = layout.getWidth() - rightBorderEndXRelative;

            int addedPieces = 0;
            for (int x = 0, x_coord = rightBorderEndXRelative; x < SCATTER_COLS_LANDSCAPE && addedPieces < pieces.size(); x++,
                    x_coord += distanceRightBorderToRight / SCATTER_COLS_LANDSCAPE) {    // scatter and add pieces to layout
                PuzzlePiece piece = pieces.get(addedPieces);

                int leftMargin = (x_coord + piece.pieceWidth < layout.getWidth() ?
                        x_coord : layout.getWidth() - piece.pieceWidth);

                for (int y = 0, y_coord = 0; y < scatterRows && addedPieces < pieces.size(); y++, y_coord += layout.getHeight() / scatterRows) {
                    piece = pieces.get(addedPieces++);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(piece.pieceWidth, piece.pieceHeight);

                    int randomOffset = (int) ((Math.random()) * piece.pieceWidth * 0.15);
                    layoutParams.topMargin = y_coord + piece.pieceHeight < layout.getHeight() ?
                            y_coord + randomOffset : layout.getHeight() - piece.pieceHeight - randomOffset;

                    layoutParams.leftMargin = leftMargin - (int) ((Math.random() - 0.5) * piece.pieceWidth * 0.15);

                    piece.setOnTouchListener(new ImagePieceTouchListener(getApplicationContext()));
                    layout.addView(piece, layoutParams);
                }
            }

            for (int i = 0; addedPieces < pieces.size(); i++, addedPieces++) {    // handle leftover pieces
                PuzzlePiece piece = pieces.get(addedPieces++);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(piece.pieceWidth, piece.pieceHeight);

                layoutParams.topMargin = (int) (piece.pieceHeight * (i + 0.5) - ((Math.random() - 0.5) * piece.pieceHeight * 0.3));

                layoutParams.leftMargin = (int) ((rightBorderEndXRelative + piece.pieceWidth * 1.5 < layout.getWidth() ?
                        rightBorderEndXRelative + piece.pieceWidth * 0.5 :
                        layout.getWidth() - piece.pieceWidth) - ((Math.random() - 0.5) * piece.pieceHeight * 0.3));

                piece.setOnTouchListener(new ImagePieceTouchListener(getApplicationContext()));
                layout.addView(piece, layoutParams);
            }
        }
        else {
            int[] outLocationBottomBorder = new int[2];
            findViewById(R.id.bottomBorder).getLocationOnScreen(outLocationBottomBorder);  // get location of bottom border of puzzle image container

            int[] outLocationPuzzlePiecesContainer = new int[2];
            findViewById(R.id.puzzle_container_layout).getLocationOnScreen(outLocationPuzzlePiecesContainer);
/*
        Log.d("ViewPictureActivity", "outLocationBottomBorder[0] = " + outLocationBottomBorder[0]);
        Log.d("ViewPictureActivity", "outLocationBottomBorder[1] = " + outLocationBottomBorder[1]);
        Log.d("ViewPictureActivity", "outLocationPuzzlePiecesContainer[0] = " + outLocationPuzzlePiecesContainer[0]);
        Log.d("ViewPictureActivity", "outLocationPuzzlePiecesContainer[1] = " + outLocationPuzzlePiecesContainer[1]);
*/
            RelativeLayout layout = findViewById(R.id.puzzle_container_layout);
            int scatterCols = pieces.size() / SCATTER_ROWS_PORTRAIT;  // rounded down, account for possible leftovers later
            Collections.shuffle(pieces);

            int bottomBorderEndYRelative = outLocationBottomBorder[1] - outLocationPuzzlePiecesContainer[1]
                    + findViewById(R.id.bottomBorder).getHeight();
            int distanceBottomBorderToBottom = layout.getHeight() - bottomBorderEndYRelative;
/*
        Log.d("ViewPictureActivity", "outLocationBottomBorder[1] = " + outLocationBottomBorder[1]);
        Log.d("ViewPictureActivity", "bottomBorderEndYRelative = " + bottomBorderEndYRelative);
        Log.d("ViewPictureActivity", "distanceBottomBorderToBottom = " + distanceBottomBorderToBottom);
*/
            int addedPieces = 0;
            for (int y = 0, y_coord = bottomBorderEndYRelative; y < SCATTER_ROWS_PORTRAIT && addedPieces < pieces.size(); y++,
                    y_coord += distanceBottomBorderToBottom / SCATTER_ROWS_PORTRAIT) {    // scatter and add pieces to layout
                PuzzlePiece piece = pieces.get(addedPieces);

                int topMargin = (y_coord + piece.pieceHeight < layout.getHeight() ?
                        y_coord : layout.getHeight() - piece.pieceHeight);

                for (int x = 0, x_coord = 0; x < scatterCols && addedPieces < pieces.size(); x++, x_coord += layout.getWidth() / scatterCols) {
                    piece = pieces.get(addedPieces++);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(piece.pieceWidth, piece.pieceHeight);

                    layoutParams.leftMargin = (x_coord + piece.pieceWidth < layout.getWidth() ?
                            x_coord : layout.getWidth() - piece.pieceWidth) - (int) ((Math.random() - 0.5) * piece.pieceHeight * 0.15);

                    layoutParams.topMargin = topMargin - (int) ((Math.random()) * piece.pieceHeight * 0.15);

                    piece.setOnTouchListener(new ImagePieceTouchListener(getApplicationContext()));
                    layout.addView(piece, layoutParams);
                }
            }

            for (int i = 0; addedPieces < pieces.size(); i++, addedPieces++) {    // handle leftover pieces
                PuzzlePiece piece = pieces.get(addedPieces++);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(piece.pieceWidth, piece.pieceHeight);

                layoutParams.leftMargin = (int) (piece.pieceWidth * (i + 0.5) - ((Math.random() - 0.5) * piece.pieceHeight * 0.3));

                layoutParams.topMargin = (int) ((bottomBorderEndYRelative + piece.pieceWidth * 1.5 < layout.getHeight() ?
                        bottomBorderEndYRelative + piece.pieceWidth * 0.5 :
                        layout.getHeight() - piece.pieceHeight) - ((Math.random() - 0.5) * piece.pieceHeight * 0.3));

                piece.setOnTouchListener(new ImagePieceTouchListener(getApplicationContext()));
                layout.addView(piece, layoutParams);
            }
        }
    }

    private void initPuzzleImageBordersAndContainer() {
        Bitmap picture;
        Bitmap scaledBitmap;
        ImageView iv = findViewById(R.id.galleryImage);
        iv.getLayoutParams().height = imgHeight;
        iv.getLayoutParams().width = imgWidth;
        int frameThickness;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            frameThickness = (int)(screenHeight*0.05 + 0.5d);
        }
        else {
            frameThickness = (int)(screenWidth*0.05 + 0.5d);
        }


        iv = findViewById(R.id.rightBorder);
        picture = BitmapFactory.decodeResource(getResources(), R.drawable.right_border);
        scaledBitmap = Bitmap.createScaledBitmap(picture, frameThickness, imgHeight, true);
        iv.setImageBitmap(scaledBitmap);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        iv = findViewById(R.id.bottomBorder);
        scaledBitmap = Bitmap.createScaledBitmap(picture, frameThickness, imgWidth + frameThickness*2, true);
        iv.setImageBitmap(Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true));

        matrix.postRotate(90);
        iv = findViewById(R.id.leftBorder);
        scaledBitmap = Bitmap.createScaledBitmap(picture, frameThickness, imgHeight, true);
        iv.setImageBitmap(Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true));

        matrix.postRotate(90);
        iv = findViewById(R.id.topBorder);
        scaledBitmap = Bitmap.createScaledBitmap(picture, frameThickness, imgWidth + frameThickness*2, true);
        iv.setImageBitmap(Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true));
    }

    // starter - to control passing and reading Extra in the same class
    public static void start(Context context, ArrayList<Integer> imageIDs, int requestedPos) {
        Intent starter = new Intent(context, ViewPictureActivity.class);
        starter.putExtra(KEY_PICTURES, imageIDs);
        starter.putExtra(KEY_REQUESTED_POS, requestedPos);
        context.startActivity(starter);
        //Log.d("ViewPictureActivity", "after start");
    }
}
