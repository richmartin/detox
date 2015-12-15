package com.moozvine.detox.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BuilderSynthesiser {
  private static final DateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
  private final StandardMethodSynthesiser standardMethodSynthesiser = new StandardMethodSynthesiser();
  private final ProcessingEnvironment processingEnv;

  public BuilderSynthesiser(final ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  void writeBuilderFor(final ElementToProcess elementToProcess)
      throws IOException, InvalidTypeException {
    final JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
        elementToProcess.getBuilderFullName());
    final BufferedWriter w = new BufferedWriter(jfo.openWriter());
    w.append(String.format("" +
            "package %1$s;                                                                     \n" +
            "                                                                                  \n" +
            "import java.util.*;                                                                                  \n" +
            "import javax.annotation.Generated;                                                 \n" +
            "import com.moozvine.detox.BestEffortSerializer;                                   \n" +
            "import com.moozvine.detox.Serializable;                                           \n" +
            "                                                                                  \n" +
            "@SuppressWarnings(\"unchecked\")                                                  \n" +
            "@Generated(value=\"%2$s\", date=\"%3$s\")                                                  \n" +
            "public final class %4$s {                                                         \n" +
            "  private %4$s() {}                                                                                \n" +
            "                                                                                  \n" +
            "",
        elementToProcess.getPackageName(),
        BuilderSynthesiser.class.getCanonicalName(),
        ISO8601.format(new Date()),
        elementToProcess.getBuilderSimpleName()
    ));

    writeFactoryMethods(
        w, elementToProcess);
    writeBuilderInterfaces(
        w, elementToProcess.getNonNullMembers());
    writeCanBuildInterface(
        w, elementToProcess);
    writeValueHolder(w, elementToProcess);
    writeCopyValueHolder(w, elementToProcess);

    w.append("}\n");
    w.close();
  }

  private void writeFactoryMethods(
      final BufferedWriter w,
      final ElementToProcess elementToProcess) throws IOException {
    final String firstInterface = elementToProcess.getNonNullMembers().isEmpty()
        ? "CanBuild"
        : elementToProcess.getNonNullMembers().get(0).getBuilderInterfaceName();
    w.append(String.format("" +
        "  public static %1$s newBuilder() {                     \n" +
        "    return new ValueHolder();                           \n" +
        "  }                                                     \n" +
        "\n" +
        "  public static CopyValueHolder copyOf(final %2$s toCopy) { \n" +
        "    return new CopyValueHolder(toCopy);                     \n" +
        "  }                                                     \n" +
        "\n", firstInterface, elementToProcess.getInterfaceSimpleName()));
  }

  //TODO: For member that are of a collection type, eg List<String> getMyIds:
  // 1. Allow it to be empty. Perhaps: public NextInterface withEmptyMyIds()
  // 2. Expose addMyId(String id) method on NextInterface

  private void writeBuilderInterfaces(
      final BufferedWriter w,
      final List<SerializableMember> nonNullMembers)
      throws IOException, InvalidTypeException {

    String previousInterface = "CanBuild";
    for (int i = nonNullMembers.size() - 1; i >= 0; --i) {
      final SerializableMember member = nonNullMembers.get(i);
      w.append(String.format("" +
              "  public interface %1$s {              \n" +
              "    %2$s with%3$s(%4$s %5$s);          \n" +
              "  }                                    \n" +
              "\n",
          member.getBuilderInterfaceName(),
          previousInterface,
          Util.upperCaseFirstChar(member.getFieldName()),
          member.getTypeMirror().toString(),
          member.getFieldName()
      ));
      previousInterface = member.getBuilderInterfaceName();
    }
  }

  private void writeCanBuildInterface(
      final BufferedWriter w,
      final ElementToProcess elementToProcess) throws IOException {
    w.append(String.format("" +
            "  public interface CanBuild {             \n" +
            "    %1$s build();                         \n" +
            "",
        elementToProcess.getInterfaceSimpleName()));
    for (final SerializableMember member : elementToProcess.getNullableMembers()) {
      w.append(String.format("" +
              "    CanBuild with%1$s(%2$s %3$s %4$s);       \n" +
              "",
          Util.upperCaseFirstChar(member.getFieldName()),
          member.getNullableAnnotation(),
          member.getTypeMirror().toString(),
          member.getFieldName()
      ));
    }
    w.append("  }\n");
    w.newLine();
  }

  private void writeValueHolder(
      final BufferedWriter w,
      final ElementToProcess elementToProcess) throws IOException, InvalidTypeException {
    w.append("  public static class ValueHolder implements  \n");

    final List<SerializableMember> nonNullMembers = elementToProcess.getNonNullMembers();
    final List<SerializableMember> nullableMembers = elementToProcess.getNullableMembers();
    for (final SerializableMember member : nonNullMembers) {
      w.append(String.format("" +
              "      %1$s,          \n" +
              "",
          member.getBuilderInterfaceName()));
    }
    w.append("      CanBuild {      \n");
    w.newLine();


    for (final SerializableMember member : elementToProcess.getSerializableMembers()) {
      w.append(String.format(
          "    private %1$s %2$s;  \n",
          member.getTypeAsString(),
          member.getFieldName()
      ));
    }
    w.newLine();

    w.append("    private ValueHolder(){}\n\n");

    String previousInterface = "CanBuild";
    for (int i = nonNullMembers.size() - 1; i >= 0; --i) {
      final SerializableMember member = nonNullMembers.get(i);
      w.append(String.format("" +
              "    public %1$s with%2$s(final %3$s %4$s) {              \n",
          previousInterface,
          Util.upperCaseFirstChar(member.getFieldName()),
          member.getTypeMirror().toString(),
          member.getFieldName()));

      if (!member.getTypeMirror().getKind().isPrimitive()) {
        w.append(String.format("" +
                "      if (%2$s == null) {                                \n" +
                "        throw new IllegalArgumentException(              \n" +
                "            \"%1$s.%2$s is not permitted to be null.\"); \n" +
                "      }                                                  \n",
            elementToProcess.getInterfaceSimpleName(),
            member.getFieldName()));
      }

      writeFieldValidator(w, member);

      w.append(String.format("" +
              "      this.%1$s = %1$s;                                  \n" +
              "      return this;                                       \n" +
              "    }                                                    \n" +
              "\n",
          member.getFieldName()));
      previousInterface = member.getBuilderInterfaceName();
    }

    for (final SerializableMember member : nullableMembers) {
      w.append(String.format("" +
              "    public CanBuild with%1$s(%2$s final %3$s %4$s) { \n",
          Util.upperCaseFirstChar(member.getFieldName()),
          member.getNullableAnnotation(),
          member.getTypeMirror().toString(),
          member.getFieldName()
      ));

      writeFieldValidator(w, member);

      w.append(String.format("" +
              "      this.%1$s = %1$s;                              \n" +
              "      return this;                                   \n" +
              "    }                                                \n" +
              "\n",
          member.getFieldName()
      ));
    }

    w.append(String.format(
        "    public %1$s build() {      \n" +
            "      final %1$s detox_result = new %1$s() {      \n",
        elementToProcess.getInterfaceSimpleName()));

    for (final SerializableMember member : elementToProcess.getSerializableMembers()) {
      w.append(String.format("" +
              "        @Override                            \n" +
              "        public %1$s %2$s() {                 \n" +
              "          return %3$s;                       \n" +
              "        }                                    \n" +
              "\n",
          member.getTypeAsString(),
          member.getGetterName(),
          member.getFieldName()));
    }

    standardMethodSynthesiser.writeEqualityMethods(
        w, elementToProcess.getInterfaceSimpleName(), elementToProcess.getSerializableMembers());
    standardMethodSynthesiser.writeReflectiveToStringMethod(
        w, elementToProcess.getInterfaceSimpleName(), elementToProcess.getSerializableMembers());

    w.append("      };     \n");
    if (elementToProcess.getValidatorFullName() != null) {
      w.append(String.format(
          "      new %1$s().validate(detox_result); \n",
          elementToProcess.getValidatorFullName()));
    }
    w.append("      return detox_result;\n");
    w.append("    }\n");
    w.append("  }\n");
  }

  private void writeCopyValueHolder(
      final BufferedWriter w,
      final ElementToProcess elementToProcess) throws IOException, InvalidTypeException {
    w.append("  public static class CopyValueHolder implements  \n");

    final List<SerializableMember> nonNullMembers = elementToProcess.getNonNullMembers();
    final List<SerializableMember> nullableMembers = elementToProcess.getNullableMembers();
    for (final SerializableMember member : nonNullMembers) {
      w.append(String.format("" +
              "      %1$s,          \n" +
              "",
          member.getBuilderInterfaceName()));
    }
    w.append("      CanBuild {      \n");
    w.newLine();

//
//
//    for (final SerializableMember member : elementToProcess.getSerializableMembers()) {
//      w.append(String.format(
//          "    private %1$s %2$s;  \n",
//          member.getTypeAsString(),
//          member.getFieldName()
//      ));
//    }
    w.append("    private final ValueHolder valueHolder = new ValueHolder();\n\n");


    w.newLine();

    w.append(String.format("    private CopyValueHolder(final %1$s toCopy) {\n",
        elementToProcess.getInterfaceSimpleName()));
    for (final SerializableMember member : elementToProcess.getSerializableMembers()) {
      w.append(String.format("" +
              "      valueHolder.with%1$s(toCopy.%2$s());\n",
          Util.upperCaseFirstChar(member.getFieldName()),
          member.getGetterName()));
    }
    w.append("    }\n\n");

    for (final SerializableMember member : elementToProcess.getSerializableMembers()) {
      w.append(String.format("" +
              "    public CopyValueHolder with%1$s(final %2$s %3$s) {     \n",
          Util.upperCaseFirstChar(member.getFieldName()),
          member.getTypeMirror().toString(),
          member.getFieldName()));

      w.append(String.format("" +
              "      valueHolder.with%1$s(%2$s);                        \n" +
              "      return this;                                       \n" +
              "    }                                                    \n" +
              "\n",
          Util.upperCaseFirstChar(member.getFieldName()),
          member.getFieldName()
      ));
    }

    w.append(String.format("" +
            "    public %1$s build() {\n" +
            "      return valueHolder.build();\n" +
            "    }\n",
        elementToProcess.getInterfaceSimpleName()));
    w.append("  }\n");
  }

  private void writeFieldValidator(
      final BufferedWriter w,
      final SerializableMember member) throws IOException {
    if (member.hasValidator()) {
      w.append(String.format("" +
              "      new %1$s().validate(\"%2$s\", %2$s);  \n",
          member.getValidator(),
          member.getFieldName()));
    }
  }
}
