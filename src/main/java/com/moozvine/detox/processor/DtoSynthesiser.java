package com.moozvine.detox.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.DeclaredType;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.moozvine.detox.processor.Util.hasValueOfMethod;

public class DtoSynthesiser {
  private final ProcessingEnvironment processingEnv;

  public DtoSynthesiser(final ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  void writeDtoFor(final ElementToProcess elementToProcess)
      throws IOException, InvalidTypeException {
    final List<SerializableMember> members = elementToProcess.getSerializableMembers();
    final JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
        elementToProcess.getDtoFullName());
    final BufferedWriter w = new BufferedWriter(jfo.openWriter());
    w.append(String.format("" +
            "package %1$s;                                                                     \n" +
            "                                                                                  \n" +
            "import com.moozvine.detox.DTO;                                                    \n" +
            "import com.moozvine.detox.AbstractSerializationService;                           \n" +
            "import com.moozvine.detox.DeserializationException;                               \n" +
            "import com.moozvine.detox.ObjectFactory;                                          \n" +
            "import org.json.JSONArray;                                                        \n" +
            "import org.json.JSONException;                                                    \n" +
            "import org.json.JSONObject;                                                       \n" +
            "import java.util.*;                                                               \n" +
            "                                                                                  \n" +
            "@SuppressWarnings(\"unchecked\")                                                  \n" +
            "public final class %2$s implements %3$s, DTO {                                    \n" +
            "                                                                                  \n" +
            "",
        elementToProcess.getPackageName(),
        elementToProcess.getDtoSimpleName(),
        elementToProcess.getInterfaceSimpleName()
    ));

    writeFields(w, members);
    w.newLine();
    writeCopyConstructors(
        w, elementToProcess.getDtoSimpleName(), elementToProcess.getInterfaceSimpleName(), members);
    writeJsonConstructors(
        w, elementToProcess.getDtoSimpleName(), elementToProcess.getInterfaceSimpleName(), members);
    writeAccessors(w, members);
    writeStandardMethods(
        w, elementToProcess.getInterfaceSimpleName(), members);
    writeFactory(
        w, elementToProcess.getDtoSimpleName(), elementToProcess.getInterfaceSimpleName());

    w.append("}\n");
    w.close();
  }

  private void writeFields(
      final BufferedWriter w,
      final List<SerializableMember> members) throws IOException, InvalidTypeException {
    w.append("  private final JSONObject json;\n");
    for (final SerializableMember member : members) {
      w.append(String.format("" +
              "  private final %1$s %2$s;                                                     \n",
          member.getTypeAsString(),
          member.getFieldName()
      ));
    }
  }


  private void writeCopyConstructors(
      final BufferedWriter w,
      final String dtoName,
      final String interfaceName,
      final List<SerializableMember> members)
      throws IOException, InvalidTypeException {
    w.append(String.format("" +
            "  private %1$s(final AbstractSerializationService service, final %2$s original) {            \n" +
            "    json = new JSONObject();                                                   \n" +
            "    json.put(\"serializedType\", %2$s.class.getName());                        \n" +
            "                                                                                 \n" +
            "",
        dtoName,
        interfaceName
    ));

    for (final SerializableMember member : members) {
      switch (member.getMemberType()) {
        case SERIALIZABLE_TYPE:
          appendCopyFragmentForSerializableType(w, member);
          break;

        case NON_SERIALIZABLE_TYPE:
          appendCopyFragmentForNonSerializableType(w, member);
          break;

        case COLLECTION:
          appendCopyFragmentForCollections(w, member);
          break;

        case STRING_MAP:
          appendCopyFragmentForStringMap(w, member);
          break;

        case ENUM:
          appendCopyFragmentForEnum(w, member);
          break;

        case NATIVE_TYPE:
          appendCopyFragmentForPrimitive(w, member);
          break;

        default:
          throw new InvalidTypeException("Unknown field type: " + member.getMemberType());
          /*
          w.append(String.format("" +
                  "    %1$s = original.%2$s();                                                  \n" +
                  "    if (%1$s != null) {                                                      \n" +
                  "      json.put(\"%1$s\", %1$s);                                              \n" +
                  "    } else {                                                                 \n" +
                  "      json.put(\"%1$s\", JSONObject.NULL);                                   \n" +
                  "    }                                                                        \n" +
                  "                                                                               \n",
              member.getFieldName(),
              member.getGetterName()
          ));
          */
      }
    }
    w.append("  }\n");
  }

