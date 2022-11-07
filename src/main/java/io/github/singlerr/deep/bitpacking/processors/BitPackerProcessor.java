package io.github.singlerr.deep.bitpacking.processors;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import io.github.singlerr.deep.bitpacking.BitPacker;
import io.github.singlerr.deep.bitpacking.BitSpec;
import io.github.singlerr.deep.utils.BitUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/***
 * Annotation preprocessor for {@link io.github.singlerr.deep.bitpacking.BitPacker} and {@link BitSpec}
 * @author Singlerr
 */
@AutoService(Processor.class)
public final class BitPackerProcessor extends AbstractProcessor {

    private static final String CLASS_PREFIX = "Packed";

    private static final String PACKED_FIELD_NAME = "packedInteger";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BitPacker.class);
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, BitPacker.class.getName() + " cannot be used on " + element.getSimpleName());
                continue;
            }


            if (!element.getModifiers().contains(Modifier.ABSTRACT)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, BitPacker.class.getName() + " must be used on abstract class.");
                continue;
            }

            List<? extends Element> temp = element.getEnclosedElements();

            List<? extends Element> setters = temp.stream().filter(e -> checkCondition(e) && isSetter((ExecutableElement) e)).toList();

            List<? extends Element> getters = temp.stream().filter(e -> checkCondition(e) && isGetter((ExecutableElement) e)).toList();

            TypeElement root = (TypeElement) element;
            ClassName className = ClassName.get(root);

            TypeSpec.Builder classSpecBuilder = TypeSpec.classBuilder(CLASS_PREFIX.concat(root.getSimpleName().toString()))
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(className);


            FieldSpec packedIntegerSpec = FieldSpec.builder(TypeName.INT, PACKED_FIELD_NAME, Modifier.PRIVATE)
                    .initializer("0")
                    .build();

            classSpecBuilder.addField(packedIntegerSpec);

            int bitShiftAmount = 0;

            for (Element setter : setters) {
                BitSpec bitSpec = setter.getAnnotation(BitSpec.class);

                ExecutableElement methodElement = (ExecutableElement) setter;
                ParameterSpec parameterSpec = ParameterSpec.get(methodElement.getParameters().get(0));

                MethodSpec setterSpec = MethodSpec.methodBuilder(setter.getSimpleName().toString())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(parameterSpec)
                        .addAnnotation(Override.class)
                        .returns(TypeName.VOID)
                        .addStatement("$L |= $L << $L",PACKED_FIELD_NAME,parameterSpec.name,bitShiftAmount)
                        .build();

                classSpecBuilder.addMethod(setterSpec);

                Optional<? extends Element> getterCorresponds = getters.stream().filter(e -> getVariableName(e).equals(bitSpec.variableName())).findAny();

                if(getterCorresponds.isEmpty()){
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot find getter corresponding to setter with variable name \"" + bitSpec.variableName() + "\"");
                    continue;
                }

                ExecutableElement getter = (ExecutableElement) getterCorresponds.get();

                TypeName returnType = ClassName.get(getter.getReturnType());

                MethodSpec getterSpec = MethodSpec.methodBuilder(getter.getSimpleName().toString())
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(TypeName.get(getter.getReturnType()))
                        .addStatement("$T var = ($T) (($L >> $L) & 0x$L)",
                                returnType,
                                returnType,
                                PACKED_FIELD_NAME,
                                bitShiftAmount,
                                Long.toHexString(BitUtils.createBitMask(bitSpec.bitSize())))
                        .addStatement("return var")
                        .build();

                classSpecBuilder.addMethod(getterSpec);

                bitShiftAmount += bitSpec.bitSize();

                if(bitShiftAmount > 64){
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "total bit size cannot exceeds 64!");
                    break;
                }
            }

            TypeSpec classSpec = classSpecBuilder.build();

            Filer filer = processingEnv.getFiler();
            try{
                JavaFile.builder(className.packageName(),classSpec)
                        .build()
                        .writeTo(filer);
            }catch (IOException ex){
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "fatal error : " + ex);
            }
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(BitPacker.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private boolean isSetter(ExecutableElement element) {
        return element.getReturnType().getKind() == TypeKind.VOID && element.getParameters().size() == 1;
    }

    private boolean isGetter(ExecutableElement element) {
        return element.getReturnType().getKind() != TypeKind.VOID && element.getParameters().size() == 0;
    }

    private boolean checkCondition(Element element){
        if (element.getAnnotation(BitSpec.class) == null || !(element instanceof ExecutableElement))
            return false;

        if (!element.getModifiers().contains(Modifier.ABSTRACT)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, BitSpec.class.getName() + " must be used on abstract method.");
            return false;
        }
        return true;
    }

    private String getVariableName(Element element){
        BitSpec bitSpec = element.getAnnotation(BitSpec.class);
        return bitSpec.variableName();
    }

}
