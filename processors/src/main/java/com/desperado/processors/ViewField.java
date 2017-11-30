package com.desperado.processors;

import com.desperado.annotations.BindView;

import javax.lang.model.element.VariableElement;

/**
 * Created by desperado on 17-11-28.
 */

public class ViewField {

    private String fieldName;

    private int id;

    public ViewField(VariableElement variableElement) {
        id = variableElement.getAnnotation(BindView.class).value();
        fieldName = variableElement.getSimpleName().toString();
    }

    public String getFieldName() {
        return fieldName;
    }

    public int getId() {
        return id;
    }
}
