package com.hal9000.puzzleapp;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;

public class GalleryScrollListener implements AbsListView.OnScrollListener {
    /** Scroll listener that aligns GridView contents to the top depending on how much of the topmost element on screen is visible
     *  (less than half => animate to next row, more than half => animate align top of the top row to the top of the screen).
     *  If you don't want animation remove ObjectAnimator and simply scroll GridView using smoothScrollToPosition() or setSelection().
     * **/

    private static final int SCROLL_ANIMATION_DUR = 200;
    private int currentScrollState;
    private int currentFirstVisibleItem;
    private int currentVisibleItemCount;
    private int totalItemCount;
    private boolean draggedFlag = false;
    private GridView gridView;
    private ObjectAnimator animateScroll = null;
    private GalleryAnimatorListener galleryAnimatorListener;

    public GalleryScrollListener(GridView gridView){
        this.gridView = gridView;
        galleryAnimatorListener = new GalleryAnimatorListener();

    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.currentFirstVisibleItem = firstVisibleItem;
        this.currentVisibleItemCount = visibleItemCount;
        this.totalItemCount = totalItemCount;
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.currentScrollState = scrollState;
        this.isScrollCompleted();
    }

    private void isScrollCompleted() {

        if (this.currentScrollState == SCROLL_STATE_DRAGGING) { // when dragged by outside input such as user touch input
            if (animateScroll != null){
                animateScroll.cancel();
                gridView.clearAnimation();
            }
            //Log.d("scroll", "isScrollCompleted() DRAGGING");
            draggedFlag = true;
        }
        else if (this.currentScrollState == SCROLL_STATE_IDLE && this.currentVisibleItemCount > 0) {
            //Log.d("scroll", "isScrollCompleted() IDLE");

            if(draggedFlag) {
                draggedFlag = false;
                // determine how much of the first visible image is cut
                View firstVisiblePic = gridView.getChildAt(0);

                final int firstPicGetBottom = firstVisiblePic.getBottom();   // always >0
                final int firstPicGetTop = firstVisiblePic.getTop(); // <0 except for transition between images (so while on margins)
                int scrollAnimationValue = 0;

                View lastVisiblePic = gridView.getChildAt(currentVisibleItemCount -1);
                final int lastPicGetBottom = lastVisiblePic.getBottom();   // always >0
/*
                Log.d("scroll", "topImageSizeLeft [.getBottom()] = " + Integer.toString(firstPicGetBottom));
                Log.d("scroll", "topImageSizeOurOfView [.getTop()] *(-1) = " + Integer.toString(firstPicGetTop*(-1)));

                Log.d("scroll", "bottomImageSizeLeft [.getTop()] = " + Integer.toString(gridView.getChildAt(currentVisibleItemCount -1).getTop()));
                Log.d("scroll", "bottomImageSizeOurOfView [.getBottom()] = " + Integer.toString(gridView.getChildAt(currentVisibleItemCount -1).getBottom()));

                Log.d("scroll", "topGridView [.getTop()] = " + Integer.toString(gridView.getTop()));
                Log.d("scroll", "bottomGridView [.getBottom()] = " + Integer.toString(gridView.getBottom()));
                Log.d("scroll", "heightGridView [.getHeight()] = " + Integer.toString(gridView.getHeight()));
*/
                if ( firstPicGetBottom > firstPicGetTop*(-1) ) {    // scroll up to align to current row
                    if (lastPicGetBottom == gridView.getHeight() && gridView.getLastVisiblePosition() + 1 == totalItemCount ) {    // if last item is fully visible we don't want to scroll up to align
                        return;
                    }
                    else {
                        scrollAnimationValue = firstPicGetTop;
                        galleryAnimatorListener.setJumpToAfterFinish(currentFirstVisibleItem);
                    }
                }// if ( firstPicGetBottom > firstPicGetTop*(-1) )
                else {  // scroll down
                    final int itemCount = gridView.getAdapter().getCount();
                    final int columnsCount = gridView.getNumColumns();
                    final int jumpToPos =  itemCount > columnsCount + currentFirstVisibleItem ? columnsCount + currentFirstVisibleItem : itemCount - 1;   // next row

                    int distanceToNextRow = firstPicGetBottom + gridView.getVerticalSpacing();
                    if (gridView.getLastVisiblePosition() + 1 != totalItemCount ) { // scroll down to align to next row
                        scrollAnimationValue = distanceToNextRow;   // included spacing to animate to the point where set selection jumps
                    }
                    else{    // scroll down case when last row becomes visible
                        int distanceToEndOfGridView = lastPicGetBottom - gridView.getHeight();
                        scrollAnimationValue = distanceToEndOfGridView < distanceToNextRow ? distanceToEndOfGridView : distanceToNextRow;
                        //^ essentially: scroll accurately to the end (align last row to bottom of the screen) OR align to next row
                        //  '?' in case where scrolling to the end of grid view would scroll too much, resulting in hiding top row
                    }
                    galleryAnimatorListener.setJumpToAfterFinish(jumpToPos);
                }// else
                animateScroll = ObjectAnimator
                        .ofInt(gridView, "scrollY", scrollAnimationValue)
                        .setDuration(SCROLL_ANIMATION_DUR);
                animateScroll.addListener(galleryAnimatorListener);
                animateScroll.start();
            }// if(draggedFlag)
        }// else if (this.currentScrollState == SCROLL_STATE_IDLE && this.currentVisibleItemCount > 0)
    }// private void isScrollCompleted()
/*
    private void getTopRowVisibilityParams(){
        Log.d("scroll", "gridView.getBottom() = " + Integer.toString(gridView.getBottom()));
        Log.d("scroll", "gridView.getTop() = " + Integer.toString(gridView.getTop()));
        View v = gridView.getChildAt(0);
        Log.d("scroll", "v.getBottom() = " + Integer.toString(v.getBottom()));
        Log.d("scroll", "v.getTop() = " + Integer.toString(v.getTop()));
    }
*/
    private class GalleryAnimatorListener implements Animator.AnimatorListener {
        /** Used by ObjectAnimator to save actual animation finish scroll position.
         *  Without this ObjectAnimator does the scroll animation and stays at finish position
         *  but when user starts scrolling again, scroll position returns to pre-animation state.
         */

        private int jumpToAfterFinish = 0;
        private boolean cancelFlag = false;

        @Override
        public void onAnimationStart(Animator animation) { }
        @Override
        public void onAnimationCancel(Animator animation) {
            //Log.d("scroll", "animation CANCEL");
            cancelFlag = true;
        }
        @Override
        public void onAnimationRepeat(Animator animation) { }

        @Override
        public void onAnimationEnd(Animator animation) {
            /** Prevents returning to pre-animation scroll position when user starts scrolling after animation
             *  (i.e. "saves" animation position) **/
            if (!cancelFlag) {  // only do the following when animation wasn't canceled (e.g. because of user input)
                gridView.setScrollY(0); // return to default position (from before animation), setSelection() gets messed up without it for some reason
                gridView.setSelection(jumpToAfterFinish); // sets actual scroll position to top of picture (where animation ends)
            }
            else {
                cancelFlag = false;
            }
            //Log.d("scroll", "animation END");
        }

        private void setJumpToAfterFinish(int position){
            jumpToAfterFinish = position;
        }
    }
}
