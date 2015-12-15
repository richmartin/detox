package com.moozvine.detox.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class responsible for writing equals, hashcode and toString methods for a Detox type.
 */
public class StandardMethodSynthesiser {
  public void writeEqualityMethods(
      final BufferedWriter w,
      final String interfaceName,
      final List<SerializableMember> members) throws IOException {
    final Set<SerializableMember> idMembers = new HashSet<>();
    for (final SerializableMember member : members) {
      if (member.isFormsId()) {
        idMembers.add(member);
      }
    }

    if (!idMembers.isEmpty()) {

      w.append(String.format("" +
              "                                                                        \n" +
              "        @Override                                                       \n" +
              "        public boolean equals(final Object o) {                         \n" +
              "          if (this == o) return true;                                   \n" +
              "          if (!(o instanceof %1$s)) return false;                       \n" +
              "                                                                        \n" +
              "          final %1$s that = (%1$s) o;                                   \n",
          interfaceName
      ));

      for (final SerializableMember member : idMembers) {
        if (member.getTypeMirror().getKind().isPrimitive()) {
          w.append(String.format("" +
                  "          if (%1$s() != that.%1$s()) return false;                      \n",
              member.getGetterName()));
        } else {
          w.append(String.format("" +
                  "          if ((%1$s() == null && that.%1$s() != null) ||                     \n" +
                  "              (%1$s() != null && !%1$s().equals(that.%1$s()))) return false; \n",
              member.getGetterName()));
        }
      }

      w.append("" +
          "                                                                        \n" +
          "          return true;                                                  \n" +
          "        }                                                               \n");

      w.append("" +
          "                                                                        \n" +
          "        @Override                                                       \n" +
          "        public int hashCode() {                                         \n" +
          "          int detox_result = 17;                                              \n");

      for (final SerializableMember idMember : idMembers) {
        switch (idMember.getTypeMirror().getKind()) {
          case BOOLEAN:
            w.append(String.format(
                "          detox_result = detox_result * 37 + (%1$s() ? 0 : 1);\n",
                idMember.getGetterName()));
            break;
          case BYTE:
          case CHAR:
          case SHORT:
          case INT:
            w.append(String.format(
                "          detox_result = detox_result * 37 + (int) %1$s();\n",
                idMember.getGetterName()));
            break;
          case LONG:
            w.append(String.format(
                "          detox_result = detox_result * 37 + (int)(%1$s() ^ (%1$s() >>> 32));\n",
                idMember.getGetterName()));
            break;
          case FLOAT:
            w.append(String.format(
                "          detox_result = detox_result * 37 + Float.floatToIntBits(%1$s());\n",
                idMember.getGetterName()));
            break;
          case DOUBLE:
            w.append(String.format(
                "          detox_result = detox_result * 37 + (int)(Double.doubleToLongBits(%1$s()) \n" +
                    "              ^ (Double.doubleToLongBits(%1$s()) >>> 32));",
                idMember.getGetterName()));
            break;
          case ARRAY:
            w.append(String.format(
                "          detox_result = detox_result * 37 + (%1$s() == null ? 0 : Arrays.asList(%1$s()).hashCode());\n",
                idMember.getGetterName()));
            break;
          case DECLARED:
            w.append(String.format(
                "          detox_result = detox_result * 37 + (%1$s() == null ? 0 : %1$s().hashCode());\n",
                idMember.getGetterName()));
            break;
        }
      }
      w.append("" +
          "                                                                        \n" +
          "          return detox_result;                                                \n" +
          "        }                                                               \n");

    }
  }

  public void writeJsonBasedToStringMethod(final BufferedWriter w) throws IOException {
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
  }

  public void writeReflectiveToStringMethod(
      final BufferedWriter w,
      final String interfaceName,
      final List<SerializableMember> members) throws IOException {
    w.append("\n" +
        "        private final BestEffortSerializer bestEffortSerializer = new BestEffortSerializer();\n" +
        "        @Override public String toString() {\n" +
        "          return bestEffortSerializer.toString(this);\n" +
        "        }\n" +
        "");
  }
}
