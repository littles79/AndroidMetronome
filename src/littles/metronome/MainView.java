/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package littles.metronome;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.MotionEvent;
import android.graphics.Matrix;
import java.lang.Runnable;

/**
 * View that draws, takes keystrokes, etc. for a simple LunarLander game.
 *
 * Has a mode which RUNNING, PAUSED, etc. Has a x, y, dx, dy, ... capturing the
 * current ship physics. All x/y etc. are measured with (0,0) at the lower left.
 * updatePhysics() advances the physics based on realtime. draw() renders the
 * ship, and does an invalidate() to prompt another draw() as soon as possible
 * by the system.
 */
class MainView extends SurfaceView implements SurfaceHolder.Callback {
    class UpdateThread extends Thread {
    	
        public int mBPM;

        public boolean mPlay;
    	
    	public MainActivity mActivity;

        /**
         * Current height of the surface/canvas.
         *
         * @see #setSurfaceSize
         */
        private int mCanvasHeight = 1;

        /**
         * Current width of the surface/canvas.
         *
         * @see #setSurfaceSize
         */
        private int mCanvasWidth = 1;

        /** Message handler used by thread to interact with TextView */
        private Handler mHandler;

        /** Indicate whether the surface has been created & is ready to draw */
        private boolean mRun = false;

        /** Scratch rect object. */
        private RectF mScratchRect;

        /** Handle to the surface manager object we interact with */
        private SurfaceHolder mSurfaceHolder;
        
        private Paint mLinePaint;
        private Paint mBlackPaint;
        
        public UpdateThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;

            Resources res = context.getResources();

            // Initialize paints for speedometer
            mLinePaint = new Paint();
            mLinePaint.setAntiAlias(true);
            mLinePaint.setARGB(255, 0, 255, 0);

            mBlackPaint = new Paint();
            mBlackPaint.setAntiAlias(false);
            mBlackPaint.setARGB(255, 0, 0, 0);

            mScratchRect = new RectF(0, 0, 0, 0);
        }

        /**
         * Starts the game, setting parameters for the current difficulty.
         */
        public void doStart() {
            synchronized (mSurfaceHolder) {

            }
        }

        /**
         * Pauses the physics update & animation.
         */
        public void pause() {
            synchronized (mSurfaceHolder) {
            }
        }

        /**
         * Restores game state from the indicated Bundle. Typically called when
         * the Activity is being restored after having been previously
         * destroyed.
         *
         * @param savedState Bundle containing the game state
         */
        public synchronized void restoreState(Bundle savedState) {
            synchronized (mSurfaceHolder) {
            }
        }

        long nextTickTime;
        
        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    if( mBPM > 0 && mActivity != null && mActivity.mRenderPending == false )
                    {
                        c = mSurfaceHolder.lockCanvas(null);
                        synchronized (mSurfaceHolder) {
                            doDraw(c);
                        }

                        mActivity.mRenderPending = true;
                    	java.lang.Runnable r = new java.lang.Runnable() {

                    	    public void run()
                    	    {
        	                    final long currentTime = System.currentTimeMillis();

        	                    Matrix matrix=new Matrix();
        	                    mActivity.mImageView.setScaleType(ImageView.ScaleType.MATRIX);   //required

        	                    float degree = (float)(currentTime/1000.0d*(mBPM/60.0d)*3.14d % (java.lang.Math.PI*2));
        	                    degree = android.util.FloatMath.sin(degree) * 90.0f;
        	                    matrix.postRotate(degree, mActivity.mImageView.getWidth()/2, mActivity.mImageView.getHeight()/2);
        	                    mActivity.mImageView.setImageMatrix(matrix);
        	                    
        	                    if( currentTime >= nextTickTime )
        	                    {
        	                    	double tickInterval = 60.0d*1000.0d/mBPM;
        	                    	double nextTime = (double)currentTime + tickInterval;
        	                    	nextTime -= nextTime % tickInterval;
        	                    	nextTickTime = (long)nextTime;
    	                    		mActivity.mSoundPool.play( mActivity.mTickSound, 1.0f, 1.0f, 1, 0, 1.0f);
        	                    }
        	                    
        	                    mActivity.mRenderPending = false;
                    	    }
                    	};
                    	
                    	mActivity.runOnUiThread(r);
                    }
                    
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        /**
         * Dump game state to the provided Bundle. Typically called when the
         * Activity is being suspended.
         *
         * @return Bundle with this view's state
         */
        public Bundle saveState(Bundle map) {
            synchronized (mSurfaceHolder) {
                if (map != null) {
                }
            }
            return map;
        }

        /**
         * Sets the current difficulty.
         *
         * @param difficulty
         */
        public void setDifficulty(int difficulty) {
            synchronized (mSurfaceHolder) {
            }
        }

