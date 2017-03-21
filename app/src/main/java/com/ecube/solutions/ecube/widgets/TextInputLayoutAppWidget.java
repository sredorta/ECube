package com.ecube.solutions.ecube.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutCompat;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Patterns;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecube.solutions.ecube.R;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * Widget based on TextInputLayout that formats error message automatically
 */
public class TextInputLayoutAppWidget extends LinearLayout {
    private static final String TAG = TextInputLayoutAppWidget.class.getSimpleName();
    private static final boolean DEBUG = true;

    private int mInputType;         //Type of input
    private String mHintText;       //Hint text
    private int mErrorColor=0;
    private Locale mLocale;         //Locale in order to process phone number
    private EditText mEditText;     //EditText inside
    private TextInputLayout mTextInputLayout;

    //Handle shadow confirmation
    private boolean mHasShadow;                         //Enables shadow visibility
    private EditText mEditTextShadow;                   //Shadow for password confirm
    private TextInputLayout mTextInputLayoutShadow;     //Shadow for password confirm

    private int mColorErrorTyping;                      //Error message color when typing
    private int mColorErrorFinal;                       //Error message color final


    private int mInputMode; //Input mode
    /*
    <enum name ="name" value="0"/>
    <enum name ="email" value="1"/>
    <enum name ="phone" value="2"/>
    <enum name ="password_define" value="3"/>
    <enum name ="password_confirm" value="4"/>
    */

    //Handle rotations
    private static final String KEY_SAVE_COLOR_ERROR_TEXT = "error_text.color";

    public TextInputLayoutAppWidget(Context context) {
        this(context, null);
    }

    public TextInputLayoutAppWidget(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextInputLayoutAppWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Get input attributes from XML
        ////////////////////////////////////////////////////////////////////////////////////////////
        mColorErrorTyping = ContextCompat.getColor(getContext(), R.color.md_lime_500); //Default color
        mColorErrorFinal = ContextCompat.getColor(getContext(),R.color.md_red_500);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TextInputLayoutAppWidget, 0, 0);
        int n = typedArray.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = typedArray.getIndex(i);

            switch (attr) {
                case R.styleable.TextInputLayoutAppWidget_inputMode:
                    mInputMode = typedArray.getInt(R.styleable.TextInputLayoutAppWidget_inputMode,0);
                    break;
                case R.styleable.TextInputLayoutAppWidget_hint:
                    mHintText = typedArray.getString(R.styleable.TextInputLayoutAppWidget_hint);
                    break;
                case R.styleable.TextInputLayoutAppWidget_hasShadow:
                    mHasShadow = typedArray.getBoolean(R.styleable.TextInputLayoutAppWidget_hasShadow, false);
                    break;
                case R.styleable.TextInputLayoutAppWidget_errorColorTyping:
                    int myColor = typedArray.getResourceId(R.styleable.TextInputLayoutAppWidget_errorColorTyping,0);
                    if (myColor != 0 ) {
                        mColorErrorTyping = ContextCompat.getColor(getContext(), myColor);
                    }
                    break;
                case R.styleable.TextInputLayoutAppWidget_errorColorFinal:
                    myColor = typedArray.getResourceId(R.styleable.TextInputLayoutAppWidget_errorColorFinal,0);
                    if (myColor != 0 )
                        mColorErrorFinal = ContextCompat.getColor(getContext(),myColor);
                    break;
                default:
                    //Do nothing
            }
        }
        typedArray.recycle();

        View root = LayoutInflater.from(context).inflate(R.layout.widget_text_input_layout, this, true);

        mEditText = (EditText) root.findViewById(R.id.widget_text_input_layout_EditText);
        mEditTextShadow = (EditText) root.findViewById(R.id.widget_text_input_layout_EditText_shadow);
        mTextInputLayoutShadow = (TextInputLayout) root.findViewById(R.id.widget_text_input_layout_TextInputLayout_shadow);
        mTextInputLayout = (TextInputLayout) root.findViewById(R.id.widget_text_input_layout_TextInputLayout);
        LinearLayout mLinearLayout = (LinearLayout) root.findViewById(R.id.widget_text_input_layout_LinearLayout);

        if (!mHasShadow) {
            mTextInputLayoutShadow.setVisibility(GONE);
        } else {
            mTextInputLayoutShadow.setVisibility(VISIBLE);
            mTextInputLayoutShadow.setHint("Confirm " + mHintText);
        }
        mTextInputLayout.setHint(mHintText);

        //Allow Error reporting
        mTextInputLayout.setErrorEnabled(true);
        mTextInputLayout.setHintAnimationEnabled(true);
        mTextInputLayout.setError("");
        if (isInEditMode()) {   // If we are in development we stop here
            return;
        }

         //Set default Locale
        mLocale = Locale.getDefault();


