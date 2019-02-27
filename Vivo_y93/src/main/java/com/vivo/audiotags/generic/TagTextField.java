package com.vivo.audiotags.generic;

public interface TagTextField extends TagField {
    String getContent();

    String getEncoding();

    void setContent(String str);

    void setEncoding(String str);
}
