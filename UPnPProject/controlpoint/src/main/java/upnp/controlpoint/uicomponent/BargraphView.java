package upnp.controlpoint.uicomponent;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tpvision.sensormgt.controlpoint.fridge.R;


//TODO: orientation
//TODO: led spacing percentage


/**
 * Draws a bargraph display 
 * number of leds, and led spacing (spacing between leds can be set)  
 * ledWidth,ledHeight, and led spacing are adjusted based on available space
 * Led spacing percentage is favoured above lde spacing. I.e. If led spacing percentage is set, led spacing is ignored
 * If the specified dimensions are to large to fit within the view, the number of leds is reduced.
 *
 * Colors are defined in the led_colors_on, and led_colors_off array
 * If the number of specified colors is smaller than the number of leds, the last color is repeated.
 * For a bargraph display in which all leds have the same color just specify a single color in the led_colors_on array
 * The led_colors_off array can be set to the background color, or darker versions of the on colors
 *
 * @attr ref android.R.styleable#BargraphView_orientation
 * @attr ref android.R.styleable#BargraphView_numberOfLeds
 * @attr ref android.R.styleable#BargraphView_ledWidth
 * @attr ref android.R.styleable#BargraphView_ledHeight
 * @attr ref android.R.styleable#BargraphView_ledSpacing
 * @attr ref android.R.styleable#BargraphView_ledSpacingPercentage
 * @attr ref android.R.styleable#BargraphView_roundingRadius
 * 
 */
public class BargraphView extends View {
    
	//for debugging
	private static final String TAG = "BargraphView"; 
	private static final boolean DBG = true;
	
	//default values
	private static final int DEFAULT_LED_WIDTH = 30;
	private static final int DEFAULT_LED_HEIGHT = 20;
	private static final int DEFAULT_NUM_LEDS = 10;
	private static final int DEFAULT_LED_SPACING_PERCENTAGE = 20;
	
	//drawing orientation
	private int mOrientation;
	private static final int HORIZONTAL = 0;
	private static final int VERTICAL = 1;
	
	//values for attributes and setters
	public static final int UNSPECIFIED = -1;
	private int mNumLeds=10;
	private int mLedWidth=UNSPECIFIED;
	private int mLedHeight=UNSPECIFIED;
	private int mLedSpacing=UNSPECIFIED;
	private int mLedSpacingPercentage=UNSPECIFIED;
	
	//computed values based on screen size, set values and defaults
	private int mComputedLedWidth;
	private int mComputedLedHeight;
	private int mComputedLedSpacing;
	private int mComputedLedSpacingPercentage;
	private int mComputedNumLeds;
	private float mLedCornerRadius=0;
	
	//number of active leds
	private int mLevel=0;
	
	//colors defined in xml array file
	private TypedArray mLedColorsOn;
	private TypedArray mLedColorsOff;
	
	//to avoid re-creating each onDraw
	private Paint mPaint;
	private RectF mLed;
	
	
    /**
     * Constructor for manual object creation
     * @param context
     */
    public BargraphView(Context context) {
        super(context);
        initBargraphView();
    }

