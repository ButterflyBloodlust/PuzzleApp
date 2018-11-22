package com.hal9000.puzzleapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.abs;

public class ViewPictureActivity extends AppCompatActivity {

    private static final String KEY_PICTURES = "pics";
    private static final String KEY_REQUESTED_POS = "requestedPos";
    private static final int piecesNumber = 9;
    private static final int rows = 3;
    private static final int cols = 3;
    private static final int scatterRows = 2;

    private ArrayList<Integer> imageIDs;
    private int currentPosition = -1;
    private ImageView galleryPicture;
    private int screenWidth;
    ArrayList<PuzzlePiece> pieces;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_picture);

        initToolbar();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                getSupportActionBar().hide();
            }
        }

        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;

        imageIDs = (ArrayList<Integer>) getIntent().getSerializableExtra(KEY_PICTURES);
        currentPosition = getIntent().getIntExtra(KEY_REQUESTED_POS, 0);

        pieces = splitPicture();
        scatterPieces();
    }

    private void initToolbar() {
        Toolbar displayActToolbar = findViewById(R.id.simple_toolbar);
        setSupportActionBar(displayActToolbar);
    }

    private void changePicture(int arrayPosition){
        //galleryPicture.setLayoutParams(new LinearLayout.LayoutParams(500,500));
        //galleryPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
        galleryPicture.setImageResource(imageIDs.get(arrayPosition));
    }

    private ArrayList<PuzzlePiece> splitPicture() {

        Bitmap picture = BitmapFactory.decodeResource(getResources(), imageIDs.get(currentPosition));
        int imgWidth = (int)(screenWidth*0.8 + 0.5d);
        int imgHeight = imgWidth;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(picture, imgWidth, imgHeight, true); // casting double to int cuts decimal value, either round or use +0.5d

        ArrayList<PuzzlePiece> pieces = new ArrayList<>(piecesNumber);

        // Calculate the with and height of the pieces
        int pieceWidth = scaledBitmap.getWidth()/cols;
        int pieceHeight = scaledBitmap.getHeight()/rows;

        // Create each bitmap piece and add it to the resulting array
        for (int row = 0, yCoord = 0; row < rows; row++, yCoord += pieceHeight) {
            for (int col = 0, xCoord = 0; col < cols; col++, xCoord += pieceWidth) {
                PuzzlePiece piece = new PuzzlePiece(getApplicationContext());
                piece.setImageBitmap(Bitmap.createBitmap(scaledBitmap, xCoord, yCoord, pieceWidth, pieceHeight));
                piece.xCoord = xCoord;
                piece.yCoord = yCoord;
                piece.pieceWidth = pieceWidth;
                piece.pieceHeight = pieceHeight;
                pieces.add(piece);
            }
        }

        initPuzzleImageBordersAndContainer(imgWidth, imgHeight);
        return pieces;
    }

    private void scatterPieces() {
        final ImageView bottomBorder = (ImageView) findViewById(R.id.bottomBorder);
        ViewTreeObserver vto = bottomBorder.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new CustomOnGlobalLayoutListener(pieces) {
            @Override
            public void onGlobalLayout() {
                ArrayList<PuzzlePiece> pieces = arrayList;
                bottomBorder.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int[] outLocationBottomBorder = new int[2];
                bottomBorder.getLocationOnScreen(outLocationBottomBorder);  // get location of bottom border of puzzle image container

                int[] outLocationPuzzlePiecesContainer = new int[2];
                findViewById(R.id.puzzle_container_layout).getLocationOnScreen(outLocationPuzzlePiecesContainer);
/*
                Log.d("ViewPictureActivity", "outLocationBottomBorder[0] = " + outLocationBottomBorder[0]);
                Log.d("ViewPictureActivity", "outLocationBottomBorder[1] = " + outLocationBottomBorder[1]);
                Log.d("ViewPictureActivity", "outLocationPuzzlePiecesContainer[0] = " + outLocationPuzzlePiecesContainer[0]);
                Log.d("ViewPictureActivity", "outLocationPuzzlePiecesContainer[1] = " + outLocationPuzzlePiecesContainer[1]);
*/
                RelativeLayout layout = findViewById(R.id.puzzle_container_layout);
                int scatterCols = pieces.size() / scatterRows;  // rounded down, account for possible leftovers later
                Collections.shuffle(pieces);

                int bottomBorderEndYRelative = outLocationBottomBorder[1] - outLocationPuzzlePiecesContainer[1]
                        + bottomBorder.getHeight();
                int distanceBottomBorderToBottom = layout.getHeight() - bottomBorderEndYRelative;

                int addedPieces = 0;
                for (int y = 0, y_coord = bottomBorderEndYRelative; y < scatterRows && addedPieces < pieces.size(); y++,
                        y_coord += distanceBottomBorderToBottom / scatterRows) {    // scatter and add pieces to layout
                    PuzzlePiece piece = pieces.get(addedPieces);

                    int topMargin = (y_coord + piece.pieceHeight < layout.getHeight() ?
                            y_coord : layout.getHeight() - piece.pieceHeight);

                    for (int x = 0, x_coord = 0; x < scatterCols && addedPieces < pieces.size(); x++, x_coord += layout.getWidth()/scatterCols) {
                        piece = pieces.get(addedPieces++);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(piece.pieceWidth, piece.pieceHeight);

                        layoutParams.leftMargin = (x_coord + piece.pieceWidth < layout.getWidth() ?
                                x_coord : layout.getWidth() - piece.pieceWidth) - (int)((Math.random()-0.5)*piece.pieceHeight*0.15);

                        layoutParams.topMargin = topMargin - (int)((Math.random())*piece.pieceHeight*0.15);

                        piece.setOnTouchListener(new ImagePieceTouchListener());
                        layout.addView(piece, layoutParams);
                    }
                }

                for (int i = 0; addedPieces < pieces.size(); i++, addedPieces++) {    // handle leftover pieces
                    PuzzlePiece piece = pieces.get(addedPieces++);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(piece.pieceWidth, piece.pieceHeight);

                    layoutParams.leftMargin = (int)(piece.pieceWidth * (i + 0.5) - ((Math.random()-0.5)*piece.pieceHeight*0.3));

                    layoutParams.topMargin = (int)((bottomBorderEndYRelative + piece.pieceWidth * 1.5 < layout.getHeight() ?
                            bottomBorderEndYRelative + piece.pieceWidth * 0.5 :
                            layout.getHeight() - piece.pieceHeight) - ((Math.random()-0.5)*piece.pieceHeight*0.3));

                    piece.setOnTouchListener(new ImagePieceTouchListener());
                    layout.addView(piece, layoutParams);
                }

            }
        });
    }

    private void initPuzzleImageBordersAndContainer(int imgWidth, int imgHeight) {
        Bitmap picture;
        Bitmap scaledBitmap;
        ImageView iv = findViewById(R.id.galleryImage);
        iv.getLayoutParams().height = imgHeight;
        iv.getLayoutParams().width = imgWidth;

        int frameThickness = (int)(screenWidth*0.05 + 0.5d);

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