/*        //Get widht of the progress bar and reset cardView accordingly
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Here you can get the size
                int width = mTextInputLayout.getMeasuredWidth();
                int height = mTextInputLayout.getMeasuredHeight();
                //Log.i(TAG, "TextInput layout : width = " + width + " height = :" + height);
                width = mEditText.getMeasuredWidth();
                height = mEditText.getMeasuredHeight();
                //Log.i(TAG, "EditText layout : width = " + width + " height = :" + height);

            }
        });
*/

        final TextWatcher mTextWatcher;
        switch (mInputMode) {
            case 0:  //name case
                mEditText.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME); //97   //textPersonName
                mTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        mTextInputLayout.setError("");
                        mErrorColor = mColorErrorTyping;
                        setErrorTextColor(mTextInputLayout, mErrorColor);
                        if (charSequence.length() > 0) {
                            if (charSequence.toString().equals("") || charSequence.toString().length() < 3) {
                                mTextInputLayout.setError("Invalid name");
                            } else {
                                mTextInputLayout.setError("");
                            }
                        } else {
                            mTextInputLayout.setError("");
                        }
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {}
                };
                break;
            case 1:  //email case
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS); //33 :textEmailAddress
                mTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        mTextInputLayout.setError("");
                        mErrorColor = mColorErrorTyping;
                        setErrorTextColor(mTextInputLayout, mErrorColor);
                        if (charSequence.length() > 0) {
                            if (!Patterns.EMAIL_ADDRESS.matcher(charSequence).matches()) {
                                mTextInputLayout.setError("Invalid email");
                            } else {
                                mTextInputLayout.setError("");
                            }
                        } else {
                            mTextInputLayout.setError("");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                };
                break;
            case 2:  //phone case  //Number 3
                mEditText.setInputType(InputType.TYPE_CLASS_PHONE); //3   //phone
                mTextWatcher = new PhoneNumberFormattingTextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        mTextInputLayout.setError("");
                        mErrorColor = mColorErrorTyping;
                        setErrorTextColor(mTextInputLayout, mErrorColor);
                        if (editable.length() > 0) {
                            if (checkPhoneNumber(getText())) {
                                mTextInputLayout.setError("");
                                hideInputKeyBoard();
                            } else {
                                mTextInputLayout.setError("Invalid phone number");
                            }
                            if ((editable.toString().matches("[0-9]+") && editable.toString().length() > 0)) {
                                //Do nothing
                            } else {
                                //The input character was not a number so we remove it
                                editable.delete(editable.length() - 1, editable.length());
                            }
                        } else {
                            mTextInputLayout.setError("");
                        }
                    }
                };
                break;
            case 3: //Password define
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); //81   //password
                mTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        mTextInputLayout.setError("");
                        mErrorColor = mColorErrorTyping;
                        if (editable.length() > 0) {
                            mTextInputLayout.setError("Password quality");
                            int quality = getPasswordQuality(editable.toString());
                            mTextInputLayout.setError(String.format("Password quality %s", quality).concat(" %"));
                            if (quality < 50)
                                mErrorColor = ContextCompat.getColor(getContext(), R.color.md_red_500);
                            else if (quality >= 50 && quality < 70)
                                mErrorColor = ContextCompat.getColor(getContext(), R.color.md_orange_500);
                            else if (quality >= 70)
                                mErrorColor = ContextCompat.getColor(getContext(), R.color.md_green_500);
                        } else {
                            mTextInputLayout.setError("");
                        }
                        setErrorTextColor(mTextInputLayout, mErrorColor);
                        mTextInputLayout.refreshDrawableState();
                    }
                };
                break;
            case 4:  //password_confirm
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                mTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        mTextInputLayout.setError("");
                        mErrorColor = mColorErrorTyping;
                        if (editable.length() > 0) {
                            int quality = getPasswordQuality(editable.toString());

                            if (quality < 70)
                                mTextInputLayout.setError(String.format("Incomplete password"));
                            else
                                mTextInputLayout.setError(String.format(""));
                        } else {
                            mTextInputLayout.setError("");
                        }
                        setErrorTextColor(mTextInputLayout, mErrorColor);
                        mTextInputLayout.refreshDrawableState();
                    }
                };
                break;
            default :
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                mTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                };

        }
        //We want to copy the input type of the primary into the shadow
        mEditTextShadow.setInputType(mEditText.getInputType());
        mEditTextShadow.refreshDrawableState();

        //Shadow text watcher
        mEditTextShadow.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Do not allow shadow be longer than real
                if (mEditTextShadow.getText().length()> mEditText.getText().length()) {
                    editable.delete(editable.length() - 1, editable.length());
                    mEditTextShadow.setText(editable);
                    mEditTextShadow.setSelection(mEditTextShadow.getText().length());
                }

                if (mEditTextShadow.getText().length() > 0 && mEditText.getText().length() > 0) {
                    //Check if both strings matches
                    String tmpStr = mEditText.getText().toString().substring(0, mEditTextShadow.getText().length());
                    Log.i(TAG, "tmpPassword = " + tmpStr);
                    if (mEditTextShadow.getText().toString().equals(tmpStr)) {
                        mTextInputLayoutShadow.setError("");
                    } else {
                        mTextInputLayoutShadow.setError("Passwords not matching");
                        mErrorColor = mColorErrorTyping;
                    }
                    setErrorTextColor(mTextInputLayoutShadow, mErrorColor);
                }
            }
        });


        //Password text listener
        mEditText.addTextChangedListener(mTextWatcher);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    //Hide input keyboard
    private void hideInputKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindowToken(),0);
    }


    //set Error message
    public void setError(String errorStr) {
        mTextInputLayout.setError(errorStr);
    }

    public void setError(String errorStr, int color) {
        mErrorColor = ContextCompat.getColor(getContext(), color);
        setErrorTextColor(mTextInputLayout, mErrorColor);
        mTextInputLayout.setError(errorStr);
        mTextInputLayout.refreshDrawableState();
    }

    //Set locale for phone
    public void setLocale(Locale locale) {
        mLocale = locale;
    }

    //Get EditText text
    public String getText() {
        return mEditText.getText().toString();
    }

    //Set EditText text
    public void setText(String text) {
        mEditText.setText(text);
    }

    //get the editText respective
    public EditText getEditText() {
        return mEditText;
    }

    public static void setErrorTextColor2(TextInputLayout textInputLayout, int color) {
        try {
            Field fErrorView = TextInputLayout.class.getDeclaredField("mErrorView");
            fErrorView.setAccessible(true);
            TextView mErrorView = (TextView) fErrorView.get(textInputLayout);
            Field fCurTextColor = TextView.class.getDeclaredField("mCurTextColor");
            fCurTextColor.setAccessible(true);
            fCurTextColor.set(mErrorView, color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Set error textcolor by reflection
    public void setErrorTextColor(View myView, int color) {
        try {
            Field fErrorView = TextInputLayout.class.getDeclaredField("mErrorView");
            fErrorView.setAccessible(true);
            TextView mErrorView = (TextView) fErrorView.get(myView);
            Field fCurTextColor = TextView.class.getDeclaredField("mCurTextColor");
            fCurTextColor.setAccessible(true);
            fCurTextColor.set(mErrorView, color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Set error textcolor by reflection
    public int getErrorTextColor(View myView) {
        try {
            Field fErrorView = TextInputLayout.class.getDeclaredField("mErrorView");
            fErrorView.setAccessible(true);
            TextView mErrorView = (TextView) fErrorView.get(myView);
            Field fCurTextColor = TextView.class.getDeclaredField("mCurTextColor");
            fCurTextColor.setAccessible(true);
            return (int) fCurTextColor.get(mErrorView);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Save contents on rotation hierarchically
    /////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.childrenStates = new SparseArray();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).saveHierarchyState(ss.childrenStates);
        }

        //Our View arguments we Save
        Bundle data = new Bundle();
        data.putInt(KEY_SAVE_COLOR_ERROR_TEXT, mErrorColor);
        data.putSerializable("LOCALE", mLocale);
        ss.myDataToSave = data;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).restoreHierarchyState(ss.childrenStates);
        }

        //Our View arguments we restore
        Bundle data = ss.myDataToSave;
        mLocale = (Locale) data.getSerializable("LOCALE");
        mErrorColor = data.getInt(KEY_SAVE_COLOR_ERROR_TEXT);
        Log.i(TAG, "Restored color : " + mErrorColor);
        Log.i(TAG, "Restored locale: " + mLocale.getDisplayCountry());
        mTextInputLayout.setErrorEnabled(true);
        TextInputLayoutAppWidget.setErrorTextColor2(mTextInputLayout, mErrorColor);
        //setErrorTextColor(mTextInputLayout,mErrorColor);

    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    static class SavedState extends BaseSavedState {
        SparseArray childrenStates;          // Handle hierarchies
        Bundle myDataToSave = new Bundle();  // Bundle used to transfer our View arguments
       // int ErrorTextColor;

       // Locale mLocale;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenStates = in.readSparseArray(classLoader);                   //Hierarchical read
            myDataToSave = in.readParcelable(Bundle.class.getClassLoader());    //Our data read
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSparseArray(childrenStates);   //Hierarchical write to Parcel
            out.writeParcelable(myDataToSave,0);    //OurView write to Parcel
        }

        public static final ClassLoaderCreator<SavedState> CREATOR
                = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Get if input is correct
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private boolean shadowMatches() {
        if (!mHasShadow) return true;
        if (mEditTextShadow.getText().length()==0) return false;
        return (mEditText.getText().toString().equals(mEditTextShadow.getText().toString()));
    }
    public boolean isValidInput() {
        /*
        <enum name ="name" value="0"/>
        <enum name ="email" value="1"/>
        <enum name ="phone" value="2"/>
        <enum name ="password_define" value="3"/>
        <enum name ="password_confirm" value="4"/>
                */
        boolean result = false;
        switch (mInputMode) {
            case 0: //textPersonName
                if (getText().length()>=3)
                    result = true;
                break;
            case 3: //Define Password
                if (getPasswordQuality(getText())> 70)
                    result = true;
                break;
            case 4: //Confirm Password
                if (getPasswordQuality(getText())> 70)
                    result = true;
                break;
            case 1: //email
                CharSequence cs = getText().trim();
                if (Patterns.EMAIL_ADDRESS.matcher(cs).matches())
                    result = true;
                break;
            case 2: //Phone
                if (checkPhoneNumber(mEditText.getText().toString()))
                    result = true;
                break;

            default:
                result = false;
        }
        if (!result) {
            mErrorColor = mColorErrorFinal;
            setErrorTextColor(mTextInputLayout, mErrorColor);
            mTextInputLayout.setError("Invalid field");
            mTextInputLayout.refreshDrawableState();
        }
        if (!shadowMatches()) {
            mErrorColor = mColorErrorFinal;
            setErrorTextColor(mTextInputLayoutShadow, mErrorColor);
            mTextInputLayoutShadow.setError("Invalid field");
            mTextInputLayoutShadow.refreshDrawableState();
            result = false;
        }

        return result;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Password handling
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //Get strength of password
    private int getPasswordQuality(String password) {
        int length = 0, uppercase = 0, lowercase = 0, digits = 0, symbols = 0, bonus = 0, requirements = 0;

        int lettersonly = 0, numbersonly = 0, cuc = 0, clc = 0;

        length = password.length();
        for (int i = 0; i < password.length(); i++) {
            if (Character.isUpperCase(password.charAt(i)))
                uppercase++;
            else if (Character.isLowerCase(password.charAt(i)))
                lowercase++;
            else if (Character.isDigit(password.charAt(i)))
                digits++;

            symbols = length - uppercase - lowercase - digits;
        }

        for (int j = 1; j < password.length() - 1; j++) {
            if (Character.isDigit(password.charAt(j)))
                bonus++;
        }
        for (int k = 0; k < password.length(); k++) {
            if (Character.isUpperCase(password.charAt(k))) {
                k++;
                if (k < password.length()) {
                    if (Character.isUpperCase(password.charAt(k))) {
                        cuc++;
                        k--;
                    }
                }
            }
        }

        for (int l = 0; l < password.length(); l++) {
            if (Character.isLowerCase(password.charAt(l))) {
                l++;
                if (l < password.length()) {
                    if (Character.isLowerCase(password.charAt(l))) {
                        clc++;
                        l--;
                    }
                }
            }
        }

        if (length > 7) { requirements++;}
        if (uppercase > 0) { requirements++; }
        if (lowercase > 0) { requirements++; }
        if (digits > 0) { requirements++; }
        if (symbols > 0) { requirements++; }
        if (bonus > 0) { requirements++;}
        if (digits == 0 && symbols == 0) { lettersonly = 1; }
        if (lowercase == 0 && uppercase == 0 && symbols == 0) { numbersonly = 1;}

        int Total = (length * 4) + ((length - uppercase) * 2)
                + ((length - lowercase) * 2) + (digits * 4) + (symbols * 6)
                + (bonus * 2) + (requirements * 2) - (lettersonly * length*2)
                - (numbersonly * length*3) - (cuc * 2) - (clc * 2);
        if (Total > 100) Total = 100;
        return Total;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Phone handling
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private boolean checkPhoneNumber(String phone) {
        Boolean isValid = false;
        if ((phone.matches("[0-9]+") && phone.length() > 0)) {
            //We only start checking if number is valid once length is larger than 2 to avoid crash
            if (phone.length()>2) {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber inputNumber;
                try {
                    inputNumber = phoneUtil.parse(phone, mLocale.getCountry());
                } catch (NumberParseException e) {
                    inputNumber = null;
                    Log.i(TAG, "Caught exception :" + e);
                }
                isValid = phoneUtil.isValidNumber(inputNumber);
            }
        }
        return isValid;
    }

    public String getFinalPhoneNumber() {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber inputNumber;
        try {
            inputNumber = phoneUtil.parse(mEditText.getText().toString(), mLocale.getCountry());
        } catch (NumberParseException e) {
            inputNumber = null;
            Log.i(TAG, "Caught exception :" + e);
        }
        return phoneUtil.format(inputNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

}
