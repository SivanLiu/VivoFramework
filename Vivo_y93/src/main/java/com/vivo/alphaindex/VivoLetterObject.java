package com.vivo.alphaindex;

class VivoLetterObject {
    String count;
    String letter;

    public VivoLetterObject(String letter, String count) {
        this.letter = letter;
        this.count = count;
    }

    public String getCount() {
        return this.count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getLetter() {
        return this.letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof VivoLetterObject)) {
            return false;
        }
        VivoLetterObject t = (VivoLetterObject) o;
        if (this.letter.equals(t.letter)) {
            z = this.count.equals(t.count);
        }
        return z;
    }
}
