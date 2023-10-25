package amar.das.acbook.model;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFileModel implements Serializable,Comparable<TextFileModel> {
    public String getAbsolutePath() {
        return absolutePath;
    }
    public void setAbsolutePath(String absolutePath){
        this.absolutePath = absolutePath;
    }
    private String absolutePath;
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int compareTo(TextFileModel object) {
    return Integer.compare(extractIdNumber(this.getFileName()), extractIdNumber(object.getFileName()));
    }

    private int extractIdNumber(String fileName) {   // Helper method to extract the "id" number from a file name
        Matcher matcher = Pattern.compile("id(\\d+)").matcher(fileName);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;// Default value 0 if "id" number is not found

//        Suppose you have the input string "id42invoice123". When you apply the regular expression pattern "id(\\d+)",
//        it matches the "id" part and captures the digits following it. In this case, it captures "42".
//        matcher.
//         find() returns true because a match is found.
//         matcher.group(1) retrieves "42", which is the first captured group (the "id" number).
//         Integer.parseInt("42") converts the extracted string "42" to an integer value of 42.

//        what does it mean ? Pattern.compile("id(\\d+)")
//        "id": This part of the pattern is a literal match for the characters "id" in the input string. The characters "id" must
//        appear in that order in the string for the pattern to match.
//
//        (\\d+): This is the part of the pattern that specifies what should be captured. It consists of the following:
//
//        ( and ): Parentheses are used to create a capturing group. In this case, they define a capturing group around
//        the \\d+ part, indicating that whatever matches the \\d+ part should be captured.
//
//       \\d+: This part of the pattern matches one or more digits (0-9). It's represented using the escape sequence \\d,
//       where \\ is an escape character in Java and is used to indicate that the following character d should be treated as
//        a regular character. + is a quantifier that means "one or more."
    }

}
