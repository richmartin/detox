package com.moozvine.detox.processor;

import com.moozvine.detox.GenerateDTO;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes("com.moozvine.detox.GenerateDTO")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class GenerateDTOProcessor extends AbstractProcessor {
  @Override
  public boolean process(
      final Set<? extends TypeElement> annotations,
      final RoundEnvironment roundEnv) {
    for (final Element element : roundEnv.getElementsAnnotatedWith(GenerateDTO.class)) {
      try {
        processElement(element);
      } catch (final IOException e) {
        error("Unable to write output file for " + element.getSimpleName() + "; " + e.getMessage());
      } catch (final InvalidTypeException e) {
        error("Invalid serializable: " + e.getMessage());
      }
    }
    return true;
  }

  private void processElement(final Element element)
      throws IOException, InvalidTypeException {
    if (!element.getKind().equals(ElementKind.INTERFACE)) {
      error(element.getEnclosingElement() + "." + element.getSimpleName()
          + " is not an interface. Only interfaces can support @GenerateDTO");
      throw new InvalidTypeException();
    }
    final ElementToProcess elementToProcess
        = new ElementToProcess(processingEnv, (TypeElement) element);
    new DtoSynthesiser(processingEnv).writeDtoFor(elementToProcess);
    new BuilderSynthesiser(processingEnv).writeBuilderFor(elementToProcess);
  }

  private void error(final String message) {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
  }
}
