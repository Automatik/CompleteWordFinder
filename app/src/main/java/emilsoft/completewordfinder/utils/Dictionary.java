package emilsoft.completewordfinder.utils;

public class Dictionary {

    private String filename;
    private int alphabetSize;
    private int maxWordLength;

    public Dictionary(String filename, int alphabetSize) {
        this.filename = filename;
        this.alphabetSize = alphabetSize;
    }

    public Dictionary(String filename, int alphabetSize, int maxWordLength) {
        this.filename = filename;
        this.alphabetSize = alphabetSize;
        this.maxWordLength = maxWordLength;
    }

    public String getFilename() {
        return filename;
    }

    public int getAlphabetSize() {
        return alphabetSize;
    }

    public int getMaxWordLength() {
        return maxWordLength;
    }

    public void setMaxWordLength(int maxWordLength) {
        this.maxWordLength = maxWordLength;
    }
}
