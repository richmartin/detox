public final class User {
  private final long id;
  private final String name;
  private final InternetAddress emailAddress;
  private final DateTime dateOfBirth;

  public User(
      final long id,
      final String name,
      final InternetAddress emailAddress,
      final DateTime dateOfBirth) {
    this.id = id;
    this.name = name;
    this.emailAddress = emailAddress;
    this.dateOfBirth = dateOfBirth;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public InternetAddress getEmailAddress() {
    return emailAddress;
  }

  public DateTime getDateOfBirth() {
    return dateOfBirth;
  }

  @Override public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final User user = (User) o;

    return id == user.id;

  }

  @Override public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }

  private class InternetAddress {
  }

  private class DateTime {
  }
}