  private void appendCopyFragmentForPrimitive(
      final BufferedWriter w,
      final SerializableMember member) throws IOException {
    appendCopyFragmentForPrimitive(w, String.format("" +
            "    %1$s = original.%2$s();                                                  \n" +
            "    json.put(\"%3$s\", %1$s);                                                \n" +
            "                                                                               \n",
        member.getFieldName(),
        member.getGetterName(),
        member.getJsonFieldName()
    ));
  }

  private void appendCopyFragmentForPrimitive(
      final BufferedWriter w,
      final String format) throws IOException {
    w.append(format);
  }

  private void appendCopyFragmentForEnum(
      final BufferedWriter w,
      final SerializableMember member) throws IOException {
    w.append(String.format("" +
            "    %1$s = original.%2$s();                                                  \n" +
            "    if (%1$s != null) {                                                      \n" +
            "      json.put(\"%3$s\", %1$s.name());                                       \n" +
            "    } else {                                                                 \n" +
            "      json.put(\"%3$s\", JSONObject.NULL);                                   \n" +
            "    }                                                                        \n" +
            "                                                                             \n",
        member.getFieldName(),
        member.getGetterName(),
        member.getJsonFieldName()
    ));
  }

  private void appendCopyFragmentForCollections(
      final BufferedWriter w,
      final SerializableMember member) throws InvalidTypeException, IOException {
    final SerializableCollectionType serializableCollectionType
        = SerializableCollectionType.fromTypeMirror(member.getTypeMirror());
    if (serializableCollectionType == null) {
      throw new IllegalArgumentException("Cannot determine type of collection. " +
          "Field: " + member.getFieldName() + ", Type: " + member.getTypeMirror());
    }
    final DeclaredType collectionMemberType = member.getCollectionMemberType();
    final String elementConverter;
    final String elementToJson;
    if (collectionMemberType == null) { // E.g. non-generic List
      elementConverter = "element";
      elementToJson = "service.toJson(element)";
    } else {
      switch (MemberType.fromTypeMirror(collectionMemberType)) {
        case ENUM:
        case NON_SERIALIZABLE_TYPE:
        case NATIVE_TYPE:
          elementConverter = "element";
          elementToJson = "service.toJson(element)";
          break;

        case SERIALIZABLE_TYPE:
          elementConverter = String.format("(%1$s) service.createDTO(element, %1$s.class)", collectionMemberType);
          elementToJson = String.format("((DTO) %1$sDTO).toJson()", member.getFieldName()); // TODO: HUH???
          break;

        case COLLECTION:
          // TODO: Support collections of collections.
          throw new IllegalArgumentException("Collections of collections are not yet supported. Field: " +
              member.getFieldName());

        default:
          throw new IllegalArgumentException("Cannot have a collection of members of type " + collectionMemberType
              + ". Field: " + member.getFieldName());
      }
    }

    w.append(String.format("" +
            "    %1$s%2$s %3$sBuilder = new %4$s%2$s();                                    \n" +
            "    json.put(\"%11$s\", new JSONArray());                                    \n" +
            "    if (original.%6$s() != null) {                                          \n" +
            "      for(%5$s element : original.%6$s()) {                                 \n" +
            "        if (element == null) {                                              \n" +
            "          %3$sBuilder.add(null);                                            \n" +
            "          json.append(\"%11$s\", JSONObject.NULL);                           \n" +
            "        } else {                                                            \n" +
            "          %5$s %3$sDTO = %7$s;                                              \n" +
            "          %3$sBuilder.add(%3$sDTO);                                         \n" +
            "          json.append(\"%11$s\", %10$s);                                     \n" +
            "        }                                                                   \n" +
            "      }                                                                     \n" +
            "      %3$s = %8$s(%3$sBuilder);                                             \n" +
            "    } else {                                                                \n" +
            "      %3$s = %9$s;                                                          \n" +
            "    }                                                                       \n" +
            "                                                                            \n",
        Util.typeToString(member.getTypeMirror()), // 1
        collectionMemberType == null
            ? ""
            : "<" + Util.declaredTypeToString(collectionMemberType) + ">", //2
        member.getFieldName(), //3
        serializableCollectionType.concreteName, //4
        collectionMemberType == null
            ? "Object"
            : Util.declaredTypeToString(collectionMemberType), // 5
        member.getGetterName(), // 6
        elementConverter, // 7
        serializableCollectionType.toImmutable, // 8
        serializableCollectionType.toEmpty, // 9
        elementToJson, // 10
        member.getJsonFieldName() // 11
    ));
  }

