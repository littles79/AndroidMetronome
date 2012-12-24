package littles.metronome;

import littles.metronome.R;

import littles.metronome.MainView;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.media.SoundPool;
import android.media.AudioManager;

public class MainActivity extends Activity {
	
	protected MainView mView;
	protected ImageView mImageView;
	protected SeekBar mBPMBar;
	
	protected SoundPool mSoundPool;
	protected int mTickSound;
	protected NumberPicker mBPMPicker;
	protected TextView mBPMText;
	
	public boolean mRenderPending;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mSoundPool = new SoundPool(4, android.media.AudioManager.STREAM_MUSIC, 0);
        mSoundPool.setOnLoadCompleteListener( new SoundPool.OnLoadCompleteListener() {

			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				
			}
        }
        );
        mTickSound = mSoundPool.load(this.getBaseContext(), R.raw.tick, 1);
        
        setContentView(R.layout.activity_main);
        mView = (MainView) findViewById(R.id.view1);
        mImageView = (ImageView) findViewById(R.id.imageView1);
        mBPMPicker = (NumberPicker) findViewById(R.id.numberPicker1);

        mBPMText = (TextView) findViewById(R.id.bpmText);

        mBPMBar = (SeekBar) findViewById(R.id.seekBar1);
        mBPMBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				UpdateBPM(seekBar);
			}
		});
        String[] values = new String[290];
        for(int i = 10; i < 300; ++i)
        {
        	values[i-10] = Integer.toString(i);
        }
    	mBPMPicker.setDisplayedValues(values);
        mView.getUpdateThread().mActivity = this;
        UpdateBPM( mBPMBar );
    }
    
    void UpdateBPM(SeekBar bar)
    {
    	int bpm = bar.getProgress();
		mView.getUpdateThread().mBPM = bpm; 
		mBPMText.setText(Integer.toString(bpm));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /*
    @Override
    public void onStop()
    {

    }
    */
}
