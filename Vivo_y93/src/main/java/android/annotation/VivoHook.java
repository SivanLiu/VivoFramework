package android.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface VivoHook {

    public enum VivoHookType {
        CHANGE_ACCESS("CHANGE_ACCESS"),
        CHANGE_CODE("CHANGE_CODE"),
        CHANGE_CODE_AND_ACCESS("CHANGE_CODE_AND_ACCESS"),
        CHANGE_PARAMETER("CHANGE_PARAMETER"),
        CHANGE_PARAMETER_AND_ACCESS("CHANGE_PARAMETER_AND_ACCESS"),
        CHANGE_BASE_CLASS("CHANGE_BASE_CLASS"),
        NEW_CLASS("NEW_CLASS"),
        NEW_FIELD("NEW_FIELD"),
        NEW_METHOD("NEW_METHOD"),
        PUBLIC_API_METHOD("PUBLIC_API_METHOD"),
        PUBLIC_API_CLASS("PUBLIC_API_CLASS"),
        PUBLIC_API_CLASS_PART("PUBLIC_API_CLASS_PART"),
        PUBLIC_API_FIELD("PUBLIC_API_FIELD");
        
        private String typeString;

        private VivoHookType(String type) {
            this.typeString = type;
        }

        public String toString() {
            return this.typeString;
        }
    }

    VivoHookType[] arrayOfHookType() default {VivoHookType.CHANGE_ACCESS, VivoHookType.CHANGE_CODE, VivoHookType.CHANGE_CODE_AND_ACCESS, VivoHookType.CHANGE_PARAMETER, VivoHookType.CHANGE_PARAMETER_AND_ACCESS, VivoHookType.CHANGE_BASE_CLASS, VivoHookType.NEW_CLASS, VivoHookType.NEW_FIELD, VivoHookType.NEW_METHOD, VivoHookType.PUBLIC_API_METHOD, VivoHookType.PUBLIC_API_CLASS, VivoHookType.PUBLIC_API_CLASS_PART, VivoHookType.PUBLIC_API_FIELD};

    VivoHookType hookType() default VivoHookType.CHANGE_CODE;
}