  private void appendCopyFragmentForStringMap(
      final BufferedWriter w,
      final SerializableMember member) throws InvalidTypeException, IOException {
    final StringMapType stringMap = StringMapType.fromTypeMirror(member.getTypeMirror());
    if (stringMap == null) {
      throw new IllegalArgumentException("Cannot determine type of Map<String, T>. " +
          "Field: " + member.getFieldName() + ", Type: " + member.getTypeMirror());
    }

    final String keyToStringConverter;
    switch (MemberType.fromTypeMirror(stringMap.getKeyType())) {
      case ENUM:
        keyToStringConverter = "element.getKey().name()";
        break;

      case NATIVE_TYPE: // i.e. String
        keyToStringConverter = "element.getKey()";
        break;

      default:
        throw new IllegalArgumentException("Cannot serialize a Map with key type of " + stringMap.getKeyType());
    }

    final DeclaredType mapMemberType = stringMap.getMemberType();
    final String elementConverter;
    final String elementToJson;
    switch (MemberType.fromTypeMirror(mapMemberType)) {
      case ENUM:
      case NON_SERIALIZABLE_TYPE:
      case NATIVE_TYPE:
        elementConverter = "element.getValue()";
        elementToJson = "service.toJson(element.getValue())";
        break;

      case SERIALIZABLE_TYPE:
        elementConverter = String.format("(%1$s) service.createDTO(element.getValue(), %1$s.class)", mapMemberType);
        elementToJson = String.format("((DTO) %1$sDTO).toJson()", member.getFieldName());
        break;

      case COLLECTION:
        // TODO: Support collections as String map members
        throw new IllegalArgumentException("Collections of collections are not yet supported. Field: " +
            member.getFieldName());

      default:
        throw new IllegalArgumentException("Cannot have a Map<String,T> where T is type " + mapMemberType
            + ". Field: " + member.getFieldName());
    }

    w.append(String.format("" +
            "    Map<%4$s, %2$s> %3$sBuilder = new HashMap<>();                          \n" +
            "    json.put(\"%11$s\", new JSONObject());                                   \n" +
            "    if (original.%6$s() != null) {                                          \n" +
            "      for(Map.Entry<%4$s, %2$s> element : original.%6$s().entrySet()) {     \n" +
            "        if (element.getValue() == null) {                                   \n" +
            "          %3$sBuilder.put(element.getKey(), null);                          \n" +
            "          json.getJSONObject(\"%11$s\")                                      \n" +
            "              .put(%5$s, JSONObject.NULL);                                  \n" +
            "        } else {                                                            \n" +
            "          %2$s %3$sDTO = %7$s;                                              \n" +
            "          %3$sBuilder.put(element.getKey(), %3$sDTO);                       \n" +
            "          json.getJSONObject(\"%11$s\")                                      \n" +
            "              .put(%5$s, %10$s);                                            \n" +
            "        }                                                                   \n" +
            "      }                                                                     \n" +
            "      %3$s = Collections.unmodifiableMap(%3$sBuilder);                      \n" +
            "    } else {                                                                \n" +
            "      %3$s = Collections.emptyMap();                                        \n" +
            "    }                                                                       \n" +
            "                                                                            \n",
        Util.typeToString(member.getTypeMirror()), // 1
        Util.declaredTypeToString(mapMemberType), //2
        member.getFieldName(), //3
        stringMap.getKeyType(), //4
        keyToStringConverter, // 5
        member.getGetterName(), // 6
        elementConverter, // 7
        "unused", // 8
        "unused", // 9
        elementToJson, // 10
        member.getJsonFieldName()  // 11
    ));
  }