    /**
     * Construct object, initializing with any attributes we understand from a
     * layout file. 
     * 
     * @see View#View(Context, AttributeSet)
     */
    public BargraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBargraphView();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BargraphView);
        
        mNumLeds = a.getInt(R.styleable.BargraphView_numberOfLeds, UNSPECIFIED);
        mLedWidth = a.getDimensionPixelSize(R.styleable.BargraphView_ledWidth, UNSPECIFIED);
        mLedHeight = a.getDimensionPixelSize(R.styleable.BargraphView_ledHeight, UNSPECIFIED);
        mLedSpacing = a.getDimensionPixelSize(R.styleable.BargraphView_ledSpacing, UNSPECIFIED);
        mOrientation = a.getInteger(R.styleable.BargraphView_orientation, VERTICAL);
        mLedCornerRadius = a.getFloat(R.styleable.BargraphView_ledCornerRadius, 0);
        
        //TODO: led spacing percentage attr
         
        a.recycle();
    }

    //TODO: 3rd constructor
    
    private final void initBargraphView() {
    	if (!isInEditMode())
    	{
    		Resources res = getResources();
    		mLedColorsOn = res.obtainTypedArray(R.array.led_colors_on);
    		mLedColorsOff = res.obtainTypedArray(R.array.led_colors_off);
    	}
    	
    	mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        
        mLed = new RectF();
    }

   
    /**
     * Sets the current led display level
     * @param level value to indicate how many leds light up
     */
    public void setLevel(int level) {
    	mLevel = level;
    	if (DBG) Log.d(TAG,"setLevel: "+level);
    	invalidate();
    }
    
    
    /**
     * @see View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	
    	int width = measureWidth(widthMeasureSpec);
    	int height = measureHeight(heightMeasureSpec);
    	
    	if (DBG) Log.d(TAG,"onMeasure: "+ width +", "+ height);
        setMeasuredDimension(width, height);
    }
    
    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;    
            
        } else {
            //compute width using specified ledWidth or default ledWidth
        	int ledwidth = mLedWidth==UNSPECIFIED ? DEFAULT_LED_WIDTH : mLedWidth;
            result = (int) getPaddingLeft() + getPaddingRight() + ledwidth;
            //where do we set computed ledwith
            
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
                
            }
        }
        return result;
    }

    /**
     * Determines the height of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
            
        } else {
            // determine size
        	//compute height using specified ledHeight or default
        	int ledHeight = (mLedHeight==UNSPECIFIED) ? DEFAULT_LED_HEIGHT : mLedHeight;
        	//compute number of Leds using specified number or default
        	int numLeds = (mNumLeds == UNSPECIFIED) ? DEFAULT_NUM_LEDS : mNumLeds;
        	
        	int ledSpacing;
        	if (mLedSpacingPercentage == UNSPECIFIED) {
        		//spacing percentage is not set, try spacing, if also not specified use spacing percentage
        		ledSpacing = (mLedSpacing == UNSPECIFIED) ? (ledHeight* DEFAULT_LED_SPACING_PERCENTAGE/100) : mLedSpacing;
        	} else {
        		//percentage is set, use it
        		ledSpacing = ledHeight * mLedSpacingPercentage/100;
        	}
        		
        		
            result = (int) (numLeds * (ledHeight+ledSpacing)) + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	 if (DBG) Log.d(TAG,"onSizeChanged: "+w+", "+h);
    	 
    	 computeLedDimensions(w,h);
    }
    
    /**
     * Computes number of leds, led size, and led spacing 
     * @param w width including spacing
     * @param h height including spacing
     */
    protected void computeLedDimensions(int w, int h) {
    
	    int width = w - getPaddingLeft() -getPaddingRight();
	   	int height = h - getPaddingTop() -getPaddingBottom();	
	    	
	   	// vertical bar, just take the remaining width
	   	mComputedLedWidth = width; 
	   	 
	   	 
	   	//assume num leds is fixed. 
	   	int numLeds = (mNumLeds == UNSPECIFIED) ? DEFAULT_NUM_LEDS : mNumLeds;
	   	int spacing;
	   	int ledHeight;
	   	int spacingPercentage;
	   	//cover 4 cases for spacing, and height
	   	if (mLedSpacing ==UNSPECIFIED) //TODO: check if this shouldn't be percentage
	   	{
	   		if (mLedHeight == UNSPECIFIED){
	   			//ledHeight and spacing undefined, ledHeight use spacing percentage or its default
	   			spacingPercentage = (mLedSpacingPercentage == UNSPECIFIED) ? 20 : mLedSpacingPercentage;
	   			spacing = (int) ((numLeds==1) ? 0 :height*spacingPercentage/((numLeds-1)*100));
	   			ledHeight = (int) (height*(100-spacingPercentage))/(numLeds*100);
	   		}
	   		else {
	   			//height defined, spacing undefined. take 20% of led height
	   			ledHeight = mLedHeight;
	   			spacingPercentage = (mLedSpacingPercentage == UNSPECIFIED) ? 20 : mLedSpacingPercentage;
	   			spacing = (int) ledHeight*spacingPercentage/100;
	   		}
	   			
	   	} else { //TODO: add spacing percentage
	   		if (mLedHeight == UNSPECIFIED){
	   			//spacing defined, height undefined. Calculate led height after subtracting led spacing from height
	   			spacing = mLedSpacing;
	   			ledHeight = (height - (spacing*(numLeds-1)))/numLeds;
	   		} 
	   		else {
	   			//spacing and height defined. Just accept it. If it doesn't fir we correct later
	   			spacing = mLedSpacing;
	   			ledHeight = mLedHeight;
	   		}
	   	}
	   	
	   	//if it doesn't fit, reduce number of leds, worst case we end up with 1 led (and no spacing)
	   	if (((ledHeight * numLeds) + (spacing * (numLeds-1))) > height)
	   	{
	   		while ((((ledHeight * numLeds) + (spacing * (numLeds-1))) > height) && (numLeds > 0)) {
	   		numLeds--;
	   		}
//TODO: spacing percentage	   		
	   		//numLeds adapted, recompute spacing and ledheight
	   		//default space between leds is 20% of led height
	       	spacing = (mLedSpacing ==UNSPECIFIED) ? ((numLeds==1) ? 0 :height*20/((numLeds-1)*100)) : mLedSpacing;
	       	//compute remaining ledHeight, override setting to exactly match space
	       	ledHeight = (int) (height-(spacing*(numLeds-1)))/numLeds;
	   	
	   	}
	   	
	   	mComputedLedHeight = ledHeight;
	   	mComputedLedSpacing = spacing;
	   	mComputedNumLeds = numLeds;
	   	Log.v(TAG,"Number of leds: "+mComputedNumLeds );
	   	Log.v(TAG,"Led size: "+mComputedLedWidth + "x" + mComputedLedHeight );
	   	Log.v(TAG,"Led Spacing: "+mComputedLedSpacing );
	   	
    }
    
    
    /**
     * Gets the number of leds computed based on default value or previously set number of leds
     * If number of leds don't fit view size, the number of leds is adapted
     * @return number of leds
     */
    public int getNumberOfLeds() {
    	return mComputedNumLeds;
    }
    
    
    /**
     * Sets the number of leds, or BargraphView.UNDEFINED
     * @param numLeds, 
     */
    public void setNumberOfLeds(int numLeds) {
    	mNumLeds = numLeds;
    	computeLedDimensions(getWidth(), getHeight());
    	if (DBG) Log.d(TAG," setNumberOfLeds: "+numLeds+ " computed: "+mComputedNumLeds);
    	invalidate();
    }
    
    /**
     * gets the ledWidth as computed based on previously defined set value or default value
     * @return led width
     */
    public int getLedWidth() {
    	return mComputedLedWidth;
    }
    
    
    /**
     * Sets the ledwith, or BargraphView.UNDEFINED
     * @param ledWidth, sets ledWidth or BargraphView.UNDEFINED 
     */
    public void setLedWidth(int ledWidth) {
    	mLedWidth = ledWidth;
    	computeLedDimensions(getWidth(), getHeight());
    	if (DBG) Log.d(TAG," setLedWidth: "+ledWidth+ " computed: "+mComputedLedWidth);
    	invalidate();
    }
    
    /**
     * gets the led height as computed based on previously defined set value or default value
     * @return led height
     */
    public int getLedHeight() {
    	return mComputedLedHeight;
    }
    
    
    /**
     * Sets the led height, or BargraphView.UNDEFINED
     * @param ledHeight, sets ledHeight or BargraphView.UNDEFINED 
     */
    public void setLedHeight(int ledHeight) {
    	mLedHeight = ledHeight;
    	computeLedDimensions(getWidth(), getHeight());
    	if (DBG) Log.d(TAG," setLedHeight: "+ledHeight+ " computed: "+mComputedLedHeight);
    	invalidate();
    }
    
    /**
     * gets the spacing between leds as computed based on previously defined set value or default value
     * @return led spacing
     */
    public int getLedSpacing() {
    	return mComputedLedHeight;
    }
    
    
    /**
     * Sets the led height, or BargraphView.UNDEFINED
     * @param ledHeight, sets ledHeight or BargraphView.UNDEFINED 
     */
    public void setLedSpacing(int ledSpacing) {
    	mLedSpacing = ledSpacing;
    	computeLedDimensions(getWidth(), getHeight());
    	if (DBG) Log.d(TAG," setLedSpacing: "+ledSpacing+ " computed: "+mComputedLedSpacing);
    	invalidate();
    }
   
    //TODO: spacingPercentage settings - perhaps needs to reset spacing?
    
    /**
     * returns the value that was set previously using setLedCornerRadius or using the ledCornerRadius attribute
     * returns 0 if no value was set previously
     * @return radious
     */
    public float getLedCornerRadius() {
    	return mLedCornerRadius;
    }
    
    /**
     * Sets the led corner radius, if set to 0 leds will be rectangles
     * @param radius
     */
    public void setLedCornerRadius(float radius) {
    	mLedCornerRadius = radius;
    }
    
    /**
     * Render the bargraph
     * 
     * @see View#onDraw(Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
       super.onDraw(canvas);

       //if (DBG) Log.d(TAG,"onDraw: "+getMeasuredWidth()+", "+getMeasuredHeight());
    
       mLed.set(0, 0, mComputedLedWidth,mComputedLedHeight);
       
       float xPos = getPaddingLeft();
       float yPos = getMeasuredHeight()-getPaddingBottom()-mComputedLedHeight; //first led starts at bottom
       mLed.offsetTo(xPos, yPos);
       
     //TODO: copy to array
       int lastOnColor=0;
       int lastOffColor=0;
       if (!isInEditMode())
       {	   
    	   lastOffColor = mLedColorsOff.getColor(mLedColorsOff.length()-1,0);
    	   lastOnColor = mLedColorsOn.getColor(mLedColorsOn.length()-1,0);
       }
    	   
       //TODO: iterators
       for (int i=0; i< mComputedNumLeds; i++)
       {
    	   if (!isInEditMode()) {
    		   if (i<mLevel)
    			   if (i<mLedColorsOn.length())
    				   mPaint.setColor(mLedColorsOn.getColor(i,lastOnColor));
    			   else
    				   mPaint.setColor(lastOnColor);
    		   else
    			   if (i<mLedColorsOff.length())
    				   mPaint.setColor(mLedColorsOff.getColor(i,lastOffColor));
    			   else
    				   mPaint.setColor(lastOffColor);
    	   }
    	   else {
    		   mPaint.setColor(0xFF90C030); //just some green color for GUI tool
    	   }
    	   
    	   if (mLedCornerRadius != 0)
    		   canvas.drawRoundRect(mLed, mLedCornerRadius, mLedCornerRadius, mPaint);
    	   else
    		   canvas.drawRect(mLed, mPaint);

    	   yPos = yPos - mComputedLedHeight-mComputedLedSpacing;
           mLed.offsetTo(xPos, yPos);	       
       }
       
    }
}
