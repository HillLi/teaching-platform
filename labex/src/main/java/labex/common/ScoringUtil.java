package labex.common;

import java.util.ArrayList;
import java.util.List;

public class ScoringUtil {

    public static boolean isAutoScorable(int type) {
        return type == 1 || type == 2 || type == 3 || type == 4;
    }

    public static Integer autoScore(int type, String referenceAnswer, String studentAnswer, int maxScore) {
        if (!isAutoScorable(type)) {
            return null;
        }
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return 0;
        }
        if (referenceAnswer == null || referenceAnswer.trim().isEmpty()) {
            return null;
        }
        switch (type) {
            case 1:
                return scoreFillInBlank(referenceAnswer, studentAnswer, maxScore);
            case 2:
                return scoreExactMatch(referenceAnswer, studentAnswer, maxScore);
            case 3:
                return scoreMultipleChoice(referenceAnswer, studentAnswer, maxScore);
            case 4:
                return scoreExactMatch(referenceAnswer, studentAnswer, maxScore);
            default:
                return null;
        }
    }

    static int scoreFillInBlank(String reference, String student, int maxScore) {
        String[] refBlanks = reference.split("\\|");
        String[] stuBlanks = student.split("\\|");
        int total = refBlanks.length;
        if (total == 0) return 0;
        int correct = 0;
        for (int i = 0; i < total; i++) {
            String ref = refBlanks[i].trim();
            String stu = i < stuBlanks.length ? stuBlanks[i].trim() : "";
            if (ref.equalsIgnoreCase(stu)) {
                correct++;
            }
        }
        if (correct == total) return maxScore;
        return Math.round((float) correct / total * maxScore);
    }

    static int scoreExactMatch(String reference, String student, int maxScore) {
        return reference.trim().equalsIgnoreCase(student.trim()) ? maxScore : 0;
    }

    static int scoreMultipleChoice(String reference, String student, int maxScore) {
        List<String> refList = sortedList(reference);
        List<String> stuList = sortedList(student);
        return refList.equals(stuList) ? maxScore : 0;
    }

    private static List<String> sortedList(String csv) {
        String[] parts = csv.toUpperCase().split("[,，]");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        list.sort(String::compareTo);
        return list;
    }
}