  private void appendCopyFragmentForNonSerializableType(
      final BufferedWriter w,
      final SerializableMember member) throws IOException {
    w.append(String.format("" +
            "    %1$s = original.%3$s();                                            \n" +
            "    if (%1$s != null) {                                                      \n" +
            "      json.put(\"%4$s\", service.toJson(%1$s));                           \n" +
            "    } else {                                                                 \n" +
            "      json.put(\"%4$s\", JSONObject.NULL);                                   \n" +
            "    }                                                                        \n" +
            "                                                                             \n",
        member.getFieldName(),
        member.getTypeAsCode(),
        member.getGetterName(),
        member.getJsonFieldName()
    ));
  }

  private void appendCopyFragmentForSerializableType(
      final BufferedWriter w,
      final SerializableMember member) throws IOException {
    w.append(String.format("" +
            "    final %2$s original%1$s = original.%3$s();                               \n" +
            "    if (original%1$s != null) {                                              \n" +
            "      %1$s = (%2$s) service.createDTO(original%1$s, %2$s.class);             \n" +
            "      json.put(\"%4$s\", service.createJSONObject(%1$s, %2$s.class));        \n" +
            "    } else {                                                                 \n" +
            "      %1$s = null;                                                           \n" +
            "      json.put(\"%4$s\", JSONObject.NULL);                                   \n" +
            "    }                                                                        \n" +
            "                                                                             \n",
        member.getFieldName(),
        member.getTypeAsCode(),
        member.getGetterName(),
        member.getJsonFieldName()
    ));
  }

