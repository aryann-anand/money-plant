package com.christo.moneyplant.helpers;

import com.google.android.material.textfield.TextInputLayout;


import java.util.HashMap;

public class TextInputForm {
    private final HashMap<String, TextInputLayout> forms;
    private final HashMap<String, TextInputValidator> validators ;
    private final HashMap<String, Boolean> optional;

    public interface TextInputValidator {
        public void validate (String text) throws Error;
    }

    public TextInputForm() {
        this.forms = new HashMap<>();
        this.validators = new HashMap<>();
        this.optional = new HashMap<>();
    }
    public void addInput (String key, TextInputLayout inputLayout){
        this.forms.put(key, inputLayout);
        this.optional.put(key, false);
//        inputLayout.getEditText()
    }
    public void addInput (String key, TextInputLayout inputLayout, boolean optional){
        this.forms.put(key, inputLayout);
        this.optional.put(key, optional);
    }
    public void addValidator (String key, TextInputValidator validator) {
        this.validators.put(key, validator);
    }
    public TextInputLayout getInput (String key) {
        return this.forms.get(key);
    }
    public boolean validateForm () {
        this.resetErrors();
        boolean valid = true;
        for (String form : forms.keySet()) {
            TextInputValidator validator = validators.get(form);
            if (validator == null) continue;
            Boolean is_optional = optional.get(form);
            TextInputLayout textInputLayout = forms.get(form);
            String text = textInputLayout.getEditText().getText().toString().trim();
            try {
                if (!is_optional && text.isEmpty()) throw  new Error("This field is required !");
                validator.validate(text);
            } catch (Error e) {
                textInputLayout.setError(e.getMessage());
                valid=false;
            }
        }
        return  valid;
    }

    public HashMap<String, String> getFormData() {
        HashMap<String, String> formData = new HashMap<>();
        for (String key : forms.keySet()) {
            String form = forms.get(key).getEditText().getText().toString().trim();
            formData.put(key, form);
        }
        return  formData;
    }

    public void resetErrors () {
        for (TextInputLayout inputLayout : forms.values()) {
            inputLayout.setError(null);
        }
    }

}
