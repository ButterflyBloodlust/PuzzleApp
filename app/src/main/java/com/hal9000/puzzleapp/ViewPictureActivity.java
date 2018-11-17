package com.hal9000.puzzleapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class ViewPictureActivity extends AppCompatActivity {

    private static final String KEY_PICTURES = "pics";
    private static final String KEY_REQUESTED_POS = "requestedPos";
    private ArrayList<Integer> imageIDs;
    private int currentPosition = -1;
    private ImageView galleryPicture;
    private int screenWidth;

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

        //galleryPicture = (ImageView)findViewById(R.id.galleryImage);

        imageIDs = (ArrayList<Integer>) getIntent().getSerializableExtra(KEY_PICTURES);
        currentPosition = getIntent().getIntExtra(KEY_REQUESTED_POS, 0);

        //changePicture(currentPosition);
        splitPicture();
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
        int piecesNumber = 9;
        int rows = 3;
        int cols = 3;

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

        RelativeLayout layout = findViewById(R.id.puzzle_container_layout);
        //ImagePieceTouchListener touchListener = new ImagePieceTouchListener();
        for(PuzzlePiece piece : pieces) {
            piece.setOnTouchListener(new ImagePieceTouchListener());
            layout.addView(piece);
        }

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

        //TODO: Scatter puzzle pieces

        return pieces;
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
