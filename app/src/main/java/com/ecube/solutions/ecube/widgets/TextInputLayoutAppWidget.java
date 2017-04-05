package com.ecube.solutions.ecube.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
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
import android.util.TypedValue;
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

import static android.content.ContentValues.TAG;

/**
 * Widget based on TextInputLayoutExtended that formats error message automatically for passwords, email...
 */
public class TextInputLayoutAppWidget extends LinearLayout {
    private static final String TAG = TextInputLayoutAppWidget.class.getSimpleName();
    private static final boolean DEBUG = true;

    private String mHintText;                                       //Hint text
    private int mErrorColor=0;
    private Locale mLocale;                                         //Locale in order to process phone number
    private TextInputEditText mEditText;                            //EditText inside
    private TextInputLayoutExtended mTextInputLayout;               //Main TextInputLayout

    //Handle shadow confirmation
    private boolean mHasShadow;                                     //Enables shadow visibility
    private TextInputEditText mEditTextShadow;                      //Shadow for password confirm
    private TextInputLayoutExtended mTextInputLayoutShadow;         //Shadow for password confirm

    private int mColorErrorTyping = 0;                              //Error message color when typing
    private int mColorErrorFinal;                                   //Error message color final
    private int mColorTextActive;                                   //EditText text color when active
    private int mColorTextInactive;                                 //EditText text color when inactive

    private int mInputMode;                                         //Input mode

