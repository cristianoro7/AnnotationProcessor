package com.desperado.processors;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by desperado on 17-11-28.
 */

public class ViewClass {

    private String packName;

    private String className;

    private List<ViewField> fields = new ArrayList<>();

    private TypeElement typeElement;

    public ViewClass(String packName, String className, TypeElement typeElement) {
        this.packName = packName;
        this.className = className;
        this.typeElement = typeElement;
    }

    public void addField(ViewField viewField) {
        fields.add(viewField);
    }

    public JavaFile generateCode() {
        MethodSpec.Builder con = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(typeElement.asType()), "activity")
                .addParameter(ClassName.get("android.view", "View"), "view")
                .beginControlFlow("if (activity == null)")
                .addStatement("return")
                .endControlFlow();

        for (ViewField f : fields) {
            con.addStatement("activity.$N = view.findViewById($L)", f.getFieldName(), f.getId());
        }

        FieldSpec.Builder fid = FieldSpec.builder(TypeName.get(typeElement.asType()), "activity");
        TypeSpec typeSpec = TypeSpec.classBuilder(className + "$ViewBinding")
                .addModifiers(Modifier.PUBLIC)
                .addField(fid.build())
                .addMethod(con.build())
                .build();
        return JavaFile.builder(packName, typeSpec).build();
    }
}