  private void writeJsonConstructors(
      final BufferedWriter w,
      final String dtoName,
      final String interfaceName,
      final List<SerializableMember> members)
      throws IOException, InvalidTypeException {
    // TODO: Restructure this method as per the writeCopyConstructor method using the MemberType instead of these
    // intolerable conditionals.

    w.append(String.format("" +
            "  private %1$s(final AbstractSerializationService service, final JSONObject json)   \n" +
            "      throws DeserializationException {                                             \n" +
            "    try {                                                                           \n" +
            "      this.json = json;                                                             \n" +
            "      json.put(\"serializedType\", %2$s.class.getName());                           \n" +
            "                                                                                    \n" +
            "",
        dtoName,
        interfaceName
    ));

    for (final SerializableMember member : members) {
      if (Util.isCollaborator(member.getTypeMirror())) {
        if (Util.isSerializable(member.getTypeMirror())) {
          w.append(String.format("" +
                  "      if (json.isNull(\"%3$s\")) {                                             \n" +
                  "        %1$s = null;                                                           \n" +
                  "      } else {                                                                 \n" +
                  "        %1$s = (%2$s) service.deserialize(json.getJSONObject(\"%3$s\"));       \n" +
                  "      }                                                                        \n" +
                  "                                                                               \n",
              member.getFieldName(),
              Util.declaredTypeToString((DeclaredType) member.getTypeMirror()),
              member.getJsonFieldName()
          ));
        } else {
          w.append(String.format("" +
                  "      if (json.isNull(\"%4$s\")) {                                             \n" +
                  "        %1$s = null;                                                           \n" +
                  "      } else {                                                                 \n" +
                  "        %1$s = (%2$s) service.deserialize(json.get(\"%4$s\"), %3$s.class);       \n" +
                  "      }                                                                        \n" +
                  "                                                                               \n",
              member.getFieldName(),
              Util.declaredTypeToString((DeclaredType) member.getTypeMirror()),
              Util.degenerify(Util.declaredTypeToString((DeclaredType) member.getTypeMirror())),
              member.getJsonFieldName()
          ));
        }
      } else {
        final SerializableCollectionType serializableCollectionType = SerializableCollectionType.fromTypeMirror(member.getTypeMirror());
        if (serializableCollectionType != null) {
          final DeclaredType typeArgument = Util.getCollectionMemberTypeMirror(member.getTypeMirror());
          final String elementConverter;
          if (typeArgument == null) { // E.g. non-generic List
            elementConverter = String.format("(%1$s) service.deserialize(%2$sArray.get(i), %1$s.class)",
                typeArgument,
                member.getFieldName());
          } else {
            final MemberType collectionMemberType = MemberType.fromTypeMirror(typeArgument);
            switch (collectionMemberType) {
              case ENUM:
              case NATIVE_TYPE:
                elementConverter = Util.typeToJsonArrayGetter(
                    member.getFieldName() + "Array", typeArgument, "i");
                break;

              case NON_SERIALIZABLE_TYPE:
                elementConverter = String.format("(%1$s) service.deserialize(%2$sArray.get(i), %1$s.class)",
                    typeArgument,
                    member.getFieldName());
                break;

              case SERIALIZABLE_TYPE:
                elementConverter = String.format("(%1$s) service.deserialize(%2$sArray.getJSONObject(i), %1$s.class)",
                    typeArgument,
                    member.getFieldName());
                break;

              case COLLECTION:
                // TODO: Support collections of collections.
                throw new IllegalArgumentException("Collections of collections are not yet supported. Field: " +
                    member.getFieldName());

              default:
                throw new IllegalArgumentException("Cannot have a collection of members of type " + collectionMemberType
                    + ". Field: " + member.getFieldName());
            }
          }
          w.append(String.format("" +
                  "      %1$s<%2$s> %3$sBuilder = new %4$s<>();                                  \n" +
                  "      if(json.has(\"%8$s\")) {                                            \n" +
                  "        JSONArray %3$sArray = json.getJSONArray(\"%8$s\");                      \n" +
                  "        for(int i=0; i<%3$sArray.length(); ++i) {                                   \n" +
                  "          %3$sBuilder.add(%6$s);                                                \n" +
                  "        }                                                                       \n" +
                  "      }                                                                       \n" +
                  "      %3$s = %7$s(%3$sBuilder);                                               \n" +
                  "                                                                              \n",
              Util.typeToString(member.getTypeMirror()), // 1
              typeArgument, // 2
              member.getFieldName(), // 3
              serializableCollectionType.concreteName, // 4
              member.getGetterName(), // 5
              elementConverter, // 6
              serializableCollectionType.toImmutable, // 7
              member.getJsonFieldName() // 8
          ));


        } else if (StringMapType.fromTypeMirror(member.getTypeMirror()) != null) {
          final StringMapType stringMapType = StringMapType.fromTypeMirror(member.getTypeMirror());
          final DeclaredType mapMemberDeclaredType = stringMapType.getMemberType();
          final String elementConverter;
          final String keyFromStringConverter;
          switch (MemberType.fromTypeMirror(stringMapType.getKeyType())) {
            case ENUM:
              keyFromStringConverter = stringMapType.getKeyType() + ".valueOf(key)";
              break;

            case NATIVE_TYPE: // i.e. String
              keyFromStringConverter = "key";
              break;

            default:
              throw new IllegalArgumentException("Cannot serialize a Map with key type " + stringMapType.getKeyType());
          }

          final MemberType mapMemberType = MemberType.fromTypeMirror(mapMemberDeclaredType);
          switch (mapMemberType) {
            case ENUM:
            case NATIVE_TYPE:
              elementConverter = Util.typeToJsonArrayGetter(
                  member.getFieldName() + "Obj", mapMemberDeclaredType, "key");
              break;

            case NON_SERIALIZABLE_TYPE:
              elementConverter = String.format("(%1$s) service.deserialize(%2$sObj.get(key), %1$s.class)",
                  mapMemberDeclaredType,
                  member.getFieldName());
              break;

            case SERIALIZABLE_TYPE:
              elementConverter = String.format("(%1$s) service.deserialize(%2$sObj.getJSONObject(key), %1$s.class)",
                  mapMemberDeclaredType,
                  member.getFieldName());
              break;

            case COLLECTION:
              // TODO: Support collections of collections.
              throw new IllegalArgumentException("StringMaps of collections are not yet supported. Field: " +
                  member.getFieldName());

            default:
              throw new IllegalArgumentException("Cannot have a Map<String,T> with T of type " + mapMemberType
                  + ". Field: " + member.getFieldName());
          }

          w.append(String.format("" +
                  "      Map<%1$s, %2$s> %3$sBuilder = new HashMap<>();                            \n" +
                  "      if(json.has(\"%7$s\")) {                                                  \n" +
                  "        JSONObject %3$sObj = json.getJSONObject(\"%7$s\");                      \n" +
                  "        for(String key : (Set<String>)%3$sObj.keySet()) {                       \n" +
                  "          %3$sBuilder.put(%4$s,                                                 \n" +
                  "              %3$sObj.isNull(key) ? null : %6$s);                               \n" +
                  "        }                                                                       \n" +
                  "      }                                                                         \n" +
                  "      %3$s = Collections.unmodifiableMap(%3$sBuilder);                          \n" +
                  "                                                                                \n",
              stringMapType.getKeyType(), // 1
              mapMemberDeclaredType, // 2
              member.getFieldName(), // 3
              keyFromStringConverter, // 4
              "unused", // 5
              elementConverter, // 6
              member.getJsonFieldName() // 7
          ));
        } else if (hasValueOfMethod(member.getTypeMirror())) {
          w.append(String.format("" +
                  "      if (json.isNull(\"%3$s\")) {                                             \n" +
                  "        %1$s = null;                                                           \n" +
                  "      } else {                                                                 \n" +
                  "        %1$s = %2$s.valueOf(String.valueOf(json.get(\"%3$s\")));               \n" +
                  "      }                                                                        \n" +
                  "                                                                               \n",
              member.getFieldName(),
              member.getTypeMirror(),
              member.getJsonFieldName()
          ));
        } else {
          w.append(String.format("" +
                  "      %1$s = %2$s;                                                           \n" +
                  "                                                                             \n",
              member.getFieldName(),
              Util.typeToJsonGetter("json", member.getTypeMirror(), member.getJsonFieldName())
          ));
        }
      }
    }
    w.append("" +
            "    } catch (final JSONException e) {                                          \n" +
            "      throw new IllegalArgumentException(e);                                   \n" +
            "    }                                                                          \n" +
            "  }                                                                            \n" +
            "                                                                               \n"
    );
  }