    //Handle rotations
    private static final String KEY_SAVE_LOCALE = "save.locale";
    private static final String KEY_SAVE_COLOR_ERROR_TEXT = "save.error.color";
    private static final String KEY_SAVE_ERROR_TEXT = "save.error.text";
    private static final String KEY_SAVE_COLOR_ERROR_TEXT_SHADOW = "save.shadow.error.color";
    private static final String KEY_SAVE_ERROR_TEXT_SHADOW = "save.shadow.error.text";

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
        //Default colors
        mColorErrorTyping = ContextCompat.getColor(getContext(), R.color.md_lime_700);  //-5983189 Default color
        mColorErrorFinal = ContextCompat.getColor(getContext(),R.color.md_red_500);     //-769226
        mColorTextActive = ContextCompat.getColor(getContext(),R.color.md_green_A700);  //-16725933
        mColorTextInactive = ContextCompat.getColor(getContext(),R.color.md_green_700); //-13070788

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
                case R.styleable.TextInputLayoutAppWidget_textActiveColor:
                    myColor = typedArray.getResourceId(R.styleable.TextInputLayoutAppWidget_textActiveColor,0);
                    if (myColor != 0 )
                        mColorTextActive = ContextCompat.getColor(getContext(),myColor);
                    Log.i(TAG, "Got color: " + mColorTextActive);
                    break;
                case R.styleable.TextInputLayoutAppWidget_textInactiveColor:
                    myColor = typedArray.getResourceId(R.styleable.TextInputLayoutAppWidget_textInactiveColor,0);
                    if (myColor != 0 )
                        mColorTextInactive = ContextCompat.getColor(getContext(),myColor);
                    break;
                default:
                    //Do nothing
            }
        }
        typedArray.recycle();

        View root = LayoutInflater.from(context).inflate(R.layout.widget_text_input_layout, this, true);
        mEditText = (TextInputEditText) root.findViewById(R.id.widget_text_input_layout_TextInputEditText);
        mEditTextShadow = (TextInputEditText) root.findViewById(R.id.widget_text_input_layout_TextInputEditText_shadow);
        mTextInputLayoutShadow = (TextInputLayoutExtended) root.findViewById(R.id.widget_text_input_layout_TextInputLayout_shadow);
        mTextInputLayout = (TextInputLayoutExtended) root.findViewById(R.id.widget_text_input_layout_TextInputLayout);

        if (!mHasShadow) {
            mTextInputLayoutShadow.setVisibility(GONE);
        } else {
            mTextInputLayoutShadow.setVisibility(VISIBLE);
            mTextInputLayoutShadow.setHint("Confirm " + mHintText);
        }
        mTextInputLayout.setHint(mHintText);
        if (isInEditMode()) {   // If we are in development we stop here
            return;
        }

         //Set default Locale
        mLocale = Locale.getDefault();

        //Handle Active/Inactive colors
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_focused}, // enabled
                new int[] {-android.R.attr.state_focused}, // disabled
        };
        int[] colors = new int[] {
                mColorTextActive,
                mColorTextActive
        };
        final ColorStateList myColorStateListActive = new ColorStateList(states, colors);
        colors = new int[] {
                mColorTextInactive,
                mColorTextInactive
        };
        final ColorStateList myColorStateListInactive = new ColorStateList(states, colors);
        mTextInputLayout.setPasswordVisibilityToggleTintList(myColorStateListInactive);
        mTextInputLayoutShadow.setPasswordVisibilityToggleTintList(myColorStateListInactive);
        //Set text color based on focus of the EditText !
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mEditText.setTextColor(mColorTextActive);
                    mTextInputLayout.setPasswordVisibilityToggleTintList(myColorStateListActive);
                } else {
                    mEditText.setTextColor(mColorTextInactive);
                    mTextInputLayout.setPasswordVisibilityToggleTintList(myColorStateListInactive);
                }
            }
        });
        //Set text color based on focus of the EditText !
        mEditTextShadow.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mEditTextShadow.setTextColor(mColorTextActive);
                    mTextInputLayoutShadow.setPasswordVisibilityToggleTintList(myColorStateListActive);
                } else {
                    mEditTextShadow.setTextColor(mColorTextInactive);
                    mTextInputLayoutShadow.setPasswordVisibilityToggleTintList(myColorStateListInactive);
                }
            }
        });

        //Handle TextWatcher depending on inputMode
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
                        if (charSequence.length() > 0) {
                            if (charSequence.toString().equals("") || charSequence.toString().length() < 3) {
                                mTextInputLayout.setError("Invalid name", mColorErrorTyping);
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
                            if (charSequence.length() > 0) {
                                if (!Patterns.EMAIL_ADDRESS.matcher(charSequence).matches()) {
                                    mTextInputLayout.setError("Invalid email",mColorErrorTyping);
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
                            if (editable.length() > 0 && editable.length() < 15) {
                                if (checkPhoneNumber(getText())) {
                                    mTextInputLayout.setError("");
                                    hideInputKeyBoard();
                                } else {
                                    mTextInputLayout.setError("Invalid phone number",mColorErrorTyping);
                                }
                                if ((editable.toString().matches("[0-9]+") && editable.toString().length() > 0)) {
                                    //Do nothing
                                } else {
                                    //The input character was not a number so we remove it
                                    editable.delete(editable.length() - 1, editable.length());
                                }
                            } else if (editable.length() >= 15) {
                                //Do not allow to enter more than 15 digits
                                editable.delete(editable.length() - 1, editable.length());
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
                            mTextInputLayout.setErrorTextColor(mErrorColor);
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
                            //Log.i(TAG,"Text Watcher ! password_confirm");
                            mTextInputLayout.setError("");
                            if (editable.length() > 0) {
                                int quality = getPasswordQuality(editable.toString());

                                if (quality < 70)
                                    mTextInputLayout.setError("Incomplete password",mColorErrorTyping);
                                else
                                    mTextInputLayout.setError("");
                            } else {
                                mTextInputLayout.setError("");
                            }
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
                    if (mEditTextShadow.getText().toString().equals(tmpStr)) {
                        mTextInputLayoutShadow.setError("");
                    } else {
                        mTextInputLayoutShadow.setError("Passwords not matching", mColorErrorTyping);
                    }
                }
            }
        });

        //Password text listener
        mEditText.addTextChangedListener(mTextWatcher);
    }

    //Hide input keyboard
    private void hideInputKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindowToken(),0);
    }


    public TextInputLayout getTextInputLayout() {
        return mTextInputLayout;
    }
    //Set locale for phone
    public void setLocale(Locale locale) {
        mLocale = locale;
    }

    //Get EditText text
    public String getText() {
        return mTextInputLayout.getText();
    }

    //Set EditText text
    public void setText(String text) {
        mTextInputLayout.setText(text);
    }

    //get the editText respective
    public TextInputEditText getEditText() {
        return mEditText;
    }


    //Set error
    public void setError(String error_text) {
        mTextInputLayout.setError("");
        mTextInputLayout.setErrorEnabled(false);
        mTextInputLayout.refreshDrawableState();
        mTextInputLayout.setErrorEnabled(true);
        mTextInputLayout.setErrorTextColor(mColorErrorFinal);
        mTextInputLayout.setError(error_text, mColorErrorFinal);
        //mTextInputLayout.refreshDrawableState();
        this.refreshDrawableState();
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
        data.putSerializable(KEY_SAVE_LOCALE, mLocale);
        //Save error and color for main
        data.putInt(KEY_SAVE_COLOR_ERROR_TEXT, mTextInputLayout.getErrorTextColor());
        if (mTextInputLayout.getError()!= null)
            data.putString(KEY_SAVE_ERROR_TEXT, mTextInputLayout.getError().toString());
        else
            data.putString(KEY_SAVE_ERROR_TEXT, "");
        //Save error and color for shadow
        data.putInt(KEY_SAVE_COLOR_ERROR_TEXT_SHADOW, mTextInputLayoutShadow.getErrorTextColor());
        if (mTextInputLayoutShadow.getError()!= null)
            data.putString(KEY_SAVE_ERROR_TEXT_SHADOW, mTextInputLayoutShadow.getError().toString());
        else
            data.putString(KEY_SAVE_ERROR_TEXT_SHADOW, "");

        ss.myDataToSave = data;
        Log.i(TAG, "OnSaveInstance : Saved locale : " + mLocale.getDisplayCountry());
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
        Log.i(TAG, "OnRestoreInstance");
        mLocale = (Locale) data.getSerializable(KEY_SAVE_LOCALE);
        mTextInputLayout.setError((String) data.getString(KEY_SAVE_ERROR_TEXT));
        mTextInputLayout.setErrorTextColor((int) data.getInt(KEY_SAVE_COLOR_ERROR_TEXT));
        mTextInputLayoutShadow.setError((String) data.getString(KEY_SAVE_ERROR_TEXT_SHADOW));
        mTextInputLayoutShadow.setErrorTextColor((int) data.getInt(KEY_SAVE_COLOR_ERROR_TEXT_SHADOW));
        Log.i(TAG, "OnRestoreInstance : Restored locale : " + mLocale.getDisplayCountry());
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
            mTextInputLayout.setError("Invalid field", mColorErrorFinal);
        }
        if (!shadowMatches()) {
            mTextInputLayoutShadow.setError("Invalid field",mColorErrorFinal);
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