        /**
         * Sets if the engine is currently firing.
         */
        public void setFiring(boolean firing) {
            synchronized (mSurfaceHolder) {
            }
        }

        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         *
         * @param b true to run, false to shut down
         */
        public void setRunning(boolean b) {
            mRun = b;
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         *
         * @see #setState(int, CharSequence)
         * @param mode one of the STATE_* constants
         */
        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, null);
            }
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         *
         * @param mode one of the STATE_* constants
         * @param message string to add to screen or null
         */
        public void setState(int mode, CharSequence message) {
            /*
             * This method optionally can cause a text message to be displayed
             * to the user when the mode changes. Since the View that actually
             * renders that text is part of the main View hierarchy and not
             * owned by this thread, we can't touch the state of that View.
             * Instead we use a Message + Handler to relay commands to the main
             * thread, which updates the user-text View.
             */
            synchronized (mSurfaceHolder) {

                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    //b.putString("text", str.toString());
                    b.putInt("viz", View.VISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
            }
        }

        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;

                // don't forget to resize the background image
            }
        }

        /**
         * Resumes from a pause.
         */
        public void unpause() {
            // Move the real time clock up to now
            synchronized (mSurfaceHolder) {
            }
        }

        /**
         * Handles a key-down event.
         *
         * @param keyCode the key that was pressed
         * @param msg the original event object
         * @return true
         */
        boolean doKeyDown(int keyCode, KeyEvent msg) {
            synchronized (mSurfaceHolder) {
                boolean okStart = false;
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) okStart = true;
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) okStart = true;
                if (keyCode == KeyEvent.KEYCODE_S) okStart = true;

                if (okStart)
                {// ready-to-start -> start
                    doStart();
                    return true;
                }

                return false;
            }
        }

        /**
         * Handles a key-up event.
         *
         * @param keyCode the key that was pressed
         * @param msg the original event object
         * @return true if the key was handled and consumed, or else false
         */
        boolean doKeyUp(int keyCode, KeyEvent msg) {
            boolean handled = false;

            synchronized (mSurfaceHolder) {
            }

            return handled;
        }

        long lastDrawTime = 0L;
        /**
         * Draws the ship, fuel/speed bars, and background to the provided
         * Canvas.
         */
        private void doDraw(Canvas canvas) {
        	float fps = 1000.0f / (System.currentTimeMillis() - lastDrawTime);
        	lastDrawTime = System.currentTimeMillis();         	
        	
        	if( canvas == null )
        		return;
        	
            // Draw the background image. Operations on the Canvas accumulate
            // so this is like clearing the screen.
            canvas.drawRect(0, 0, 300, 300, mBlackPaint);
            
            canvas.drawText(String.format("FPS: %1$.2f", fps), 10, 100, mLinePaint);


            // Draw the fuel gauge
            //mScratchRect.set(4, 4, 4 + fuelWidth, 4 + UI_BAR_HEIGHT);
            //canvas.drawRect(mScratchRect, mLinePaint);

            // Draw the ship with its current rotation
            canvas.save();
            canvas.restore();
        }
    }

    /** Handle to the application context, used to e.g. fetch Drawables. */
    private Context mContext;

    /** Pointer to the text view to display "Paused.." etc. */
    private TextView mStatusText;

    /** The thread that actually draws the animation */
    private UpdateThread thread;

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new UpdateThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                mStatusText.setVisibility(m.getData().getInt("viz"));
                mStatusText.setText(m.getData().getString("text"));
            }
        });

        setFocusable(true); // make sure we get key events
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     *
     * @return the animation thread
     */
    public UpdateThread getUpdateThread() {
        return thread;
    }

    /**
     * Standard override to get key-press events.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        return thread.doKeyDown(keyCode, msg);
    }

    /**
     * Standard override for key-up. We actually care about these, so we can
     * turn off the engine or stop rotating.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent msg) {
        return thread.doKeyUp(keyCode, msg);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
    	/*
    	if( thread.mMode == (int)UpdateThread.STATE_READY )
    	{
    		thread.doStart();
    		return true;
    	}
    	*/
    	if( event.getY() < 500 )
    	{
    		thread.doKeyDown(KeyEvent.KEYCODE_SPACE, null);
    		thread.doKeyUp(KeyEvent.KEYCODE_SPACE, null);
    	}
    	if( event.getY() > 500 )
    	{
    		thread.doKeyDown(KeyEvent.KEYCODE_DPAD_DOWN, null);
    		thread.doKeyUp(KeyEvent.KEYCODE_DPAD_DOWN, null);
    	}
    	return false;
    }
    
    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();
    }

    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextView(TextView textView) {
        mStatusText = textView;
    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        thread.setSurfaceSize(width, height);
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setRunning(true);
        thread.start();
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
}