  private void writeAccessors(
      final BufferedWriter w,
      final List<SerializableMember> members) throws IOException, InvalidTypeException {
    for (final SerializableMember member : members) {
      w.append(String.format("" +
              "  @Override                                                                      \n" +
              "  public %1$s %2$s() {                                                           \n" +
              "    return %3$s;                                                                 \n" +
              "  }                                                                              \n" +
              "                                                                                 \n" +
              "",
          Util.typeToString(member.getTypeMirror()),
          member.getGetterName(),
          member.getFieldName()
      ));
    }

    w.append("  @Override\n");
    w.append("  public JSONObject toJson() {\n");
    w.append("    return json;\n");
    w.append("  }\n");
    w.newLine();
  }

  private void writeStandardMethods(
      final BufferedWriter w,
      final String interfaceName,
      final List<SerializableMember> members) throws IOException {
    w.append("" +
            "                                                                  \n" +
            "  @Override                                                       \n" +
            "  public String toString() {                                      \n" +
            "    try {                                                         \n" +
            "      return json.toString(2);                                    \n" +
            "    } catch (JSONException impossible) {                          \n" +
            "      throw new RuntimeException(impossible);                     \n" +
            "    }                                                             \n" +
            "  }                                                               \n"
    );

    final Set<SerializableMember> idMembers = new HashSet<>();
    for (final SerializableMember member : members) {
      if (member.isFormsId()) {
        idMembers.add(member);
      }
    }

    if (!idMembers.isEmpty()) {

      w.append(String.format("" +
              "                                                                  \n" +
              "  @Override                                                       \n" +
              "  public boolean equals(Object o) {                               \n" +
              "    if (this == o) return true;                                   \n" +
              "    if (!(o instanceof %1$s)) return false;                       \n" +
              "                                                                  \n" +
              "    %1$s that = (%1$s) o;                                         \n",
          interfaceName
      ));

      for (final SerializableMember member : idMembers) {
        if (member.getTypeMirror().getKind().isPrimitive()) {
          w.append(String.format("" +
                  "    if (%1$s() != that.%1$s()) return false;                      \n",
              member.getGetterName()));
        } else {
          w.append(String.format("" +
                  "    if ((%1$s() == null && that.%1$s() != null) ||                     \n" +
                  "        (%1$s() != null && !%1$s().equals(that.%1$s()))) return false; \n",
              member.getGetterName()));
        }
      }

      w.append("" +
          "                                                                  \n" +
          "    return true;                                                  \n" +
          "  }                                                               \n");

      w.append("" +
          "                                                                  \n" +
          "  @Override                                                       \n" +
          "  public int hashCode() {                                         \n" +
          "    int result = 17;                                              \n");

      for (final SerializableMember idMember : idMembers) {
        switch (idMember.getTypeMirror().getKind()) {
          case BOOLEAN:
            w.append(String.format(
                "    result = result * 37 + (%1$s() ? 0 : 1);\n",
                idMember.getGetterName()));
            break;
          case BYTE:
          case CHAR:
          case SHORT:
          case INT:
            w.append(String.format(
                "    result = result * 37 + (int) %1$s();\n",
                idMember.getGetterName()));
            break;
          case LONG:
            w.append(String.format(
                "    result = result * 37 + (int)(%1$s() ^ (%1$s >>> 32));\n",
                idMember.getGetterName()));
            break;
          case FLOAT:
            w.append(String.format(
                "    result = result * 37 + Float.floatItIntBits(%1$s);\n",
                idMember.getGetterName()));
            break;
          case DOUBLE:
            w.append(String.format(
                "    result = result * 37 + (int)(Double.doubleToLongBits(%1$s()) \n" +
                    "        ^ (Double.doubleToLongBits(%1$s) >>> 32));",
                idMember.getGetterName()));
            break;
          case ARRAY:
            w.append(String.format(
                "    result = result * 37 + (%1$s() == null ? 0 : Arrays.asList(%1$s()).hashCode());\n",
                idMember.getGetterName()));
            break;
          case DECLARED:
            w.append(String.format(
                "    result = result * 37 + (%1$s() == null ? 0 : %1$s().hashCode());\n",
                idMember.getGetterName()));
            break;
        }
      }
      w.append("" +
          "                                                                  \n" +
          "    return result;                                                \n" +
          "  }                                                               \n");
    }
  }

  private void writeFactory(
      final BufferedWriter w,
      final String dtoName,
      final String interfaceName)
      throws IOException {
    w.append(String.format("" +
            "                                                                                 \n" +
            "  public static class Factory implements ObjectFactory<%1$s> {                   \n" +
            "                                                                                 \n" +
            "    @Override                                                                    \n" +
            "    public DTO createDTO(AbstractSerializationService service, %1$s original) {  \n" +
            "      return new %2$s(service, original);                                        \n" +
            "    }                                                                            \n" +
            "                                                                                 \n" +
            "    @Override                                                                    \n" +
            "    public DTO createDTO(AbstractSerializationService service, JSONObject json)  \n" +
            "        throws DeserializationException {                                        \n" +
            "      return new %2$s(service, json);                                            \n" +
            "    }                                                                            \n" +
            "  }                                                                              \n" +
            "",
        interfaceName, dtoName
    ));
  }
}
